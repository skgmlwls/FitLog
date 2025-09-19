package com.nhj.fitlog.domain.vo

import com.nhj.fitlog.domain.model.ChatLogModel

/** UI 활용용 VO */
data class ChatLogVO(
    val messageId: String,
    val uid: String,
    val sessionId: String,
    val role: String,
    val content: String,
    val createdAt: Long
) {
    fun toModel() = ChatLogModel(messageId, uid, sessionId, role, content, createdAt)
}