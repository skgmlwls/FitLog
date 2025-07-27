package com.nhj.fitlog.presentation.exercise.history_detail

import android.content.Context
import androidx.lifecycle.ViewModel
import com.nhj.fitlog.FitLogApplication
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

@HiltViewModel
class ExerciseHistoryDetailViewModel @Inject constructor(
    @ApplicationContext val context: Context
) : ViewModel() {
    val application = context as FitLogApplication

    val date = "2025 / 01 / 01"
    val exerciseName = "체스트 프레스 - 뉴텍머신"
    val intensityColor = "red" // red / blue / yellow

    val exerciseSets = listOf(
        ExerciseSetUI("50", "12"),
        ExerciseSetUI("50", "12"),
        ExerciseSetUI("60", "12")
    )

    // 뒤로가기
    fun onBackNavigation() {
        application.navHostController.popBackStack()
    }

}

data class ExerciseSetUI(
    val weight: String,
    val reps: String
)