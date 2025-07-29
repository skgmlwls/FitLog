package com.nhj.fitlog.presentation.home

import android.content.Context
import android.util.Log
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.nhj.fitlog.FitLogApplication
import com.nhj.fitlog.domain.model.UserModel
import com.nhj.fitlog.domain.vo.ExerciseRecordVO
import com.nhj.fitlog.domain.vo.ExerciseSetVO
import com.nhj.fitlog.domain.vo.UserVO
import com.nhj.fitlog.utils.ExerciseScreenName
import com.nhj.fitlog.utils.MainScreenName
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    @ApplicationContext private val context: Context
) : ViewModel() {
    val application = context as FitLogApplication

    private var userListener: ListenerRegistration? = null

//    /** 앱 첫 실행 시 DataStore 에 저장된 UID 읽기 */
//    suspend fun firstRun() {
//        val prefs = application.dataStore.data.first()
//        val dsUid = prefs[FitLogApplication.AUTO_LOGIN_UID_KEY] ?: "없음"
//        Log.d("HomeViewModel", "dataStoreUid = $dsUid")
//    }

    /** Firestore ▶ users/{uid} 문서 변경 실시간 구독 시작 */
    fun startUserListener() {
        val uid = application.userUid
        if (uid.isBlank()) return

        userListener = FirebaseFirestore
            .getInstance()
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

    // ViewModel이 종료되어 더 이상 사용되지 않을 때 호출됩니다.
    // Firestore 실시간 리스너를 제거하여 메모리 누수 및 불필요한 콜백을 방지
    override fun onCleared() {
        userListener?.remove()
        super.onCleared()
    }
    
    // 로그아웃
    fun onLogout() {
        viewModelScope.launch {
            // 1) DataStore 에서 UID 삭제
            application.dataStore.edit { prefs ->
                prefs.remove(FitLogApplication.AUTO_LOGIN_UID_KEY)
            }
            // 2) 메모리 UID 초기화
            application.userUid = ""
            application.userModel = UserModel()  // <- 추가된 부분

            // 3) 백스택 전체 삭제 후 로그인 화면으로 이동
            application.navHostController.navigate(MainScreenName.MAIN_SCREEN_LOGIN.name) {
                popUpTo(application.navHostController.graph.id) {
                    inclusive = true
                }
                launchSingleTop = true
            }
        }
    }


    // 친구 기록 로그 임시 더미 데이터
    val friendLogs = listOf(
        ExerciseRecordVO(
            recordId = "1",
            date = "2025-07-26",
            exerciseTypeId = "chest_upper",
            sets = listOf(
                ExerciseSetVO(setNumber = 1, reps = 10, weight = 40.0),
                ExerciseSetVO(setNumber = 2, reps = 10, weight = 40.0),
                ExerciseSetVO(setNumber = 3, reps = 10, weight = 40.0),
                ExerciseSetVO(setNumber = 4, reps = 10, weight = 40.0),
            ),
            memo = "좋았음",
            imageUrl = "",
            createdAt = System.currentTimeMillis()
        ),
        ExerciseRecordVO(
            recordId = "2",
            date = "2025-07-25",
            exerciseTypeId = "back_middle",
            sets = listOf(
                ExerciseSetVO(setNumber = 1, reps = 12, weight = 50.0),
                ExerciseSetVO(setNumber = 2, reps = 12, weight = 50.0),
                ExerciseSetVO(setNumber = 3, reps = 12, weight = 50.0),
            ),
            memo = "피곤했음",
            imageUrl = "",
            createdAt = System.currentTimeMillis() - 3600000
        )
    )

    // 설정 화면으로 이동
    fun onNavigateToSettings() {
        application.navHostController.navigate(MainScreenName.MAIN_SCREEN_SETTING.name)  // 실제 경로로 교체
    }

    // 운동 종류 화면으로 이동
    fun onNavigateToExerciseTypeScreen() {
        application.navHostController.navigate(ExerciseScreenName.EXERCISE_TYPE_SCREEN.name)
    }

}