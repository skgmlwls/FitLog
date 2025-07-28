package com.nhj.fitlog.presentation.home

import android.content.Context
import android.util.Log
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nhj.fitlog.FitLogApplication
import com.nhj.fitlog.domain.vo.ExerciseRecordVO
import com.nhj.fitlog.domain.vo.ExerciseSetVO
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

    /** 앱 첫 실행 시 DataStore 에 저장된 UID 읽기 */
    suspend fun firstRun() {
        val prefs = application.dataStore.data.first()
        val dsUid = prefs[FitLogApplication.AUTO_LOGIN_UID_KEY] ?: "없음"
        Log.d("HomeViewModel", "dataStoreUid = $dsUid")
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

    fun onNavigateToExerciseTypeScreen() {
        application.navHostController.navigate(ExerciseScreenName.EXERCISE_TYPE_SCREEN.name)
    }

}