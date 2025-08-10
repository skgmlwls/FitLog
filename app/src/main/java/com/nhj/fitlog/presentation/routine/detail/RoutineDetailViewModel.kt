package com.nhj.fitlog.presentation.routine.detail

import android.content.Context
import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nhj.fitlog.FitLogApplication
import com.nhj.fitlog.data.service.RoutineService
import com.nhj.fitlog.domain.model.RoutineExerciseModel
import com.nhj.fitlog.domain.model.RoutineModel
import com.nhj.fitlog.domain.model.RoutineSetModel
import com.nhj.fitlog.utils.RoutineScreenName
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.launch
import javax.inject.Inject

// 운동 + 세트 묶음 데이터 클래스
data class RoutineExerciseWithSets(
    val exercise: RoutineExerciseModel,
    val sets: List<RoutineSetModel>
)

@HiltViewModel
class RoutineDetailViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val routineService: RoutineService,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val application = context as FitLogApplication

    // 현재 선택된 루틴 정보
    val routine = mutableStateOf<RoutineModel?>(null)
    // 루틴의 운동 및 세트 목록
    val items = mutableStateOf<List<RoutineExerciseWithSets>>(emptyList())
    // 로딩 상태 표시
    val loading = mutableStateOf(false)
    // 오류 메시지
    val error = mutableStateOf<String?>(null)

    // Navigation 인자로 전달된 routineId
    private val routineIdArg: String? = savedStateHandle.get<String>("routineId")

    // 루틴 상세 데이터 로드
    fun load(routineId: String? = routineIdArg) {
        val uid = application.userUid ?: return
        val rid = routineId ?: return

        loading.value = true
        error.value = null

        viewModelScope.launch {
            try {
                // 1) 루틴 데이터 불러오기
                routine.value = routineService.fetchRoutine(uid, rid)

                // 2) 루틴에 속한 운동 목록 불러오기
                val exercises = routineService.getRoutineExercises(uid, rid)

                // 3) 최신 운동 메모로 덮어쓰기
                val latestMemoMap = routineService.getLatestExerciseMemos(
                    uid,
                    exercises.map { it.exerciseTypeId }.distinct()
                )
                val mergedExercises = exercises.map { ex ->
                    val latest = latestMemoMap[ex.exerciseTypeId]
                    if (!latest.isNullOrBlank()) ex.copy(exerciseMemo = latest) else ex
                }

                // 4) 각 운동의 세트 목록을 병렬로 불러오기
                val combined = mergedExercises.map { ex ->
                    viewModelScope.async {
                        val sets = routineService.getRoutineSets(uid, rid, ex.itemId)
                        RoutineExerciseWithSets(ex, sets)
                    }
                }.awaitAll()

                // 화면 표시용 데이터 업데이트
                items.value = combined
            } catch (t: Throwable) {
                Log.e("RoutineDetailVM", "load failed", t)
                error.value = t.message
            } finally {
                loading.value = false
            }
        }
    }

    // 뒤로 가기
    fun onBack() = application.navHostController.popBackStack()

    // 루틴 수정
    fun onNavigateToEdit(routineId: String) = application.navHostController.navigate(
        "${RoutineScreenName.ROUTINE_DETAIL_EDIT_SCREEN.name}/$routineId"
    )


}