package com.nhj.fitlog.data.service

import com.nhj.fitlog.data.repository.FriendRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class FriendItem(
    val docId: String,
    val friendUid: String,
    val nickname: String,
    val profileImageUrl: String,
    val picturePublic: Boolean,
    val recordPublic: Boolean,
    val isVisible: Boolean,
    val addedAt: Long,
    val hasTodayRecord: Boolean = false,
    val lastRecordDate: String? = null    // ✅ 추가: 가장 최근 기록 날짜(yyyy-MM-dd)
)

data class FriendRequestItem(
    val requestId: String,
    val fromUid: String,
    val nickname: String,
    val profileImageUrl: String,
    val createdAt: Long
)

class FriendService(
    private val friendRepository: FriendRepository
) {
    /** 실시간 목록 + 각 친구의 닉네임/프로필 병합 */
    fun streamFriendItems(myUid: String): Flow<List<FriendItem>> {
        return friendRepository.streamFriends(myUid).map { list ->
            coroutineScope {
                val today = SimpleDateFormat("yyyy-MM-dd", Locale.KOREA).format(Date())

                list.map { fd ->
                    async {
                        val brief = friendRepository.getUserBrief(fd.friendUid)
                        val latestDate = if (fd.friendUid.isNotBlank())
                            friendRepository.getLatestRecordDate(fd.friendUid)
                        else null

                        val hasRecordToday = (latestDate == today)

                        FriendItem(
                            docId = fd.docId,
                            friendUid = fd.friendUid,
                            nickname = brief?.nickname ?: "(알 수 없음)",
                            profileImageUrl = brief?.profileImageUrl ?: "",
                            picturePublic = brief?.picturePublic ?: false,
                            recordPublic = brief?.recordPublic ?: false,
                            isVisible = fd.isVisible,
                            addedAt = fd.addedAt,
                            hasTodayRecord = hasRecordToday,
                            lastRecordDate = latestDate             // ✅ 주입
                        )
                    }
                }.map { it.await() }
            }
        }
    }


    suspend fun toggleVisibility(myUid: String, friendDocId: String, visible: Boolean) {
        withContext(Dispatchers.IO) {
            friendRepository.updateVisibility(myUid, friendDocId, visible)
        }
    }

    suspend fun addFriend(myUid: String, friendUid: String) {
        withContext(Dispatchers.IO) {
            friendRepository.addFriend(myUid, friendUid)
        }
    }

    suspend fun deleteFriend(myUid: String, friendDocId: String) {
        withContext(Dispatchers.IO) {
            friendRepository.deleteFriend(myUid, friendDocId)
        }
    }


    suspend fun searchUsersByNickname(keyword: String): List<com.nhj.fitlog.data.repository.UserBrief> {
        return withContext(Dispatchers.IO) {
            friendRepository.searchUsersByNickname(keyword)
        }
    }

    // 친구 추가 요청 관련 ////////////////////////////////////////////////////////////////////////////
    fun streamIncomingRequests(myUid: String): Flow<List<FriendRequestItem>> {
        return friendRepository.streamIncomingFriendRequests(myUid).map { docs ->
            coroutineScope {
                docs.map { doc ->
                    async {
                        val brief = friendRepository.getUserBrief(doc.fromUid)
                        FriendRequestItem(
                            requestId = doc.requestId,
                            fromUid = doc.fromUid,
                            nickname = brief?.nickname ?: "",
                            profileImageUrl = brief?.profileImageUrl ?: "",
                            createdAt = doc.createdAt
                        )
                    }
                }.map { it.await() }
            }
        }
    }

    suspend fun hasPendingOutgoingRequest(fromUid: String, toUid: String): Boolean =
        withContext(Dispatchers.IO) { friendRepository.hasPendingOutgoingRequest(fromUid, toUid) }

    suspend fun getIncomingRequestId(myUid: String, fromUid: String): String? =
        withContext(Dispatchers.IO) { friendRepository.getIncomingRequestId(myUid, fromUid) }

    suspend fun sendFriendRequest(fromUid: String, toUid: String) =
        withContext(Dispatchers.IO) { friendRepository.sendFriendRequest(fromUid, toUid) }

    suspend fun acceptFriendRequest(myUid: String, requestId: String, fromUid: String) =
        withContext(Dispatchers.IO) { friendRepository.acceptFriendRequest(myUid, requestId, fromUid) }

    suspend fun declineFriendRequest(myUid: String, requestId: String) =
        withContext(Dispatchers.IO) { friendRepository.declineFriendRequest(myUid, requestId) }
    ////////////////////////////////////////////////////////////////////////////////////////////////

}
