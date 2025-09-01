package com.nhj.fitlog.presentation.home

import android.content.Context
import android.util.Log
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.datastore.preferences.core.edit
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.nhj.fitlog.FitLogApplication
import com.nhj.fitlog.data.service.FriendItem
import com.nhj.fitlog.data.service.FriendService
import com.nhj.fitlog.domain.model.UserModel
import com.nhj.fitlog.domain.vo.UserVO
import com.nhj.fitlog.utils.ExerciseScreenName
import com.nhj.fitlog.utils.FriendScreenName
import com.nhj.fitlog.utils.MainScreenName
import com.nhj.fitlog.utils.RecordScreenName
import com.nhj.fitlog.utils.RoutineScreenName
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val friendService: FriendService
) : ViewModel() {
    val application = context as FitLogApplication

    private var userListener: ListenerRegistration? = null

    // 인스타 ‘스토리’ 영역용: 오늘 업데이트한 친구들
    val updatedFriends = mutableStateListOf<FriendItem>()
    private var updatedFriendsJob: Job? = null

    // 🔄 당겨서 새로고침 상태
    val isRefreshing = mutableStateOf(false)

    /** Firestore ▶ users/{uid} 문서 변경 실시간 구독 시작 */
    fun startUserListener() {
        val uid = application.userUid
        if (uid.isBlank()) return

        userListener = FirebaseFirestore.getInstance()
            .collection("users")
            .document(uid)
            .addSnapshotListener { snap, err ->
                if (err != null) {
                    Log.e("HomeViewModel", "userListener error", err)
                    return@addSnapshotListener
                }
                snap?.toObject(UserVO::class.java)
                    ?.toModel()
                    ?.also { updated ->
                        application.userModel = updated
                        Log.d("HomeViewModel", "userModel updated: $updated")
                    }
            }
    }

    /** 친구들 중 ‘오늘 기록 있음’만 실시간 업데이트 */
    fun startUpdatedFriendsListener() {
        if (updatedFriendsJob != null) return
        val uid = application.userUid
        if (uid.isBlank()) return

        updatedFriendsJob = viewModelScope.launch {
            friendService.streamFriendItems(uid).collectLatest { list ->
                // 🔽 변경: 둘 다 비공개면 제외
                val filtered = list.filter { it.hasTodayRecord && (it.picturePublic || it.recordPublic) }
                updatedFriends.clear()
                updatedFriends.addAll(filtered)
            }
        }
    }

    /** ⤵️ 당겨서 새로고침: 한 번만 수집해서 즉시 반영 */
    fun refreshUpdatedFriends() {
        val uid = application.userUid
        if (uid.isBlank()) return
        viewModelScope.launch {
            try {
                isRefreshing.value = true
                val list = friendService.streamFriendItems(uid).first()
                // 🔽 변경: 둘 다 비공개면 제외
                val filtered = list.filter { it.hasTodayRecord && (it.picturePublic || it.recordPublic) }
                updatedFriends.clear()
                updatedFriends.addAll(filtered)
            } catch (t: Throwable) {
                Log.e("HomeViewModel", "refresh error", t)
            } finally {
                isRefreshing.value = false
            }
        }
    }


    // ViewModel이 종료되어 더 이상 사용되지 않을 때 호출됩니다.
    // Firestore 실시간 리스너를 제거하여 메모리 누수 및 불필요한 콜백을 방지
    override fun onCleared() {
        userListener?.remove()
        updatedFriendsJob?.cancel()
        super.onCleared()
    }

    // 설정 화면으로 이동
    fun onNavigateToSettings() {
        application.navHostController.navigate(MainScreenName.MAIN_SCREEN_SETTING.name)  // 실제 경로로 교체
    }

    // 운동 종류 화면으로 이동
    fun onNavigateToExerciseTypeScreen() {
        application.navHostController.navigate(ExerciseScreenName.EXERCISE_TYPE_SCREEN.name)
    }

    // 루틴 리스트 화면으로 이동
    fun onNavigateToRoutineList() {
        application.navHostController.navigate(RoutineScreenName.ROUTINE_LIST_SCREEN.name)
    }

    // 기록 켈린더 화면으로 이동
    fun onNavigateToRecordCalendar(
        previousScreen: String,
        targetUid: String = application.userUid,
        targetNickname: String = application.userModel.nickname
    ) {
        // 닉네임은 경로 안전하게 인코딩 (공백/한글 등)
        val safeNickname = android.net.Uri.encode(targetNickname)
        application.navHostController.navigate(
            "${RecordScreenName.RECORD_CALENDAR_SCREEN.name}/$previousScreen/$targetUid/$safeNickname"
        )
    }

    // 친구 목록 화면으로 이동
    fun onNavigateToFriendsList() {
        application.navHostController.navigate(FriendScreenName.FRIEND_LIST_SCREEN.name)
    }

    // 오늘 날짜/현재 시간으로 기록 작성 화면으로 이동
    fun onNavigateToRecordExerciseToday() {
        val zone = ZoneId.of("Asia/Seoul")
        val today = LocalDate.now(zone).format(DateTimeFormatter.ISO_DATE) // "yyyy-MM-dd"
        application.navHostController.navigate(
            "${RecordScreenName.RECORD_EXERCISE_SCREEN.name}/$today"
        )
    }

}