package com.nhj.fitlog.presentation.exercise.add

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.nhj.fitlog.FitLogApplication
import com.nhj.fitlog.utils.ExerciseScreenName
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

@HiltViewModel
class ExerciseAddViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
) : ViewModel() {
    val application = context as FitLogApplication

    // 운동 이름
    var exerciseName by mutableStateOf("")
    // 기타 메모
    var exerciseMemo by mutableStateOf("")

    // 운동 카테고리
    val categoryOptions = listOf("가슴", "등", "하체", "어깨", "팔", "복부")
    // 선택된 카테고리 변수
    var exerciseCategory by mutableStateOf(categoryOptions.first()) // 기본 선택값 설정

    // 운동 카테고리 화면으로 이동
    fun onNavigateExerciseType() {
        application.navHostController.navigate(ExerciseScreenName.EXERCISE_TYPE_SCREEN.name) {
            popUpTo(ExerciseScreenName.EXERCISE_TYPE_SCREEN.name) {
                inclusive = true
            }
            launchSingleTop = true
        }
    }

    // 뒤로가기
    fun onBackNavigation() {
        application.navHostController.popBackStack()
    }

}