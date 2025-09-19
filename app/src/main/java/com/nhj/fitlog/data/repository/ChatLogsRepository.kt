package com.nhj.fitlog.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.nhj.fitlog.domain.vo.ChatLogVO
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class ChatLogsRepository {
    private val db = FirebaseFirestore.getInstance()

    private fun col(uid: String) = db.collection("users")
        .document(uid)
        .collection("chatLogs")

    /** 세션 실시간 스트림 (createdAt 오름차순) */
    fun streamSession(uid: String, sessionId: String): Flow<List<ChatLogVO>> = callbackFlow {
        val reg = col(uid)
            .whereEqualTo("sessionId", sessionId)
            .orderBy("createdAt", Query.Direction.ASCENDING)
            .addSnapshotListener { snap, e ->
                if (e != null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                val list = snap?.documents?.mapNotNull { it.toObject(com.nhj.fitlog.domain.model.ChatLogModel::class.java)?.toVO() }
                    ?: emptyList()
                trySend(list)
            }
        awaitClose { reg.remove() }
    }

    /** 페이지네이션 조회 (과거 기준) */
    suspend fun getSessionPage(
        uid: String,
        sessionId: String,
        limit: Long = 30,
        startBeforeCreatedAt: Long? = null
    ): List<ChatLogVO> {
        var q = col(uid)
            .whereEqualTo("sessionId", sessionId)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .limit(limit)

        if (startBeforeCreatedAt != null) q = q.startAfter(startBeforeCreatedAt)

        return q.get().await()
            .documents
            .mapNotNull { it.toObject(com.nhj.fitlog.domain.model.ChatLogModel::class.java)?.toVO() }
            .sortedBy { it.createdAt } // UI는 ASC가 편함
    }

    /** 메시지 저장(신규/덮어쓰기) */
    suspend fun upsert(uid: String, vo: ChatLogVO) {
        col(uid).document(vo.messageId).set(vo.toModel()).await()
    }

    /** 세션 전체 삭제 */
    suspend fun clearSession(uid: String, sessionId: String) {
        val batch = db.batch()
        val docs = col(uid).whereEqualTo("sessionId", sessionId).get().await()
        docs.documents.forEach { batch.delete(it.reference) }
        batch.commit().await()
    }

    /** 최근 세션 ID 목록(최신 메시지 기준 Top N) */
    suspend fun listRecentSessions(uid: String, top: Long = 10): List<String> {
        val snaps = col(uid).orderBy("createdAt", Query.Direction.DESCENDING).limit(500).get().await()
        return snaps.documents
            .mapNotNull { it.getString("sessionId") }
            .distinct()
            .take(top.toInt())
    }
}