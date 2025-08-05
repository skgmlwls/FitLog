package com.nhj.fitlog.presentation.exercise.list

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.nhj.fitlog.FitLogApplication
import com.nhj.fitlog.data.service.ExerciseService
import com.nhj.fitlog.domain.vo.ExerciseTypeVO
import com.nhj.fitlog.utils.ExerciseCategories
import com.nhj.fitlog.utils.ExerciseScreenName
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ExerciseTypeViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val exerciseService: ExerciseService
) : ViewModel() {
    val application = context as FitLogApplication

    // 화면 상단 카테고리 목록
    val categories = listOf(
        ExerciseCategories.TOTAL.str,
        ExerciseCategories.CHEST.str,
        ExerciseCategories.BACK.str,
        ExerciseCategories.SHOULDER.str,
        ExerciseCategories.LEG.str,
        ExerciseCategories.ARM.str,
        ExerciseCategories.ABDOMEN.str,
        ExerciseCategories.ETC.str
    )

    // 전체 데이터를 보관하는 StateFlow
    private val allExercises = MutableStateFlow<List<ExerciseTypeVO>>(emptyList())

    // 사용자가 선택한 카테고리
    val selectedCategory = MutableStateFlow(ExerciseCategories.TOTAL.str)

    // allExercises 와 selectedCategory 를 결합해 자동으로 필터링된 리스트를 내보내는 Flow
    val exerciseList: StateFlow<List<ExerciseTypeVO>> =
        allExercises
            .combine(selectedCategory) { list, category ->
                if (category == ExerciseCategories.TOTAL.str) list
                else list.filter { it.category == category }
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.Lazily,
                initialValue = emptyList()
            )

    // 초기 로드 함수
    fun loadExercises() {
        viewModelScope.launch {
            val uid = application.userUid
            allExercises.value = exerciseService.fetchExerciseTypes(uid)
        }
    }

    // 5) 카테고리 선택
    fun selectCategory(category: String) {
        selectedCategory.value = category
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
