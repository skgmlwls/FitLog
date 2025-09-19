// src/tools/logger.ts
import { db } from "../firebase";

export async function logChat(
  uid: string,
  sessionId: string,
  role: "user" | "assistant" | "tool",
  content: string
) {
  await db.collection("users").doc(uid).collection("chatLogs").add({
    sessionId,
    role,
    content,
    createdAt: Date.now(),
  });
  return { ok: true };
}
