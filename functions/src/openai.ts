import OpenAI from "openai";
import { getExerciseTimeseries, getRecentStats } from "./tools/analytics";
import { detectRisk } from "./tools/risk";
import { planNextWeek } from "./tools/planner";
import { logChat } from "./tools/logger";
import { getDayRecordSummary, getRecentSessions, getCategoryBreakdown, getBestSet, getPRTrend } from "./tools/records";
import { listExerciseTypes, searchExerciseTypes } from "./tools/exerciseTypes";
import { listRoutines, getRoutineDetail, recommendRoutine, addRoutine } from "./tools/routines";

/** System Prompt + Data Manifest (질문에 주신 요구사항) */
export const SYSTEM_PROMPT = `
당신은 FitLog의 개인 AI 코치입니다.
목표: 최근 4주 운동 기록을 바탕으로 대화형 피드백, 편향(부위 볼륨), 위험 신호(고RPE 연속/휴식 부족), 다음 주 루틴 제안을 합니다.
원칙:
- 톤: 간결·실전·숫자 근거. 의료행위는 회피하고 필요시 전문의 상담 안내.
- 개인정보 최소화: UID/집계 숫자만 사용, 사진/전화번호는 쓰지 않음.
- 데이터 접근은 직접 하지 않고 서버에서 제공하는 "툴"만 호출합니다.
- 한국어로 답하되 표/리스트는 짧고 읽기 좋게.

- 사용자가 "루틴/계획/짜줘/추천"을 요구하면 반드시 다음을 수행:
  1) recommend_routine 툴을 호출하여 Day1~Day4의 운동/세트/반복/권장 무게까지 포함한 상세 루틴을 제시.
  2) 서버가 제공한 serverRecommendedRoutine이 있으면 우선 참고하고 부족한 부분만 보완.
  3) 답변 **마지막에 반드시 CTA** 포함:
     - missingExerciseTypes가 비어있지 않으면:
       "exerciseType에 없는 운동: {이름1, 이름2, ...}. 이 운동들을 exerciseType에 추가하고 루틴을 저장할까요?"
     - 아니면:
       "위 루틴을 바로 저장할까요?"
  4) 사용자가 동의하면 add_routine 툴을 호출해 저장.
- 단순한 분할/세트/RPE 요약만으로 끝내지 말 것. Day1~Day4 상세 목록을 포함할 것.

출력(JSON):
{
  "reply": "사용자에게 보여줄 최종 답변",
  "highlights": ["키 포인트 1", "키 포인트 2"],
  "actions": [ { "type": "add_routine", "args": { "name": "...", "memo": "...", "exercises": [ ... ] } } ]
}
`;

export const DATA_MANIFEST = `
[DATA MANIFEST - READ ONLY]
- Firestore 루트: /users/{uid}/...

- 하루 기록 문서:
/users/{uid}/exerciseRecords/{recordId}
  - createdAt: number (ms)
  - date: string ("YYYY-MM-DD")
  - recordedAt: number (ms)
  - deleteState: boolean
  - exerciseCount: number
  - imageUrlList: string[]
  - intensity: string ("NORMAL" 등)
  - memo: string
  - recordId: string
  - volumeByCategory: map { "가슴": number, "등": number, ... }

- 하루 기록 하위 운동:
/users/{uid}/exerciseRecords/{recordId}/exercises/{itemId}
  - createdAt: number
  - exerciseCategory: string ("가슴" 등)
  - exerciseMemo: string
  - exerciseName: string
  - exerciseTypeId: string
  - itemId: string
  - order: number
  - setCount: number

- 세트:
/users/{uid}/exerciseRecords/{recordId}/exercises/{itemId}/sets/{setId}
  - createdAt: number
  - reps: number
  - setId: string
  - setNumber: number
  - weight: number
`;

export const DEFAULT_MODEL_FALLBACK = "gpt-4o-mini";
export const HEAVY_MODEL_FALLBACK = "gpt-5-mini";

