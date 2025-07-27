package com.nhj.fitlog.presentation.exercise.list

import android.content.Context
import androidx.lifecycle.ViewModel
import com.nhj.fitlog.FitLogApplication
import com.nhj.fitlog.domain.vo.ExerciseTypeVO
import com.nhj.fitlog.utils.ExerciseScreenName
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import javax.inject.Inject

@HiltViewModel
class ExerciseTypeViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
) : ViewModel() {
    val application = context as FitLogApplication

    val categories = listOf("전체", "가슴", "등", "하체", "어깨", "팔", "복부")

    val selectedCategory = MutableStateFlow("전체")

    val allExercises = listOf(
        ExerciseTypeVO("pushup_001", "푸쉬업", "가슴", 0),
        ExerciseTypeVO("pullup_001", "턱걸이", "등", 0),
        ExerciseTypeVO("squat_001", "스쿼트", "하체", 0),
        ExerciseTypeVO("lateral_001", "레터럴 레이즈", "어깨", 0)
    )

    val exerciseList = MutableStateFlow(allExercises)

    fun selectCategory(category: String) {
        selectedCategory.value = category
        exerciseList.value = if (category == "전체") {
            allExercises
        } else {
            allExercises.filter { it.category == category }
        }
    }

    // 운동 추가 화면으로 이동
    fun onNavigateExerciseAdd() {
        application.navHostController.navigate(ExerciseScreenName.EXERCISE_ADD_SCREEN.name)
    }
    // 운동 상세 화면으로 이동
    fun onNavigateExerciseDetail() {
        application.navHostController.navigate(ExerciseScreenName.EXERCISE_DETAIL_SCREEN.name)
    }
    // 뒤로가기
    fun onBackNavigation() {
        application.navHostController.popBackStack()
    }


}
