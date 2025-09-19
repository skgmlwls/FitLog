// src/index.ts
import "./firebase";

import { onCall, HttpsError } from "firebase-functions/v2/https";
import { defineSecret } from "firebase-functions/params";

import { newOpenAI, coachChatOnce, SYSTEM_PROMPT, DATA_MANIFEST, pickModelFromSecrets, shouldEscalate } from "./openai";
import { assertAuth, assertParams } from "./utils/guard";
import { safe } from "./utils/safe";

import { admin } from "./firebase";
import { getExerciseTimeseries, getRecentStats } from "./tools/analytics";
import { detectRisk } from "./tools/risk";
import { planNextWeek } from "./tools/planner";
import { logChat } from "./tools/logger";
import { getDayRecordSummary, getRecentSessions, getCategoryBreakdown, getBestSet, getPRTrend } from "./tools/records";
import { listExerciseTypes, searchExerciseTypes } from "./tools/exerciseTypes";
import { listRoutines, getRoutineDetail, recommendRoutine, addRoutine } from "./tools/routines";

const OPENAI_API_KEY = defineSecret("OPENAI_API_KEY");
// 기존 기본 모델
const OPENAI_MODEL   = defineSecret("OPENAI_MODEL");
// ✅ 무거운 요청용(선택)
const OPENAI_MODEL_HEAVY = defineSecret("OPENAI_MODEL_HEAVY");
const REGION = "asia-northeast3";

/**
 * ✅ “타자 효과” 스트리밍:
 * 1) coachChatOnce로 툴/데이터까지 사용해 '최종 답' 생성
 * 2) RTDB에 30~50ms 간격으로 chunk append (타자 느낌)
 * 3) Firestore chatLogs에 최종 저장 + status=done
 */
