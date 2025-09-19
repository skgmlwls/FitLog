// src/tools/routines.ts
import { db } from "../firebase";
import { getRecentStats } from "./analytics";

/** 유효 카테고리 */
const VALID_CATEGORIES = new Set(["가슴","등","어깨","하체","팔","복부","기타"]);
const sanitizeCategory = (c: string) => VALID_CATEGORIES.has(c) ? c : "기타";

/** YYYY-MM-DD(KST) */
function ymdKSTDaysAgo(daysAgo: number): string {
  const now = Date.now();
  const kstMidnight = new Date(now + 9 * 3600_000);
  kstMidnight.setUTCHours(0, 0, 0, 0);
  kstMidnight.setUTCDate(kstMidnight.getUTCDate() - daysAgo);
  return kstMidnight.toISOString().slice(0, 10);
}

const roundTo = (x: number, step = 2.5) => Math.round(x / step) * step;

/** 최근 기록에서 특정 운동의 마지막 작업 무게 추정 (가장 최근 세션의 최고 중량) */
async function getLastWorkingWeight(uid: string, exerciseName: string, lookbackDays = 180) {
  const sinceStr = ymdKSTDaysAgo(lookbackDays);
  const recSnap = await db
    .collection(`users/${uid}/exerciseRecords`)
    .where("date", ">=", sinceStr)
    .orderBy("date", "desc")
    .limit(60) // 최근 60일치 세션 안에서 탐색
    .get();

  for (const rec of recSnap.docs) {
    const exSnap = await rec.ref
      .collection("exercises")
      .where("exerciseName", "==", exerciseName)
      .limit(1)
      .get();
    if (exSnap.empty) continue;

    const ex = exSnap.docs[0];
    const setsSnap = await ex.ref.collection("sets").orderBy("setNumber", "desc").get();
    let top = 0;
    let topReps = 0;
    setsSnap.forEach(s => {
      const d: any = s.data();
      if ((d.weight || 0) > top) {
        top = d.weight || 0;
        topReps = d.reps || 0;
      }
    });
    if (top > 0) return { weight: top, reps: topReps };
  }
  return null;
}

/** 내 exerciseType 이름 맵(lowercase) */
async function getExistingExerciseTypeMap(uid: string) {
  const snap = await db.collection(`users/${uid}/exerciseType`).get();
  const map = new Map<string, { id: string; name: string; category: string }>();
  snap.forEach(d => {
    const x = d.data() as any;
    const key = String(x.name || "").trim().toLowerCase();
    if (!key) return;
    map.set(key, { id: x.id, name: x.name, category: String(x.category || "") });
  });
  return map;
}

/** 기본 운동 풀(카테고리 → 후보 리스트) */
const EX_POOL: Record<string, string[]> = {
  가슴: ["벤치 프레스", "인클라인 덤벨 프레스", "체스트 프레스", "딥스", "케이블 플라이"],
  등:   ["랫풀다운", "바벨 로우", "시티드 로우", "풀업", "원암 덤벨 로우"],
  어깨: ["오버헤드 프레스", "레터럴 레이즈", "리어델트 플라이", "프론트 레이즈"],
  하체: ["백 스쿼트", "레그 프레스", "루마니안 데드리프트", "레그 컬", "레그 익스텐션", "카프 레이즈"],
  팔:   ["바벨 컬", "덤벨 컬", "케이블 푸시다운", "오버헤드 트라이셉 익스텐션", "해머 컬"],
  복부: ["크런치", "레그 레이즈", "케이블 크런치", "행잉 레그 레이즈"],
  기타: ["페이스 풀", "풀오버"]
};

function pickN(source: string[], n: number, used = new Set<string>()) {
  // 중복 최소화를 위해 아직 안 쓴 것 우선
  const fresh = source.filter(s => !used.has(s));
  const pool = fresh.length >= n ? fresh : source;
  const res: string[] = [];
  for (let i = 0; i < n && pool.length; i++) {
    const idx = i % pool.length;
    res.push(pool[idx]);
    used.add(pool[idx]);
  }
  return res;
}

/** 세트 스킴(간단): 대근육 8~10, 보조 10~12, 복부 15~20 */
function repScheme(category: string, name: string) {
  if (category === "복부") return { reps: 15, sets: 3 };
  const big = ["벤치 프레스","바벨 로우","백 스쿼트","오버헤드 프레스","루마니안 데드리프트","풀업","딥스"];
  if (big.includes(name)) return { reps: 8, sets: 3 };
  return { reps: 10, sets: 3 };
}