/** 간단 휴리스틱: 길거나 전문 키워드가 많으면 승격 */
const HEAVY_KEYWORDS = [
  "메소사이클","주기화","RIR","디로딩","부상","재활","장비 제한",
  "볼륨 분배","과부하","마이크로사이클","근거 비교","대안 비교"
];
export function shouldEscalate(userMsg: string, force = false) {
  if (force) return true;
  if (!userMsg) return false;
  if (userMsg.length > 400) return true;
  const hit = HEAVY_KEYWORDS.some(k => userMsg.includes(k));
  return hit;
}

/** 시크릿/환경변수에서 모델 고르기 */
export function pickModelFromSecrets(
  defaultSecret?: string | null,
  heavySecret?: string | null,
  kind: "chat" | "heavy" = "chat"
) {
  if (kind === "heavy") {
    return (heavySecret && heavySecret.trim()) || HEAVY_MODEL_FALLBACK;
  }
  return (defaultSecret && defaultSecret.trim()) || DEFAULT_MODEL_FALLBACK;
}

export function newOpenAI(apiKey: string, baseURL?: string) {
  return new OpenAI({ apiKey, baseURL }); // 표준 Chat Completions + tools 사용  :contentReference[oaicite:3]{index=3}
}

/** LLM Tool Schemas */
export const TOOL_SCHEMAS = [
  {
    type: "function",
    function: {
      name: "get_exercise_timeseries",
      description: "특정 운동의 최근 N일 세트를 주차별로 집계",
      parameters: {
        type: "object",
        properties: {
          uid: { type: "string" },
          exerciseName: { type: "string" },
          sinceDays: { type: "number" }
        },
        required: ["uid", "exerciseName", "sinceDays"]
      }
    }
  },
  {
    type: "function",
    function: {
      name: "get_recent_stats",
      description: "최근 W주 전체 요약",
      parameters: {
        type: "object",
        properties: {
          uid: { type: "string" },
          weeks: { type: "number", default: 4 }
        },
        required: ["uid"]
      }
    }
  },
  {
    type: "function",
    function: {
      name: "detect_risk",
      description: "위험 신호 감지",
      parameters: {
        type: "object",
        properties: { uid: { type: "string" } },
        required: ["uid"]
      }
    }
  },
  {
    type: "function",
    function: {
      name: "plan_next_week",
      description: "다음 주 루틴 제안",
      parameters: {
        type: "object",
        properties: {
          uid: { type: "string" },
          focusTargets: { type: "array", items: { type: "string" } }
        },
        required: ["uid"]
      }
    }
  },
  {
    type: "function",
    function: {
      name: "log_chat",
      description: "대화 저장",
      parameters: {
        type: "object",
        properties: {
          uid: { type: "string" },
          sessionId: { type: "string" },
          role: { type: "string" },
          content: { type: "string" }
        },
        required: ["uid","sessionId","role","content"]
      }
    }
  },
  // === 기록/요약 ===
    {
      type: "function",
      function: {
        name: "get_day_record_summary",
        description: "하루 기록 요약(세트/볼륨/운동 목록)",
        parameters: {
          type: "object",
          properties: { uid: { type: "string" }, recordId: { type: "string" } },
          required: ["uid", "recordId"]
        }
      }
    },
    {
      type: "function",
      function: {
        name: "get_recent_sessions",
        description: "최근 N회 간단 목록(날짜/메모/카테고리 볼륨 요약)",
        parameters: {
          type: "object",
          properties: { uid: { type: "string" }, limit: { type: "number", default: 10 } },
          required: ["uid"]
        }
      }
    },
    {
      type: "function",
      function: {
        name: "get_category_breakdown",
        description: "최근 W주 카테고리 비중(%) + 절대볼륨",
        parameters: {
          type: "object",
          properties: { uid: { type: "string" }, weeks: { type: "number", default: 4 } },
          required: ["uid"]
        }
      }
    },
    // === 기록/성능 ===
    {
      type: "function",
      function: {
        name: "get_best_set",
        description: "특정 운동 최대 중량/반복(기간 내)",
        parameters: {
          type: "object",
          properties: {
            uid: { type: "string" },
            exerciseName: { type: "string" },
            sinceDays: { type: "number", default: 90 }
          },
          required: ["uid", "exerciseName"]
        }
      }
    },
    {
      type: "function",
      function: {
        name: "get_pr_trend",
        description: "특정 운동 주차별 est.1RM 추세(기간 내)",
        parameters: {
          type: "object",
          properties: {
            uid: { type: "string" },
            exerciseName: { type: "string" },
            sinceDays: { type: "number", default: 120 }
          },
          required: ["uid", "exerciseName"]
        }
      }
    },
    // === 운동 종류 ===
    {
      type: "function",
      function: {
        name: "list_exercise_types",
        description: "전체/카테고리별 운동종류 목록",
        parameters: {
          type: "object",
          properties: { uid: { type: "string" }, category: { type: "string" } },
          required: ["uid"]
        }
      }
    },
    {
      type: "function",
      function: {
        name: "search_exercise_types",
        description: "이름 부분 일치 검색",
        parameters: {
          type: "object",
          properties: { uid: { type: "string" }, keyword: { type: "string" } },
          required: ["uid", "keyword"]
        }
      }
    },
    // === 루틴 ===
    {
      type: "function",
      function: {
        name: "list_routines",
        description: "내 루틴 목록(운동 개수/메모)",
        parameters: {
          type: "object",
          properties: { uid: { type: "string" } },
          required: ["uid"]
        }
      }
    },
    {
      type: "function",
      function: {
        name: "get_routine_detail",
        description: "루틴 상세(운동/세트까지)",
        parameters: {
          type: "object",
          properties: { uid: { type: "string" }, routineId: { type: "string" } },
          required: ["uid", "routineId"]
        }
      }
    },
    {
      type: "function",
      function: {
        name: "recommend_routine",
        description: "최근 통계를 반영한 **상세 루틴** 생성(운동/세트/반복/권장무게 포함) + missingExerciseTypes + draftForAddRoutine 반환",
        parameters: {
          type: "object",
          properties: {
            uid: { type: "string" },
            focusTargets: { type: "array", items: { type: "string" } }
          },
          required: ["uid"]
        }
      }
    },
    {
      type: "function",
      function: {
        name: "add_routine",
        description: "루틴 추가(routines/{routineId} + exercises + sets)",
        parameters: {
          type: "object",
          properties: {
            uid: { type: "string" },
            name: { type: "string" },
            memo: { type: "string" },
            exercises: {
              type: "array",
              items: {
                type: "object",
                properties: {
                  exerciseName: { type: "string" },
                  exerciseCategory: { type: "string" },
                  exerciseTypeId: { type: "string" },
                  order: { type: "number" },
                  setCount: { type: "number" },
                  exerciseMemo: { type: "string" },
                  sets: {
                    type: "array",
                    items: {
                      type: "object",
                      properties: {
                        setNumber: { type: "number" },
                        reps: { type: "number" },
                        weight: { type: "number" }
                      },
                      required: ["setNumber","reps","weight"]
                    }
                  }
                },
                required: ["exerciseName","exerciseCategory","order","setCount"]
              }
            }
          },
          required: ["uid","name","exercises"]
        }
      }
    }
];

