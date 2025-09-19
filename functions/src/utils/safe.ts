import { HttpsError } from "firebase-functions/v2/https";

export async function safe<T>(fn: () => Promise<T>) {
  try {
    return await fn();
  } catch (e: any) {
    console.error("[CALLABLE ERROR]", e?.stack || e);
    // 이미 HttpsError면 그대로 전달
    if (e?.code && e?.message) throw e;
    // 그 외는 의미 있는 메시지로 변환
    throw new HttpsError("internal", e?.message || "Internal error");
  }
}