/** 이름 → 카테고리(풀에서 역추론) 없으면 기타 */
function inferCategoryFromPool(name: string): string {
  for (const [cat, arr] of Object.entries(EX_POOL)) {
    if (arr.includes(name)) return cat;
  }
  return "기타";
}

/** --- 기존 목록/조회 유지 --- */

export async function listRoutines(uid: string) {
  const snap = await db.collection(`users/${uid}/routines`).orderBy("createdAt", "desc").get();
  const rows = snap.docs.map(d => {
    const x = d.data() as any;
    return {
      routineId: x.routineId,
      name: x.name,
      memo: x.memo || "",
      exerciseCount: x.exerciseCount || 0,
      createdAt: x.createdAt || 0
    };
  });
  return { ok: true, data: rows };
}

export async function getRoutineDetail(uid: string, routineId: string) {
  const ref = db.collection(`users/${uid}/routines`).doc(routineId);
  const doc = await ref.get();
  if (!doc.exists) return { ok: false, error: "routine not found" };

  const header = doc.data()!;
  const exSnap = await ref.collection("exercises").orderBy("order").get();

  const exercises: any[] = [];
  for (const ex of exSnap.docs) {
    const e = ex.data();
    const setsSnap = await ex.ref.collection("sets").orderBy("setNumber").get();
    const sets = setsSnap.docs.map(s => s.data());
    exercises.push({
      itemId: e.itemId,
      exerciseName: e.exerciseName,
      exerciseCategory: e.exerciseCategory,
      setCount: e.setCount || sets.length,
      memo: e.exerciseMemo || "",
      sets
    });
  }

  return { ok: true, data: { ...header, exercises } };
}

/**
 * ✅ 루틴 추천(상세): 최근 4주 편향/부족부위 반영 + 운동풀에서 선택
 * - days: Upper / Lower / Pull / Push (4일)
 * - 각 운동: { exerciseName, exerciseCategory, order, setCount, sets[{setNumber,reps,weight}] }
 * - weight: 최근 작업 무게가 있으면 80~90%로 권장, 없으면 0
 * - missingExerciseTypes: exerciseType에 없는 운동 이름 목록(lowercase 비교)
 * - nextStep: 사용자 CTA 문구
 * - draftForAddRoutine: 즉시 addRoutine에 넘길 payload
 */