export const chatWithCoachStream = onCall(
  {
    region: REGION,
    secrets: [OPENAI_API_KEY, OPENAI_MODEL, OPENAI_MODEL_HEAVY],
    timeoutSeconds: 300,
    memory: "1GiB",
  },
  async (req) =>
    safe(async () => {
      const { uid, sessionId, message, streamId } = req.data || {};
      assertParams(
        { uid, sessionId, message, streamId },
        ["uid", "sessionId", "message", "streamId"]
      );
      assertAuth(req, uid);

      const apiKey = OPENAI_API_KEY.value() || process.env.OPENAI_API_KEY;
      if (!apiKey)
        throw new HttpsError("failed-precondition", "OPENAI_API_KEY not set");

      const defaultSecret = OPENAI_MODEL.value() || process.env.OPENAI_MODEL;
      const heavySecret =
        OPENAI_MODEL_HEAVY.value() || process.env.OPENAI_MODEL_HEAVY;
      const heavy = shouldEscalate(String(message));
      const model = pickModelFromSecrets(
        defaultSecret,
        heavySecret,
        heavy ? "heavy" : "chat"
      );

      // RTDB 경로
      const rtdb = admin.database();
      const streamRef = rtdb.ref(
        `/chatStreams/${uid}/${sessionId}/${streamId}`
      );

      // 초기화
      const now = Date.now();
      await streamRef.set({
        status: "pending",
        createdAt: now,
        updatedAt: now,
        content: "",
        error: null,
        meta: null,
      });

      // 질문 로그
      await logChat(uid, sessionId, "user", String(message));

      // 안전 append(transaction)
      const appendContent = async (text: string) => {
        if (!text) return;
        await streamRef.child("content").transaction((prev) => {
          const prevStr = (typeof prev === "string" ? prev : "") as string;
          return prevStr + text;
        });
        await streamRef.update({ updatedAt: Date.now() });
      };

      const client = newOpenAI(apiKey);

      // ✅ 서버 사전 집계 — recommendRoutine으로 교체(상세 루틴/미싱 운동 확보)
      let stats: any = { data: {} };
      let risks: any = { risks: [] };
      let rec: any = {
        plan: null,
        draftForAddRoutine: null,
        missingExerciseTypes: [],
      };
      try {
        [stats, risks, rec] = await Promise.all([
          getRecentStats(uid, 4),
          detectRisk(uid),
          recommendRoutine(uid, []), // ← 핵심 변경
        ]);
      } catch (e) {
        console.warn("[tools-prefetch failed]", e);
      }

      // ✅ meta: recommend_routine / add_routine 액션을 올바른 형태로
      const actions: Array<{ type: string; args?: Record<string, any> }> = [
        { type: "recommend_routine", args: { focusTargets: [] } },
      ];
      if (rec?.draftForAddRoutine) {
        actions.push({
          type: "add_routine",
          args: { ...rec.draftForAddRoutine },
        });
      }

      const highlights = [
        stats?.data
          ? `최근 ${stats.data?.sessionCount ?? 0}회 / 세트 ${
              stats.data?.totalSets ?? 0
            } / 볼륨 ${stats.data?.totalVolume ?? 0}kg`
          : "최근 기록 요약을 불러왔습니다.",
      ];
      await streamRef.child("meta").set({ highlights, actions });

      // ✅ 시스템 프롬프트: 서버 추천 루틴을 참고용으로 주입
      const systemPrompt =
        SYSTEM_PROMPT +
        "\n" +
        DATA_MANIFEST +
        "\n\n[SERVER-PREFETCHED]\n" +
        `- recentStats: ${JSON.stringify(stats?.data || {})}\n` +
        `- risks: ${JSON.stringify(risks?.risks || [])}\n` +
        `- serverRecommendedRoutine: ${JSON.stringify({
          plan: rec?.plan || null,
          missingExerciseTypes: rec?.missingExerciseTypes || [],
        })}\n`;

      // “텍스트만” 가드
      const messages: any[] = [
        { role: "system", content: systemPrompt },
        {
          role: "system",
          content:
            "지금부터 출력은 사용자에게 보여줄 한국어 텍스트만 작성합니다. JSON/코드블록/키:값 형태 금지. 불릿·숫자목록은 허용.",
        },
        { role: "user", content: String(message) },
      ];

      await streamRef.update({ status: "streaming", updatedAt: Date.now() });

      let finalText = "";
      let usedStreaming = false;

      try {
        // 스트리밍
        const stream = await client.chat.completions.create({
          model,
          stream: true,
          messages,
        });
        usedStreaming = true;

        let buffer = "";
        let lastFlush = Date.now();
        const DEBOUNCE_MS = 200;

        for await (const part of stream) {
          const delta = part.choices?.[0]?.delta?.content || "";
          if (!delta) continue;

          finalText += delta;
          buffer += delta;

          const t = Date.now();
          if (t - lastFlush >= DEBOUNCE_MS) {
            await appendContent(buffer);
            buffer = "";
            lastFlush = t;
          }
        }
        if (buffer) await appendContent(buffer);
      } catch (e: any) {
        // 폴백(one-shot) + 타자효과
        console.warn("[streaming failed → fallback typing]", e?.message || e);

        const oneShot = await coachChatOnce(client, model, {
          uid,
          sessionId,
          userMessage: String(message),
        });

        const parseSafe = (x: any) => {
          if (!x) return null;
          if (typeof x === "object") return x;
          if (typeof x === "string") {
            try {
              return JSON.parse(x);
            } catch {
              return null;
            }
          }
          return null;
        };
        const parsed = parseSafe(oneShot) || oneShot;

        const replyText =
          parsed && typeof parsed === "object" && parsed.reply
            ? String(parsed.reply)
            : typeof oneShot === "string"
            ? oneShot
            : "";
        finalText = replyText || "응답 생성 실패";

        // 폴백 meta 유지/보강
        const highlights2 =
          (parsed && parsed.highlights) || highlights || [];
        const actions2 = (parsed && parsed.actions) || actions || [];
        await streamRef.child("meta").set({ highlights: highlights2, actions: actions2 });

        // 타자연출
        const STEP = 36;
        const DELAY = 35;
        for (let i = 0; i < finalText.length; i += STEP) {
          const chunk = finalText.slice(i, i + STEP);
          await appendContent(chunk);
          await new Promise((r) => setTimeout(r, DELAY));
        }
      }

      // ✅ CTA 폴백: 모델이 CTA를 누락했을 경우 서버가 문장 덧붙임
      try {
        const hasCTA = /저장할까요|추가.*저장할까요/.test(finalText);
        if (!hasCTA) {
          const missing = Array.isArray(rec?.missingExerciseTypes)
            ? rec.missingExerciseTypes
            : [];
          const cta =
            missing.length > 0
              ? `\n\n**다음 단계**\n- exerciseType에 없는 운동: ${missing.join(
                  ", "
                )}\n- 이 운동들을 exerciseType에 추가하고 루틴을 저장할까요?`
              : `\n\n**다음 단계**\n- 위 루틴을 이름 "다음 주 추천 루틴(4일)"로 저장할까요?`;
          await appendContent(cta);
          finalText += cta;
        }
      } catch {}

      // 로그 + 종료
      await logChat(uid, sessionId, "assistant", finalText || "(빈 응답)");

      await streamRef.update({
        status: "done",
        updatedAt: Date.now(),
        usage: { model, inputTokens: null, outputTokens: null },
        _debug: { usedStreaming },
      });

      return { ok: true };
    })
);



/** === 기존 callable 유지 (약간의 가드 추가는 선택) === */
export const get_exercise_timeseries = onCall({ region: REGION }, async (req) => safe(async () => {
  const { uid, exerciseName, sinceDays } = req.data || {};
  assertParams({ uid, exerciseName, sinceDays }, ["uid", "exerciseName", "sinceDays"]);
  assertAuth(req, uid);
  const days = Math.max(1, Math.min(Number(sinceDays) || 30, 180));
  return getExerciseTimeseries(uid, String(exerciseName), days);
}));

