// src/tools/records.ts
import { db } from "../firebase";
import { isoWeekStart } from "../utils/date";

function ymdKSTDaysAgo(daysAgo: number): string {
  const now = Date.now();
  const kst = new Date(now + 9 * 3600_000);
  kst.setUTCHours(0, 0, 0, 0);
  kst.setUTCDate(kst.getUTCDate() - daysAgo);
  return kst.toISOString().slice(0, 10);
}

/** get_day_record_summary: 하루 기록 요약(세트/볼륨/운동 목록) */
export async function getDayRecordSummary(uid: string, recordId: string) {
  const recRef = db.collection(`users/${uid}/exerciseRecords`).doc(recordId);
  const recSnap = await recRef.get();
  if (!recSnap.exists) return { ok: false, error: "record not found" };

  const rec = recSnap.data()!;
  const exSnap = await recRef.collection("exercises").orderBy("order").get();

  let totalSets = 0;
  let totalVolume = 0;
  const exercises: Array<{
    itemId: string;
    name: string;
    category: string;
    setCount: number;
    volume: number;
  }> = [];

  for (const ex of exSnap.docs) {
    const e = ex.data();
    const sets = await ex.ref.collection("sets").get();

    let vol = 0;
    sets.forEach(s => {
      const d = s.data();
      vol += (Number(d.reps) || 0) * (Number(d.weight) || 0);
    });

    totalSets += (Number(e.setCount) || sets.size || 0);
    totalVolume += vol;

    exercises.push({
      itemId: e.itemId,
      name: e.exerciseName,
      category: e.exerciseCategory,
      setCount: Number(e.setCount) || sets.size || 0,
      volume: vol
    });
  }

  return {
    ok: true,
    data: {
      recordId: rec.recordId,
      date: rec.date,
      memo: rec.memo || "",
      intensity: rec.intensity || "NORMAL",
      totalSets,
      totalVolume,
      exercises
    }
  };
}

/** get_recent_sessions: 최근 N회 간단 목록(날짜/메모/카테고리 볼륨 요약) */
export async function getRecentSessions(uid: string, limit: number = 10) {
  const recSnap = await db
    .collection(`users/${uid}/exerciseRecords`)
    .orderBy("date", "desc")
    .limit(Math.max(1, Math.min(limit, 50)))
    .get();

  const rows = recSnap.docs.map(d => {
    const r = d.data() as any;
    return {
      recordId: r.recordId,
      date: r.date,
      memo: r.memo || "",
      intensity: r.intensity || "NORMAL",
      volumeByCategory: r.volumeByCategory || {},
      exerciseCount: r.exerciseCount || 0
    };
  });

  return { ok: true, data: rows };
}

/** get_category_breakdown: 최근 W주 카테고리 비중(%) + 절대볼륨 */
export async function getCategoryBreakdown(uid: string, weeks: number = 4) {
  const since = ymdKSTDaysAgo(Math.max(1, Math.min(weeks, 26)) * 7);
  const snap = await db
    .collection(`users/${uid}/exerciseRecords`)
    .where("date", ">=", since)
    .orderBy("date")
    .get();

  const vbc: Record<string, number> = {};
  let total = 0;

  snap.forEach(doc => {
    const r = doc.data() as any;
    const map = (r.volumeByCategory || {}) as Record<string, number>;
    for (const [k, v] of Object.entries(map)) {
      const num = Number(v) || 0;
      vbc[k] = (vbc[k] || 0) + num;
      total += num;
    }
  });

  const rows = Object.entries(vbc).map(([cat, vol]) => ({
    category: cat,
    volume: vol,
    sharePct: total ? Math.round((vol / total) * 100) : 0
  })).sort((a, b) => b.volume - a.volume);

  return { ok: true, data: { totalVolume: total, breakdown: rows } };
}

/** get_best_set: 특정 운동 최대 중량/반복(기간 내) */
export async function getBestSet(
  uid: string,
  exerciseName: string,
  sinceDays: number = 90
) {
  const since = ymdKSTDaysAgo(Math.max(1, Math.min(sinceDays, 365)));
  const recSnap = await db
    .collection(`users/${uid}/exerciseRecords`)
    .where("date", ">=", since)
    .orderBy("date", "desc")
    .get();

  let best = { weight: 0, reps: 0, date: "", recordId: "" };

  for (const rec of recSnap.docs) {
    const r = rec.data();
    const exSnap = await rec.ref
      .collection("exercises")
      .where("exerciseName", "==", exerciseName)
      .get();

    for (const ex of exSnap.docs) {
      const sets = await ex.ref.collection("sets").get();
      sets.forEach(s => {
        const d = s.data();
        const w = Number(d.weight) || 0;
        const reps = Number(d.reps) || 0;
        if (w > best.weight || (w === best.weight && reps > best.reps)) {
          best = { weight: w, reps, date: r.date, recordId: r.recordId };
        }
      });
    }
  }

  return { ok: true, data: best };
}

/** get_pr_trend: 특정 운동 주차별 est.1RM 추세(기간 내) */
export async function getPRTrend(
  uid: string,
  exerciseName: string,
  sinceDays: number = 120
) {
  const since = ymdKSTDaysAgo(Math.max(7, Math.min(sinceDays, 720)));
  const recSnap = await db
    .collection(`users/${uid}/exerciseRecords`)
    .where("date", ">=", since)
    .orderBy("date")
    .get();

  const weekAgg: Record<string, { bestEst1RM: number }> = {};

  for (const rec of recSnap.docs) {
    const r = rec.data() as any;
    const exSnap = await rec.ref
      .collection("exercises")
      .where("exerciseName", "==", exerciseName)
      .get();

    let weekBest = 0;
    for (const ex of exSnap.docs) {
      const sets = await ex.ref.collection("sets").get();
      sets.forEach(s => {
        const d = s.data() as any;
        const w = Number(d.weight) || 0;
        const reps = Number(d.reps) || 0;
        // 간단 Epley: 1RM = w * (1 + reps/30)
        const est = w * (1 + reps / 30);
        if (est > weekBest) weekBest = est;
      });
    }

    if (weekBest > 0) {
      const wk = isoWeekStart(String(r.date));
      if (!weekAgg[wk] || weekBest > weekAgg[wk].bestEst1RM) {
        weekAgg[wk] = { bestEst1RM: weekBest };
      }
    }
  }

  const trend = Object.entries(weekAgg)
    .map(([weekStart, v]) => ({ weekStart, bestEst1RM: Number(v.bestEst1RM.toFixed(1)) }))
    .sort((a, b) => a.weekStart.localeCompare(b.weekStart));

  return { ok: true, data: trend };
}
