// src/firebase.ts
import * as admin from "firebase-admin";

// 중복 초기화 방지
if (admin.apps.length === 0) {
  admin.initializeApp({
    // ✅ RTDB를 쓰려면 databaseURL 반드시 지정
    databaseURL: "https://fitlog-5b415-default-rtdb.firebaseio.com",
  });
}

export const db = admin.firestore();   // Firestore
export { admin };                      // RTDB는 admin.database()로 접근
