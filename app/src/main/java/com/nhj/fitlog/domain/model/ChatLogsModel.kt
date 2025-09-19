package com.nhj.fitlog.domain.model

import com.nhj.fitlog.domain.vo.ChatLogVO

/** Firestore 저장용 모델 */
data class ChatLogModel(
    var messageId: String = "",
    var uid: String = "",
    var sessionId: String = "",
    var role: String = "",          // "user" | "assistant" | "system"(옵션)
    var content: String = "",
    var createdAt: Long = System.currentTimeMillis()
) {
    fun toVO() = ChatLogVO(messageId, uid, sessionId, role, content, createdAt)
}