export async function recommendRoutine(uid: string, focusTargets: string[] = []) {
  const stats = await getRecentStats(uid, 4);
  const vbc = stats.data?.volumeByCategory || {};
  const total = stats.data?.totalVolume || 0;

  const high: string[] = [];
  const low: string[] = [];
  for (const [cat, vol] of Object.entries(vbc)) {
    const share = total ? (Number(vol) / total) * 100 : 0;
    if (share >= 35) high.push(cat);
    if (share <= 10) low.push(cat);
  }

  // 4일 분할 템플릿
  const daysTemplate = [
    { day: 1, name: "Upper", spec: { "가슴": 2, "등": 2, "어깨": 1, "팔": 1 } },
    { day: 2, name: "Lower", spec: { "하체": 4, "복부": 2 } },
    { day: 3, name: "Pull",  spec: { "등": 3, "팔": 2 } },
    { day: 4, name: "Push",  spec: { "가슴": 3, "어깨": 2, "팔": 1 } },
  ];

  // focusTargets 반영: 부족 부위(low) + 사용자가 고른 부위 우선권
  const focus = new Set<string>([...focusTargets.map(sanitizeCategory), ...low.map(sanitizeCategory)]);
  const used = new Set<string>();

  // 운동 선택
  type DraftExercise = {
    exerciseName: string;
    exerciseCategory: string;
    order: number;
    setCount: number;
    exerciseMemo?: string;
    sets: Array<{ setNumber: number; reps: number; weight: number }>;
  };

  const allChosenNames: string[] = [];
  const days: Array<{ day: number; name: string; exercises: DraftExercise[] }> = [];

  for (const tpl of daysTemplate) {
    const exercises: DraftExercise[] = [];
    let order = 0;

    // spec 내에서 focus 카테고리 먼저 채택
    const cats = Object.entries(tpl.spec)
      .sort(([aCat], [bCat]) => (focus.has(aCat) === focus.has(bCat)) ? 0 : (focus.has(aCat) ? -1 : 1));

    for (const [cat, cnt] of cats) {
      const pool = EX_POOL[cat] || [];
      const pick = pickN(pool, cnt, used);
      for (const name of pick) {
        order += 1;
        const category = sanitizeCategory(cat || inferCategoryFromPool(name));
        const { reps, sets } = repScheme(category, name);

        // 최근 무게 추정 (있으면 0.8~0.9 배 적용)
        // 대근육(8렙)은 0.9, 그 외 0.85
        let suggest = 0;
        const last = await getLastWorkingWeight(uid, name).catch(() => null);
        if (last && last.weight > 0) {
          const ratio = reps <= 8 ? 0.9 : 0.85;
          suggest = roundTo(last.weight * ratio, 2.5);
        }

        const setArr = Array.from({ length: sets }, (_, i) => ({
          setNumber: i + 1,
          reps,
          weight: suggest
        }));

        exercises.push({
          exerciseName: name,
          exerciseCategory: category,
          order,
          setCount: sets,
          sets: setArr
        });
        allChosenNames.push(name);
      }
    }
    days.push({ day: tpl.day, name: tpl.name, exercises });
  }

  // exerciseType 존재 여부 확인
  const typeMap = await getExistingExerciseTypeMap(uid);
  const missing = Array.from(new Set(
    allChosenNames
      .map(n => n.trim().toLowerCase())
      .filter(k => !typeMap.has(k))
  ));

  // 노트(편향 반영)
  const notes: string[] = [];
  if (focusTargets.length) notes.push(`포커스: ${focusTargets.join(", ")}`);
  if (high.length) notes.push(`비중 높은 부위(감량 권장): ${high.join(", ")}`);
  if (low.length) notes.push(`비중 낮은 부위(보강 권장): ${low.join(", ")}`);
  notes.push("권장 RPE 7~8, 마지막 1–2세트는 -2.5kg 감해 품질 유지");

  // addRoutine에 바로 넣을 초안 payload
  const draftForAddRoutine = {
    name: "다음 주 추천 루틴(4일)",
    memo: notes.join(" / "),
    exercises: days.flatMap(d => d.exercises).map(e => ({
      exerciseName: e.exerciseName,
      exerciseCategory: e.exerciseCategory,
      order: e.order,
      setCount: e.setCount,
      exerciseMemo: e.exerciseMemo || "",
      sets: e.sets
    }))
  };

  const nextStep = !missing.length
    ? "바로 루틴을 저장할까요?"
    : `exerciseType에 없는 운동: ${missing.join(", ")}. 이 운동들을 exerciseType에 추가하고, 추천 루틴을 저장할까요?`;

  return {
    ok: true,
    plan: {
      name: "다음 주 추천 루틴(4일)",
      weeklyFrequency: 4,
      days,
      notes
    },
    exercisePool: EX_POOL,             // 참고용(클라에서 미리보기 가능)
    missingExerciseTypes: missing,     // 없는 운동 이름(lowercase 기준)
    nextStep,                          // CTA 문구
    draftForAddRoutine                 // addRoutine 호출용 준비 데이터
  };
}

/** 루틴 추가: routines/{routineId} + exercises + sets */
export async function addRoutine(uid: string, payload: {
  name: string;
  memo?: string;
  exercises: Array<{
    exerciseName: string;
    exerciseCategory: string;
    exerciseTypeId?: string;
    order: number;
    setCount: number;
    sets?: Array<{ setNumber: number; reps: number; weight: number }>;
    exerciseMemo?: string;
  }>;
}) {
  const routineId = db.collection(`users/${uid}/routines`).doc().id;
  const ref = db.collection(`users/${uid}/routines`).doc(routineId);

  await ref.set({
    routineId,
    name: payload.name,
    memo: payload.memo || "",
    exerciseCount: payload.exercises.length,
    createdAt: Date.now()
  });

  for (const ex of payload.exercises) {
    const itemId = db.collection("tmp").doc().id;
    const exRef = ref.collection("exercises").doc(itemId);
    await exRef.set({
      itemId,
      createdAt: Date.now(),
      exerciseName: ex.exerciseName,
      exerciseCategory: sanitizeCategory(ex.exerciseCategory),
      exerciseTypeId: ex.exerciseTypeId || "",
      order: ex.order,
      setCount: Math.max(1, Math.min(Number(ex.setCount) || 3, 8)),
      exerciseMemo: ex.exerciseMemo || ""
    });

    const sets = ex.sets && ex.sets.length
      ? ex.sets
      : Array.from({ length: Math.max(1, Math.min(Number(ex.setCount) || 3, 8)) }, (_, i) => ({ setNumber: i + 1, reps: 10, weight: 0 }));

    for (const s of sets) {
      const setId = db.collection("tmp").doc().id;
      await exRef.collection("sets").doc(setId).set({
        setId,
        createdAt: Date.now(),
        setNumber: s.setNumber,
        reps: s.reps,
        weight: s.weight
      });
    }
  }

  return { ok: true, routineId };
}
