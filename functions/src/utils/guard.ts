import { HttpsError } from "firebase-functions/v2/https";

export function assertAuth(context: any, uid: string) {
  const authed = context?.auth?.uid;
  if (!authed) throw new HttpsError("unauthenticated", "로그인이 필요합니다.");
  if (uid !== authed) throw new HttpsError("permission-denied", "본인 데이터만 조회할 수 있습니다.");
}

export function assertParams(obj: any, keys: string[]) {
  for (const k of keys) {
    if (obj[k] === undefined || obj[k] === null || obj[k] === "") {
      throw new HttpsError("invalid-argument", `필수 파라미터 누락: ${k}`);
    }
  }
}
