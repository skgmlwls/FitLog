import { startOfISOWeek, format } from "date-fns";

export function isoWeekStart(dateStr: string): string {
  // dateStr: "YYYY-MM-DD" (기록의 date 필드)
  const d = new Date(dateStr + "T00:00:00");
  return format(startOfISOWeek(d), "yyyy-MM-dd");
}