export const get_recent_stats = onCall({ region: REGION }, async (req) => safe(async () => {
  const { uid, weeks } = req.data || {};
  assertParams({ uid }, ["uid"]);
  assertAuth(req, uid);
  const w = Number.isFinite(Number(weeks)) ? Number(weeks) : 4;
  const safeWeeks = Math.max(1, Math.min(w, 12));
  return getRecentStats(uid, safeWeeks);
}));

export const detect_risk = onCall({ region: REGION }, async (req) => safe(async () => {
  const { uid } = req.data || {};
  assertParams({ uid }, ["uid"]);
  assertAuth(req, uid);
  return detectRisk(uid);
}));

export const plan_next_week = onCall({ region: REGION }, async (req) => safe(async () => {
  const { uid, focusTargets } = req.data || {};
  assertParams({ uid }, ["uid"]);
  assertAuth(req, uid);
  const targets = Array.isArray(focusTargets) ? focusTargets.map(String).slice(0, 5) : [];
  return planNextWeek(uid, targets);
}));

export const log_chat = onCall({ region: REGION }, async (req) => safe(async () => {
  const { uid, sessionId, role, content } = req.data || {};
  assertParams({ uid, sessionId, role, content }, ["uid", "sessionId", "role", "content"]);
  assertAuth(req, uid);
  const r = String(role);
  if (!["user", "assistant", "tool"].includes(r)) {
    throw new HttpsError("invalid-argument", "role must be one of user|assistant|tool");
  }
  const body = String(content).slice(0, 10_000);
  return logChat(uid, String(sessionId), r as "user"|"assistant"|"tool", body);
}));

export const get_day_record_summary = onCall({ region: REGION }, async (req) => safe(async () => {
  const { uid, recordId } = req.data || {};
  assertParams({ uid, recordId }, ["uid", "recordId"]);
  assertAuth(req, uid);
  return getDayRecordSummary(uid, String(recordId));
}));

export const get_recent_sessions = onCall({ region: REGION }, async (req) => safe(async () => {
  const { uid, limit } = req.data || {};
  assertParams({ uid }, ["uid"]);
  assertAuth(req, uid);
  return getRecentSessions(uid, Number(limit) || 10);
}));

export const get_category_breakdown = onCall({ region: REGION }, async (req) => safe(async () => {
  const { uid, weeks } = req.data || {};
  assertParams({ uid }, ["uid"]);
  assertAuth(req, uid);
  return getCategoryBreakdown(uid, Number(weeks) || 4);
}));

export const get_best_set = onCall({ region: REGION }, async (req) => safe(async () => {
  const { uid, exerciseName, sinceDays } = req.data || {};
  assertParams({ uid, exerciseName }, ["uid", "exerciseName"]);
  assertAuth(req, uid);
  return getBestSet(uid, String(exerciseName), Number(sinceDays) || 90);
}));

export const get_pr_trend = onCall({ region: REGION }, async (req) => safe(async () => {
  const { uid, exerciseName, sinceDays } = req.data || {};
  assertParams({ uid, exerciseName }, ["uid", "exerciseName"]);
  assertAuth(req, uid);
  return getPRTrend(uid, String(exerciseName), Number(sinceDays) || 120);
}));

export const list_exercise_types = onCall({ region: REGION }, async (req) => safe(async () => {
  const { uid, category } = req.data || {};
  assertParams({ uid }, ["uid"]);
  assertAuth(req, uid);
  return listExerciseTypes(uid, category ? String(category) : undefined);
}));

export const search_exercise_types = onCall({ region: REGION }, async (req) => safe(async () => {
  const { uid, keyword } = req.data || {};
  assertParams({ uid, keyword }, ["uid", "keyword"]);
  assertAuth(req, uid);
  return searchExerciseTypes(uid, String(keyword));
}));

export const list_routines = onCall({ region: REGION }, async (req) => safe(async () => {
  const { uid } = req.data || {};
  assertParams({ uid }, ["uid"]);
  assertAuth(req, uid);
  return listRoutines(uid);
}));

export const get_routine_detail = onCall({ region: REGION }, async (req) => safe(async () => {
  const { uid, routineId } = req.data || {};
  assertParams({ uid, routineId }, ["uid", "routineId"]);
  assertAuth(req, uid);
  return getRoutineDetail(uid, String(routineId));
}));

export const recommend_routine = onCall({ region: REGION }, async (req) => safe(async () => {
  const { uid, focusTargets } = req.data || {};
  assertParams({ uid }, ["uid"]);
  assertAuth(req, uid);
  return recommendRoutine(uid, Array.isArray(focusTargets) ? focusTargets.map(String) : []);
}));

export const add_routine = onCall({ region: REGION }, async (req) => safe(async () => {
  const { uid, name, memo, exercises } = req.data || {};
  assertParams({ uid, name, exercises }, ["uid", "name", "exercises"]);
  assertAuth(req, uid);
  return addRoutine(uid, { name: String(name), memo: String(memo || ""), exercises });
}));
