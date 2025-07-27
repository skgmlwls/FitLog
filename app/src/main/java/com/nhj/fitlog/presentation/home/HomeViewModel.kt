package com.nhj.fitlog.presentation.home

import android.content.Context
import androidx.lifecycle.ViewModel
import com.nhj.fitlog.FitLogApplication
import com.nhj.fitlog.domain.vo.ExerciseRecordVO
import com.nhj.fitlog.domain.vo.ExerciseSetVO
import com.nhj.fitlog.utils.ExerciseScreenName
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    @ApplicationContext private val context: Context
) : ViewModel() {
    val application = context as FitLogApplication


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