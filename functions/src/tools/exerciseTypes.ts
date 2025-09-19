// src/tools/exerciseTypes.ts
import { db } from "../firebase";

/** list_exercise_types: 전체/카테고리별 운동종류 목록 */
export async function listExerciseTypes(uid: string, category?: string) {
  const colRef = db.collection(`users/${uid}/exerciseType`);
  let q: FirebaseFirestore.Query = colRef; // ← Query로 시작

  if (category) {
    q = q.where("category", "==", category);
  }

  const snap = await q.orderBy("name").get();

  const rows = snap.docs.map(d => {
    const x = d.data() as any;
    return {
      id: x.id,
      name: x.name,
      category: x.category,
      memo: x.memo || "",
      createdAt: x.createdAt || 0
    };
  });

  return { ok: true, data: rows };
}

/** search_exercise_types: 이름 부분 일치 검색(소문자 간략 검색) */
export async function searchExerciseTypes(uid: string, keyword: string) {
  const kw = (keyword || "").trim();
  if (!kw) return { ok: true, data: [] };

  // Firestore는 contains 쿼리가 없어 클라/서버에서 전체 fetch 후 필터(소규모 전제)
  const snap = await db.collection(`users/${uid}/exerciseType`).get();
  const rows = snap.docs
    .map(d => d.data() as any)
    .filter(x => String(x.name || "").toLowerCase().includes(kw.toLowerCase()))
    .sort((a, b) => String(a.name).localeCompare(String(b.name)))
    .map(x => ({ id: x.id, name: x.name, category: x.category, memo: x.memo || "" }));

  return { ok: true, data: rows.slice(0, 50) };
}
