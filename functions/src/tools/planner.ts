import { getRecentStats } from "./analytics";

/** 4) 다음 주 루틴 제안  */
export async function planNextWeek(uid: string, focusTargets: string[] = []) {
  // 최근 4주 통계를 참고해 기본 플랜 조정
  const stats = await getRecentStats(uid, 4);
  const vol = stats.data.volumeByCategory || {};
  const totalVol = stats.data.totalVolume || 0;

  // 편향이 큰 부위(>35%)는 유지/감소, 부족 부위(<10%)는 보강
  const notes: string[] = [];
  Object.entries(vol as Record<string, number>).forEach(([cat, v]) => {
    const share = totalVol ? Math.round((Number(v) / totalVol) * 100) : 0;
    if (share >= 35) notes.push(`"${cat}" 비중이 높음(${share}%). 다음 주는 세트 10~15% 감량 권장`);
    if (share <= 10) notes.push(`"${cat}" 비중이 낮음(${share}%). 보강 세트 추가 권장`);
  });

  // 포커스 타깃이 있으면 세트 상향
  const plan = {
    split: "ULP",              // Upper/Lower/Push(예시)
    weeklyFrequency: 4,
    setsPerTarget: focusTargets.length ? 14 : 12,
    rpe: "7~8",
    focusTargets,
    notes
  };

  return { ok: true, plan };
}
