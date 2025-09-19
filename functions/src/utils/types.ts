export type WeekAgg = {
  weekStart: string;         // YYYY-MM-DD (ISO 주 시작: 월)
  totalVolume: number;       // Σ(reps * weight)
  totalSets: number;         // 세트 수
  totalReps: number;         // 반복 수
  topSetWeight: number;      // 최고 중량
  est1RMMax: number;         // 대략적 1RM 추정 (Epley)
};

export type RecentStats = {
  sessionCount: number;
  avgSessionPerWeek: number;
  totalSets: number;
  totalVolume: number;
  volumeByCategory: Record<string, number>;
};

export type RiskReport = {
  ok: true;
  risks: string[];
};

export type NextWeekPlan = {
  ok: true;
  plan: {
    split: string;            // 예: "ULP"
    weeklyFrequency: number;  // 주당 빈도
    setsPerTarget: number;    // 타깃부위 세트
    rpe: string;              // 권장 RPE 범위
    focusTargets: string[];
    notes?: string[];
  }
};

export type DayRecordSummary = {
  recordId: string;
  date: string;
  memo: string;
  intensity: string;
  totalSets: number;
  totalVolume: number;
  exercises: Array<{
    itemId: string;
    name: string;
    category: string;
    setCount: number;
    volume: number;
  }>;
};

export type CategoryBreakdown = {
  totalVolume: number;
  breakdown: Array<{ category: string; volume: number; sharePct: number }>;
};

export type PRTrend = Array<{ weekStart: string; bestEst1RM: number }>;

export type RoutineListItem = {
  routineId: string;
  name: string;
  memo: string;
  exerciseCount: number;
  createdAt: number;
};

export type RoutineDetail = {
  routineId: string;
  name: string;
  memo: string;
  exerciseCount: number;
  createdAt: number;
  exercises: Array<{
    itemId: string;
    exerciseName: string;
    exerciseCategory: string;
    setCount: number;
    memo: string;
    sets: Array<{ setId: string; setNumber: number; reps: number; weight: number; createdAt: number }>;
  }>;
};