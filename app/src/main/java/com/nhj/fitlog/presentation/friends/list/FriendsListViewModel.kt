package com.nhj.fitlog.presentation.friends.list

import android.content.Context
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nhj.fitlog.FitLogApplication
import com.nhj.fitlog.data.repository.UserBrief
import com.nhj.fitlog.data.service.FriendItem
import com.nhj.fitlog.data.service.FriendService
import com.nhj.fitlog.utils.FriendScreenName
import com.nhj.fitlog.utils.MainScreenName
import com.nhj.fitlog.utils.RecordScreenName
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FriendsListViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val friendService: FriendService
) : ViewModel() {

    val application = context as FitLogApplication

    val isLoading = mutableStateOf(false)
    val error = mutableStateOf<String?>(null)
    val friends = mutableStateListOf<FriendItem>()

    val searchText = mutableStateOf("")
    private var streamJob: Job? = null

    val searchLoading = mutableStateOf(false)
    val searchResults = mutableStateListOf<UserBrief>()

    // 추가: 상태 저장용
    val pendingOutgoingUids = mutableStateListOf<String>()                 // 내가 보낸 PENDING
    val incomingRequestMap = mutableStateMapOf<String, String>()           // fromUid -> requestId (내가 받은 PENDING)

    // 공개 범위 없을 때 다이얼로그 상태
    val showPublicNoneDialog = mutableStateOf(false)

    fun clearSearchResults() {
        searchResults.clear()
    }

    fun start() {
        val userUID = application.userUid ?: return
        if (streamJob != null) return
        streamJob = viewModelScope.launch {
            isLoading.value = true
            friendService.streamFriendItems(userUID).collectLatest { items ->
                friends.clear()
                friends.addAll(items)
                isLoading.value = false
                error.value = null
            }
        }
        // ✅ 내가 받은 요청 실시간 수집해 uid->requestId 맵 갱신
        viewModelScope.launch {
            friendService.streamIncomingRequests(userUID).collectLatest { list ->
                incomingRequestMap.clear()
                list.forEach { req -> incomingRequestMap[req.fromUid] = req.requestId }
            }
        }
    }

    // 검색 시 ‘내가 보낸(PENDING)’ 상태도 계산
    fun searchUsers(keyword: String) {
        viewModelScope.launch {
            if (keyword.isBlank()) {
                searchResults.clear()
                pendingOutgoingUids.clear()
                return@launch
            }
            try {
                searchLoading.value = true
                val results = friendService.searchUsersByNickname(keyword)
                val myUid = application.userUid
                searchResults.clear()
                val filtered = results.filter { it.uid != myUid }
                searchResults.addAll(filtered)

                // ✅ 보낸요청(PENDING) 상태 동기화
                pendingOutgoingUids.clear()
                if (!myUid.isNullOrBlank()) {
                    // 병렬로 체크
                    filtered.map { it.uid }.forEach { toUid ->
                        viewModelScope.launch {
                            val pending = friendService.hasPendingOutgoingRequest(myUid, toUid)
                            if (pending) pendingOutgoingUids.add(toUid)
                        }
                    }
                }
            } catch (e: Exception) {
                error.value = e.message
            } finally {
                searchLoading.value = false
            }
        }
    }

    // 다이얼로그에서 쓸 헬퍼 (수락/거절)
    fun acceptIncoming(fromUid: String) {
        val myUid = application.userUid ?: return
        val reqId = incomingRequestMap[fromUid] ?: return
        viewModelScope.launch {
            try {
                friendService.acceptFriendRequest(myUid, reqId, fromUid)
            } catch (e: Exception) { error.value = e.message }
        }
    }

    fun declineIncoming(fromUid: String) {
        val myUid = application.userUid ?: return
        val reqId = incomingRequestMap[fromUid] ?: return
        viewModelScope.launch {
            try {
                friendService.declineFriendRequest(myUid, reqId)
            } catch (e: Exception) { error.value = e.message }
        }
    }

    fun toggleVisibility(myUid: String, friendDocId: String, visible: Boolean) {
        viewModelScope.launch {
            try {
                friendService.toggleVisibility(myUid, friendDocId, visible)
            } catch (e: Exception) {
                error.value = e.message
            }
        }
    }

    fun addFriend(myUid: String, friendName: String) {
        val userUID = application.userUid ?: return
        viewModelScope.launch {
            try {
                friendService.addFriend(myUid, friendName)
                error.value = null
            } catch (e: Exception) {
                error.value = e.message
            }
        }
    }

    /** UI에서 보여줄 필터 + 정렬된 목록 (마지막 기록 최신순) */
    fun filtered(): List<FriendItem> {
        val q = searchText.value.trim()
        val base = if (q.isEmpty()) {
            friends
        } else {
            friends.filter {
                it.nickname.contains(q, ignoreCase = true) ||
                        it.friendUid.contains(q, ignoreCase = true)
            }
        }

        // lastRecordDate(yyyy-MM-dd) 기준 최신순 ↓, null은 맨 뒤
        // 동률일 때는 addedAt DESC로 안정화
        return base.sortedWith(
            compareByDescending<FriendItem> { it.lastRecordDate ?: "" }  // "" < "2025-08-31"
                .thenByDescending { it.addedAt }
        )
    }

    fun deleteFriend(friendDocId: String) {
        val userUID = application.userUid ?: return
        viewModelScope.launch {
            try {
                friendService.deleteFriend(userUID, friendDocId)
                error.value = null
                // 실시간 스트림 중이라면 자동 갱신됨
            } catch (e: Exception) {
                error.value = e.message
            }
        }
    }

    // 친구 요청
    fun sendFriendRequest(fromUid: String, toUid: String) {
        viewModelScope.launch {
            try {
                friendService.sendFriendRequest(fromUid, toUid)
                error.value = null
            } catch (e: Exception) {
                error.value = e.message
            }
        }
    }

    fun onNavigateToFriendRequests() {
        application.navHostController.navigate(FriendScreenName.FRIEND_REQUESTS_SCREEN.name)
    }

    fun onNavigateToRecord(friendUID: String, nickName: String) {
        val screenName = FriendScreenName.FRIEND_LIST_SCREEN.name
        application.navHostController.navigate(
            "${RecordScreenName.RECORD_CALENDAR_SCREEN.name}/${screenName}/${friendUID}/${nickName}"
        )
    }

    fun onBack() = application.navHostController.popBackStack()
}
