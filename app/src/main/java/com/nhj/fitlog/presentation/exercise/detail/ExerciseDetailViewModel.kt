package com.nhj.fitlog.presentation.exercise.detail

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.nhj.fitlog.FitLogApplication
import com.nhj.fitlog.data.service.ExerciseService
import com.nhj.fitlog.utils.ExerciseCategories
import com.nhj.fitlog.utils.ExerciseScreenName
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ExerciseDetailViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val exerciseService: ExerciseService
) : ViewModel() {
    val application = context as FitLogApplication

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

    // exerciseId 로 운동 상세 데이터 가져오기
    fun getExerciseType() {
        viewModelScope.launch {
            val uid = application.userUid
            exerciseService.fetchExerciseType(uid, exerciseId)
                ?.let {
                    exerciseName = it.name
                    exerciseCategory = it.category
                    exerciseMemo = it.memo
                }
        }
    }
    
    // 운동 상세 수정 화면 이동
    fun onNavigateToEdit() {
        application.navHostController.navigate(
            ExerciseScreenName.EXERCISE_DETAIL_EDIT_SCREEN.name +
                    "/${exerciseId}" +
                    "/${exerciseName}" +
                    "/${exerciseCategory}" +
                    "/${exerciseMemo}"
        )
    }

    // 운동 이전 기록 화면 이동
    fun onNavigateToHistory() {
        application.navHostController.navigate(ExerciseScreenName.EXERCISE_HISTORY_SCREEN.name)
    }

    // 뒤로가기
    fun onNavigateBack() {
        application.navHostController.popBackStack()
    }

}