/** Tool 실행기 */
async function runTool(name: string, args: any) {
  switch (name) {
    case "get_exercise_timeseries":
      return await getExerciseTimeseries(args.uid, args.exerciseName, args.sinceDays);
    case "get_recent_stats":
      return await getRecentStats(args.uid, args.weeks || 4);
    case "detect_risk":
      return await detectRisk(args.uid);
    case "plan_next_week":
      return await planNextWeek(args.uid, args.focusTargets || []);
    case "log_chat":
      return await logChat(args.uid, args.sessionId, args.role, args.content);
        case "get_day_record_summary":
          return await getDayRecordSummary(args.uid, args.recordId);
        case "get_recent_sessions":
          return await getRecentSessions(args.uid, args.limit || 10);
        case "get_category_breakdown":
          return await getCategoryBreakdown(args.uid, args.weeks || 4);
        case "get_best_set":
          return await getBestSet(args.uid, args.exerciseName, args.sinceDays || 90);
        case "get_pr_trend":
          return await getPRTrend(args.uid, args.exerciseName, args.sinceDays || 120);
        case "list_exercise_types":
          return await listExerciseTypes(args.uid, args.category);
        case "search_exercise_types":
          return await searchExerciseTypes(args.uid, args.keyword);
        case "list_routines":
          return await listRoutines(args.uid);
        case "get_routine_detail":
          return await getRoutineDetail(args.uid, args.routineId);
        case "recommend_routine":
          return await recommendRoutine(args.uid, args.focusTargets || []);
        case "add_routine":
          return await addRoutine(args.uid, {
            name: args.name,
            memo: args.memo,
            exercises: args.exercises || []
          });
        default:
          return { ok: false, error: `Unknown tool: ${name}` };
  }
}

