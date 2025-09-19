package com.nhj.fitlog.data.service

import com.nhj.fitlog.data.repository.ChatLogsRepository
import com.nhj.fitlog.domain.vo.ChatLogVO
import kotlinx.coroutines.flow.Flow
import java.util.UUID

class ChatLogsService(
    private val repo: ChatLogsRepository
) {
    fun streamSession(uid: String, sessionId: String): Flow<List<ChatLogVO>> =
        repo.streamSession(uid, sessionId)

    suspend fun appendUser(uid: String, sessionId: String, text: String): ChatLogVO {
        val vo = ChatLogVO(
            messageId = UUID.randomUUID().toString(),
            uid = uid,
            sessionId = sessionId,
            role = "user",
            content = text,
            createdAt = System.currentTimeMillis()
        )
        repo.upsert(uid, vo)
        return vo
    }

    suspend fun appendAssistant(uid: String, sessionId: String, text: String): ChatLogVO {
        val vo = ChatLogVO(
            messageId = UUID.randomUUID().toString(),
            uid = uid,
            sessionId = sessionId,
            role = "assistant",
            content = text,
            createdAt = System.currentTimeMillis()
        )
        repo.upsert(uid, vo)
        return vo
    }

    suspend fun getOlderPage(uid: String, sessionId: String, before: Long?): List<ChatLogVO> =
        repo.getSessionPage(uid, sessionId, limit = 30, startBeforeCreatedAt = before)

    suspend fun clearSession(uid: String, sessionId: String) = repo.clearSession(uid, sessionId)

    suspend fun listRecentSessions(uid: String, top: Long = 10) = repo.listRecentSessions(uid, top)
}