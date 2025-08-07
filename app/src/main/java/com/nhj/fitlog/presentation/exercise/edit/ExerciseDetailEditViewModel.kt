package com.nhj.fitlog.presentation.exercise.edit

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nhj.fitlog.FitLogApplication
import com.nhj.fitlog.data.service.ExerciseService
import com.nhj.fitlog.domain.model.ExerciseTypeModel
import com.nhj.fitlog.utils.ExerciseCategories
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ExerciseDetailEditViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val exerciseService: ExerciseService
) : ViewModel() {
    val application = context as FitLogApplication

    // 원본 이름 저장 (수정 전)
    var originalName by mutableStateOf("")
    // 운동 아이디
    var exerciseId by mutableStateOf("")
    // 운동 이름
    var exerciseName by mutableStateOf("")
    // 운동 카테고리
    var exerciseCategory by mutableStateOf("") // 기본 선택값 설정
    // 기타 메모
    var exerciseMemo by mutableStateOf("")

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

    // 에러 상태
    var showNameBlankError by mutableStateOf(false)
    var showNameDuplicateError by mutableStateOf(false)
    var showSaveConfirm by mutableStateOf(false)

    // 운동 상세 수정
    fun updateExerciseType() {
        viewModelScope.launch {
            val uid = application.userUid

            // 1) 이름 미입력 검사
            if (exerciseName.isBlank()) {
                showNameBlankError = true
                return@launch
            }

            // 2) 중복 검사 (이름이 변경된 경우에만)
            if (exerciseName != originalName) {
                val available = exerciseService.isNameAvailable(uid, exerciseName)
                if (!available) {
                    showNameDuplicateError = true
                    return@launch
                }
            }

            // 3) 업데이트
            val model = ExerciseTypeModel(
                id        = exerciseId,
                name      = exerciseName,
                category  = exerciseCategory,
                memo      = exerciseMemo,
                createdAt = System.currentTimeMillis() // 필요 시 원본 createdAt 유지
            )
            exerciseService.updateExerciseType(uid, model)

            // 4) 확인 다이얼로그 표시
            showSaveConfirm = true
        }
    }


    // 뒤로가기
    fun onNavigateBack() {
        showSaveConfirm = false
        application.navHostController.popBackStack()
    }

}