/** 오케스트레이션: tool_calls 루프 */
export async function coachChatOnce(client: OpenAI, model: string, payload: {
  uid: string; sessionId: string; userMessage: string;
}) {
  const messages: any[] = [
    { role: "system", content: SYSTEM_PROMPT + "\n" + DATA_MANIFEST },
    { role: "user", content: payload.userMessage }
  ];

  // 질문 로그
  await logChat(payload.uid, payload.sessionId, "user", payload.userMessage);

  for (let turn = 0; turn < 4; turn++) {
    // 1) LLM 호출
    const res = await client.chat.completions.create({
      model,
      // temperature: 0.7, // ← 온도 고정 모델이면 빼세요
      messages,
      tools: TOOL_SCHEMAS as any,
      tool_choice: "auto",
      // response_format: { type: "json_object" } // 모델에 따라 불가하면 제거
    });

    const msg: any = res.choices[0].message;

    // 2) 툴 호출이 있으면: (순서 중요)
    if (msg.tool_calls?.length) {
      // (A) 먼저 assistant 메시지(툴 호출 포함)를 그대로 넣는다.
      messages.push(msg);

        // (B) 각 tool_call에 대해 대응하는 tool 메시지를 추가한다.
        for (const call of msg.tool_calls) {
          const toolName = call.function?.name;
          const toolArgsFromLLM = JSON.parse(call.function?.arguments || "{}");

          // ✅ 서버에서 강제 주입: LLM이 뭘 보내든 무시하고 우리 값 사용
          const toolArgs = {
            ...toolArgsFromLLM,
            uid: payload.uid,
            sessionId: payload.sessionId,
          };

          const toolResult = await runTool(toolName, toolArgs);

          messages.push({
            role: "tool",
            tool_call_id: call.id,
            name: toolName,
            content: JSON.stringify(toolResult),
          });
        }

      // (C) 다음 턴으로 넘어가서 LLM이 툴 결과를 읽고 최종 답변 생성하도록 한다.
      continue;
    }

    // 3) 최종 답변(툴 호출 없음) → JSON 파싱 후 반환
    const raw = msg.content || "{}";
    let parsed: any;
    try { parsed = JSON.parse(raw); }
    catch { parsed = { reply: String(raw), highlights: [], actions: [] }; }

    await logChat(payload.uid, payload.sessionId, "assistant", parsed.reply ?? raw);
    return parsed;
  }

  // 4) 보호용 기본 응답
  return { reply: "답변이 길어져 간단 요약만 제공해요. 더 구체적으로 질문해 주세요.", highlights: [], actions: [] };
}
