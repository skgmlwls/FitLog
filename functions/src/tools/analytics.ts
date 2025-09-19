// src/tools/analytics.ts
import { db } from "../firebase";
import { isoWeekStart } from "../utils/date";
import { WeekAgg, RecentStats } from "../utils/types";

/** KST(Asia/Seoul) 기준 YYYY-MM-DD */
function ymdKSTDaysAgo(daysAgo: number): string {
  const now = Date.now();
  const kstMidnight = new Date(now + 9 * 3600_000);
  kstMidnight.setUTCHours(0, 0, 0, 0);
  kstMidnight.setUTCDate(kstMidnight.getUTCDate() - daysAgo);
  return kstMidnight.toISOString().slice(0, 10);
}

/** 1) 특정 운동 타임시리즈(최근 N일) → 주차 집계  */
export async function getExerciseTimeseries(
  uid: string,
  exerciseName: string,
  sinceDays: number
) {
  const sinceStr = ymdKSTDaysAgo(sinceDays);
  // 디버그 로그(원하면 남겨두세요)
  // console.log("[getExerciseTimeseries]", { uid, exerciseName, sinceStr });

  const recSnap = await db
    .collection(`users/${uid}/exerciseRecords`)
    .where("date", ">=", sinceStr)
    .orderBy("date")
    .get();

  const weekAgg: Record<
    string,
    { totalVolume: number; totalSets: number; totalReps: number; topSetWeight: number }
  > = {};

  for (const rec of recSnap.docs) {
    const recData = rec.data();
    const exSnap = await rec.ref
      .collection("exercises")
      .where("exerciseName", "==", exerciseName)
      .get();

    for (const ex of exSnap.docs) {
      const sets = await ex.ref.collection("sets").get();

      let totalVol = 0;
      let totalSets = 0;
      let totalReps = 0;
      let topSet = 0;

      sets.forEach((s: any) => {
        const d = s.data();
        const vol = (d.reps || 0) * (d.weight || 0);
        totalVol += vol;
        totalReps += d.reps || 0;
        totalSets += 1;
        if ((d.weight || 0) > topSet) topSet = d.weight;
      });

      const weekKey = isoWeekStart(recData.date); // "YYYY-MM-DD"
      if (!weekAgg[weekKey]) {
        weekAgg[weekKey] = {
          totalVolume: 0,
          totalSets: 0,
          totalReps: 0,
          topSetWeight: 0,
        };
      }
      weekAgg[weekKey].totalVolume += totalVol;
      weekAgg[weekKey].totalSets += totalSets;
      weekAgg[weekKey].totalReps += totalReps;
      if (topSet > weekAgg[weekKey].topSetWeight) weekAgg[weekKey].topSetWeight = topSet;
    }
  }

  const data: WeekAgg[] = Object.entries(weekAgg)
    .map(([weekStart, v]) => ({
      weekStart,
      ...v,
      est1RMMax: v.topSetWeight * (1 + v.totalReps / 30),
    }))
    .sort((a, b) => a.weekStart.localeCompare(b.weekStart));

  return { ok: true, data };
}

/** 2) 최근 W주 요약  */
export async function getRecentStats(uid: string, weeks = 4) {
  const sinceStr = ymdKSTDaysAgo(weeks * 7);
  console.log("[getRecentStats]", { uid, sinceStr });

  const recSnap = await db
    .collection(`users/${uid}/exerciseRecords`)
    .where("date", ">=", sinceStr)
    .orderBy("date")
    .get();

  console.log("[getRecentStats] recSnap.size=", recSnap.size);
  const first = recSnap.docs[0]?.data();
  if (first) console.log("[getRecentStats] first.date=", first.date, "first.createdAt=", first.createdAt);

  let sessionCount = 0;
  let totalVolume = 0;
  const volumeByCategory: Record<string, number> = {};
  let totalSets = 0;

  for (const rec of recSnap.docs) {
    const r = rec.data();
    sessionCount += 1;

    const vbc = (r.volumeByCategory || {}) as Record<string, number>;
    for (const [cat, vol] of Object.entries(vbc)) {
      const v = Number(vol) || 0;
      volumeByCategory[cat] = (volumeByCategory[cat] || 0) + v;
      totalVolume += v;
    }

    const exSnap = await rec.ref.collection("exercises").get();
    exSnap.forEach((ex: any) => {
      const d = ex.data();
      totalSets += d.setCount || 0;
    });
  }

  const data: RecentStats = {
    sessionCount,
    avgSessionPerWeek: weeks > 0 ? sessionCount / weeks : 0,
    totalSets,
    totalVolume,
    volumeByCategory,
  };

  return { ok: true, data };
}
