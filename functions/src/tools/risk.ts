// src/tools/risk.ts
import { db } from "../firebase";

/** KST(Asia/Seoul) 기준 YYYY-MM-DD */
function ymdKSTDaysAgo(daysAgo: number): string {
  const now = Date.now();
  const kstMidnight = new Date(now + 9 * 3600_000);
  kstMidnight.setUTCHours(0, 0, 0, 0);
  kstMidnight.setUTCDate(kstMidnight.getUTCDate() - daysAgo);
  return kstMidnight.toISOString().slice(0, 10);
}

export async function detectRisk(uid: string) {
  const sinceStr = ymdKSTDaysAgo(14);
  // console.log("[detectRisk]", { uid, sinceStr });

  const recSnap = await db
    .collection(`users/${uid}/exerciseRecords`)
    .where("date", ">=", sinceStr)
    .orderBy("date", "desc")
    .get();

  const risks: string[] = [];
  let consecutiveHigh = 0;

  const DAY = 24 * 60 * 60 * 1000;
  const last7 = Date.now() - 7 * DAY;
  const daysActive = new Set<string>();

  const vol7A: Record<string, number> = {};
  const vol7B: Record<string, number> = {};

  recSnap.forEach((rec: any) => {
    const r = rec.data();
    const created = (r.createdAt as number) || 0;
    const intensity = String(r.intensity || "").toUpperCase();

    if (intensity === "HIGH" || intensity === "VERY_HIGH") {
      consecutiveHigh += 1;
      if (consecutiveHigh >= 3 && !risks.includes("고RPE 세션 3회 이상 연속")) {
        risks.push("고RPE 세션 3회 이상 연속");
      }
    } else {
      consecutiveHigh = 0;
    }

    const dayKey = new Date(String(r.date) + "T00:00:00").toISOString().slice(0, 10);
    if (created >= last7) daysActive.add(dayKey);

    const bucket = created >= Date.now() - 7 * DAY ? vol7A : vol7B;
    const vbc = (r.volumeByCategory || {}) as Record<string, number>;
    for (const [cat, vol] of Object.entries(vbc)) {
      bucket[cat] = (bucket[cat] || 0) + (vol || 0);
    }
  });

  if (daysActive.size >= 6) risks.push("최근 7일 중 6일 이상 운동(휴식 부족 가능)");

  for (const cat of new Set([...Object.keys(vol7A), ...Object.keys(vol7B)])) {
    const a = vol7A[cat] || 0;
    const b = vol7B[cat] || 0;
    if (b > 0 && a > b * 1.4 && a > 3000) {
      risks.push(`"${cat}" 볼륨 최근 1주 급증(+40%↑)`);
    }
  }

  return { ok: true, risks };
}
