package com.nhj.fitlog.presentation.exercise.history

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.nhj.fitlog.FitLogApplication
import com.nhj.fitlog.domain.vo.ExerciseRecordVO
import com.nhj.fitlog.utils.ExerciseScreenName
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

@HiltViewModel
class ExerciseHistoryViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
) : ViewModel() {
    val application = context as FitLogApplication

    // 운동 이름
    var exerciseName by mutableStateOf("체스트 프레스")

    val historyList = listOf(
        HistoryItem("2025 / 01 / 01", "red"),
        HistoryItem("2025 / 01 / 08", "red"),
        HistoryItem("2025 / 01 / 15", "blue"),
        HistoryItem("2025 / 01 / 22", "yellow"),
        HistoryItem("2025 / 01 / 25", "blue"),
        HistoryItem("2025 / 01 / 30", "blue")
    )
    
    // 운동 이전 기록 상세 보기
    fun onNavigateToHistoryDetail(item: HistoryItem) {
        application.navHostController.navigate(ExerciseScreenName.EXERCISE_HISTORY_DETAIL_SCREEN.name)
    }

    // 뒤로가기
    fun onBackNavigation() {
        application.navHostController.popBackStack()
    }

}

data class HistoryItem(
    val date: String,
    val color: String // "red", "blue", "yellow" 등
)
