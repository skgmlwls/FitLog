package com.nhj.fitlog.data.repository

import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

data class FriendDoc(
    val docId: String,
    val friendUid: String,
    val isVisible: Boolean,
    val addedAt: Long
)

data class UserBrief(
    val uid: String,
    val nickname: String,
    val profileImageUrl: String,
    val picturePublic: Boolean,
    val recordPublic: Boolean
)


data class FriendRequestDoc(
    val requestId: String,
    val fromUid: String,
    val status: String,
    val createdAt: Long
)

class FriendRepository {
    private val db = FirebaseFirestore.getInstance()

    /** /users/{uid}/friends 실시간 스트림 */
    fun streamFriends(uid: String) = callbackFlow<List<FriendDoc>> {
        val reg = db.collection("users")
            .document(uid)
            .collection("friends")
            .orderBy("addedAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snap, err ->
                if (err != null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                val list = snap?.documents.orEmpty().mapNotNull { it.toFriendDoc() }
                trySend(list)
            }
        awaitClose { reg.remove() }
    }

    /** 개별 친구 공개여부 토글 */
    suspend fun updateVisibility(myUid: String, friendDocId: String, visible: Boolean) {
        db.collection("users").document(myUid)
            .collection("friends").document(friendDocId)
            .update("isVisible", visible)
            .await()
    }

    /** 간단 프로필 조회: /users/{friendUid} */
    suspend fun getUserBrief(friendUid: String): UserBrief? {
        if (friendUid.isBlank()) return null
        val doc = db.collection("users").document(friendUid).get().await()
        if (!doc.exists()) return null
        return UserBrief(
            uid = friendUid,
            nickname = doc.getString("nickname") ?: "",
            profileImageUrl = doc.getString("profileImageUrl") ?: "",
            picturePublic = doc.getBoolean("picturePublic") ?: false,
            recordPublic = doc.getBoolean("recordPublic") ?: false
        )
    }

    /** 친구 추가 (friendUid만 알고 있을 때) */
    suspend fun addFriend(myUid: String, friendUid: String) {
        val now = System.currentTimeMillis()
        val ref = db.collection("users").document(myUid)
            .collection("friends").document(friendUid) // ← 문서 ID 고정
        ref.set(
            mapOf(
                "friendUid" to friendUid,
                "isVisible" to true,
                "addedAt" to now
            )
        ).await()
    }

    // 친구 삭제
    suspend fun deleteFriend(myUid: String, friendDocId: String) {
        val myFriends = db.collection("users").document(myUid).collection("friends")
        val myDocRef = myFriends.document(friendDocId)

        // 1) 내 도큐먼트에서 friendUid 읽기 (과거에 랜덤 ID를 썼던 경우 대비)
        val snap = myDocRef.get().await()
        val friendUid = snap.getString("friendUid") ?: friendDocId  // 필드 없으면 docId를 그대로 사용

        // 2) 상대방 쪽 친구 도큐먼트(문서 ID를 내 UID로 작성해두었음)
        val theirDocRef = db.collection("users")
            .document(friendUid)
            .collection("friends")
            .document(myUid)

        // 3) 배치로 양쪽 삭제
        db.runBatch { batch ->
            batch.delete(myDocRef)      // 내 목록에서 삭제
            batch.delete(theirDocRef)   // 상대 목록에서도 삭제
        }.await()
    }

    suspend fun searchUsersByNickname(keyword: String, limit: Long = 20): List<UserBrief> {
        if (keyword.isBlank()) return emptyList()

        // 닉네임 prefix 검색 (ex: "카" → "카" ~ "카\uf8ff")
        val start = keyword
        val end = keyword + "\uf8ff"

        val snap = db.collection("users")
            .whereGreaterThanOrEqualTo("nickname", start)
            .whereLessThanOrEqualTo("nickname", end)
            .orderBy("nickname", Query.Direction.ASCENDING)
            .limit(limit)
            .get()
            .await()

        return snap.documents.mapNotNull { doc ->
            val uid = doc.getString("uid") ?: return@mapNotNull null
            UserBrief(
                uid = uid,
                nickname = doc.getString("nickname") ?: "",
                profileImageUrl = doc.getString("profileImageUrl") ?: "",
                picturePublic = doc.getBoolean("picturePublic") ?: false,
                recordPublic = doc.getBoolean("recordPublic") ?: false
            )
        }
    }


    /** 친구의 가장 최근 운동 'date'(yyyy-MM-dd) 한 건 조회 */
    suspend fun getLatestRecordDate(userUid: String): String? {
        val snap = db.collection("users").document(userUid)
            .collection("exerciseRecords")
            .whereEqualTo("deleteState", false)               // ✅ 삭제 제외
            .orderBy("date", Query.Direction.DESCENDING)
            .limit(1)
            .get()
            .await()

        return snap.documents.firstOrNull()?.getString("date")
    }



    fun streamIncomingFriendRequests(myUid: String) = callbackFlow<List<FriendRequestDoc>> {
        val reg = db.collection("users").document(myUid)
            .collection("friendRequests")
            .whereEqualTo("status", "PENDING")
            .addSnapshotListener { snap, err ->
                if (err != null) {
                    android.util.Log.e("FriendRepository", "friendRequests listen failed", err)
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                val list = snap?.documents.orEmpty()
                    .map { d ->
                        FriendRequestDoc(
                            requestId = d.id,
                            fromUid = d.getString("fromUid") ?: "",
                            status = d.getString("status") ?: "PENDING",
                            createdAt = d.getLong("createdAt") ?: 0L
                        )
                    }
                    .sortedByDescending { it.createdAt }
                trySend(list)
            }
        awaitClose { reg.remove() }
    }

    suspend fun hasPendingOutgoingRequest(fromUid: String, toUid: String): Boolean {
        val snap = db.collection("users").document(toUid)
            .collection("friendRequests")
            .whereEqualTo("fromUid", fromUid)
            .whereEqualTo("status", "PENDING")
            .limit(1)
            .get()
            .await()
        return !snap.isEmpty
    }

    suspend fun getIncomingRequestId(myUid: String, fromUid: String): String? {
        val snap = db.collection("users").document(myUid)
            .collection("friendRequests")
            .whereEqualTo("fromUid", fromUid)
            .whereEqualTo("status", "PENDING")
            .limit(1)
            .get()
            .await()
        return snap.documents.firstOrNull()?.id
    }


    /** 닉네임 검색에서 '추가' 클릭 → 친구요청 보내기 */
    suspend fun sendFriendRequest(fromUid: String, toUid: String) {
        val data = mapOf(
            "fromUid" to fromUid,
            "status" to "PENDING",
            "createdAt" to System.currentTimeMillis()
        )
        db.collection("users").document(toUid)
            .collection("friendRequests")
            .add(data)
            .await()
    }

    // 친구 추가 요청 관련 ////////////////////////////////////////////////////////////////////////////
    /** 수락 → 내 friends에 추가 + 요청 삭제(or 상태 변경) */
    suspend fun acceptFriendRequest(myUid: String, requestId: String, fromUid: String) {
        val now = System.currentTimeMillis()
        val myFriendsRef = db.collection("users").document(myUid)
            .collection("friends").document(fromUid)           // ← 문서 ID = 상대 UID (멱등/중복방지)

        val theirFriendsRef = db.collection("users").document(fromUid)
            .collection("friends").document(myUid)             // ← 문서 ID = 내 UID

        val reqRef = db.collection("users").document(myUid)
            .collection("friendRequests").document(requestId)

        db.runBatch { batch ->
            batch.set(myFriendsRef, mapOf(
                "friendUid" to fromUid,
                "isVisible" to true,
                "addedAt" to now
            ))
            batch.set(theirFriendsRef, mapOf(
                "friendUid" to myUid,
                "isVisible" to true,
                "addedAt" to now
            ))
            batch.delete(reqRef)
        }.await()
    }

    /** 거절 → 요청 삭제(or status 변경) */
    suspend fun declineFriendRequest(myUid: String, requestId: String) {
        db.collection("users").document(myUid)
            .collection("friendRequests").document(requestId)
            .delete()
            .await()
    }
    ////////////////////////////////////////////////////////////////////////////////////////////////


    // --- helpers ---
    private fun DocumentSnapshot.toFriendDoc(): FriendDoc? {
        val friendUid = getString("friendUid") ?: ""
        val isVisible = getBoolean("isVisible") ?: true
        val addedAt = getLong("addedAt") ?: 0L
        return FriendDoc(id, friendUid, isVisible, addedAt)
    }
}
