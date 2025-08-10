package com.nhj.fitlog.presentation.exercise.add

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.nhj.fitlog.FitLogApplication
import com.nhj.fitlog.data.service.ExerciseService
import com.nhj.fitlog.domain.model.ExerciseTypeModel
import com.nhj.fitlog.utils.ExerciseCategories
import com.nhj.fitlog.utils.ExerciseScreenName
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ExerciseAddViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val exerciseService: ExerciseService
) : ViewModel() {
    val application = context as FitLogApplication

    // 운동 이름
    var exerciseName by mutableStateOf("")
    // 기타 메모
    var exerciseMemo by mutableStateOf("")

    // 에러 표시 상태
    var showNameBlankError by mutableStateOf(false)
    var showDuplicateNameError by mutableStateOf(false)

    // 운동 카테고리
    val categoryOptions = listOf(
        ExerciseCategories.CHEST.str,
        ExerciseCategories.BACK.str,
        ExerciseCategories.SHOULDER.str,
        ExerciseCategories.LEG.str,
        ExerciseCategories.ARM.str,
        ExerciseCategories.ABDOMEN.str,
        ExerciseCategories.ETC.str
    )

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

    // 운동 종류 추가
    fun addExercise() {
        viewModelScope.launch {
            val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return@launch

            // 1) 이름 미입력 검사
            if (exerciseName.isBlank()) {
                showNameBlankError = true
                return@launch
            }

            // 2) 중복 검사
            val available = exerciseService.isNameAvailable(uid, exerciseName)
            if (!available) {
                showDuplicateNameError = true
                return@launch
            }

            // 3) 저장
            val model = ExerciseTypeModel(
                id        = "",
                name      = exerciseName,
                category  = exerciseCategory,
                memo      = exerciseMemo,
                createdAt = System.currentTimeMillis()
            )
            exerciseService.addExerciseType(uid, model)

            // 4) 완료 후 화면 복귀
//            application.navHostController.navigate(ExerciseScreenName.EXERCISE_TYPE_SCREEN.name) {
//                popUpTo(ExerciseScreenName.EXERCISE_TYPE_SCREEN.name) { inclusive = true }
//                launchSingleTop = true
//            }
            application.navHostController.popBackStack()
        }
    }

    // 뒤로가기
    fun onBackNavigation() {
        application.navHostController.popBackStack()
    }

}