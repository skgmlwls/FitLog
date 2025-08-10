package com.nhj.fitlog.presentation.routine.add

import android.content.Context
import android.util.Log
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
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
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

/** UI에서 다루기 편하도록 '운동 + 세트들'을 묶은 모델 */
data class RoutineExerciseWithSets(
    val exercise: RoutineExerciseModel,
    val sets: MutableList<RoutineSetModel> = mutableStateListOf()
)

@HiltViewModel
class RoutineAddViewModel @Inject constructor(
    @ApplicationContext context: Context,
    private val routineService: RoutineService
) : ViewModel() {

    // 공개 프로퍼티만 사용
    val application: FitLogApplication = context as FitLogApplication

    var name = mutableStateOf("")
    var memo = mutableStateOf("")

    // 화면 표시용: 운동 카드 목록 (각 카드에 세트 포함)
    val routineExerciseList = mutableStateOf<List<RoutineExerciseWithSets>>(emptyList())

    // 저장 에러 상태
    var showNameBlankError = mutableStateOf(false)
    var showDuplicateNameError = mutableStateOf(false)

    fun saveRoutine() {
        val uid = application.userUid
        viewModelScope.launch {
            // 1) 이름 유효성
            val routineName = name.value.trim()
            if (routineName.isBlank()) {
                showNameBlankError.value = true
                return@launch
            }

            // 2) 중복 체크
            val available = routineService.isNameAvailable(uid, routineName)
            if (!available) {
                showDuplicateNameError.value = true
                return@launch
            }

            // 3) 루틴 본문 만들기
            val routineModel = RoutineModel(
                routineId = "",
                name = routineName,
                memo = memo.value,
                exerciseCount = routineExerciseList.value.size,
                createdAt = System.currentTimeMillis()
            )

            // 4) 저장 (루틴 + exercises + sets)
            val items = routineExerciseList.value
            val newId = routineService.addRoutine(uid, routineModel, items)

            // 5) 저장 후 뒤로가기/토스트 등
            // application.showToast("저장되었습니다")
            application.navHostController.popBackStack()
        }

    }

    /** + 버튼 → 운동 선택 화면 이동 */
    fun onAddExerciseClick() {
        application.navHostController.navigate(RoutineScreenName.ROUTINE_ADD_LIST_SCREEN.name)
    }

    /** 선택 화면에서 돌아오면 savedStateHandle 값을 소비하여 카드 추가 */
    fun consumeSelectedExerciseIfExists() {
        val handle = application.navHostController.currentBackStackEntry?.savedStateHandle ?: return
        val typeId = handle.get<String>("selectedExerciseId") ?: return
        val typeName = handle.get<String>("selectedExerciseName") ?: return
        val typeCategory = handle.get<String>("selectedExerciseCategory") ?: return
        val typeMemo = handle.get<String>("selectedExerciseMemo") ?: return
        handle.remove<String>("selectedExerciseId")
        handle.remove<String>("selectedExerciseName")
        handle.remove<String>("selectedExerciseCategory")
        handle.remove<String>("selectedExerciseMemo")

        val now = System.currentTimeMillis()
        val ex = RoutineExerciseModel(
            itemId = UUID.randomUUID().toString(),
            exerciseTypeId = typeId,
            exerciseName = typeName,
            exerciseCategory = typeCategory,
            order = routineExerciseList.value.size,
            exerciseMemo = typeMemo,
            setCount = 3,
            createdAt = now
        )

        // 기본 3세트
        val defaultSets = mutableStateListOf(
            RoutineSetModel(setId = UUID.randomUUID().toString(), setNumber = 1, weight = 0.0, reps = 0, createdAt = now),
            RoutineSetModel(setId = UUID.randomUUID().toString(), setNumber = 2, weight = 0.0, reps = 0, createdAt = now),
            RoutineSetModel(setId = UUID.randomUUID().toString(), setNumber = 3, weight = 0.0, reps = 0, createdAt = now),
        )

        val updated = routineExerciseList.value.toMutableList()
        updated.add(RoutineExerciseWithSets(exercise = ex, sets = defaultSets))
        routineExerciseList.value = updated
    }

    /** 세트 추가 */
    fun addSet(itemId: String) {
        val list = routineExerciseList.value.toMutableList()
        val idx = list.indexOfFirst { it.exercise.itemId == itemId }
        if (idx < 0) return
        val nextNo = list[idx].sets.size + 1
        list[idx].sets.add(
            RoutineSetModel(
                setId = UUID.randomUUID().toString(),
                setNumber = nextNo,
                weight = 0.0,
                reps = 0,
                createdAt = System.currentTimeMillis()
            )
        )
        list[idx].exercise.setCount = list[idx].sets.size
        routineExerciseList.value = list
    }

    /** 세트 삭제(번호 재정렬) */
    fun removeSet(itemId: String, setId: String) {
        val list = routineExerciseList.value.toMutableList()
        val exIdx = list.indexOfFirst { it.exercise.itemId == itemId }
        if (exIdx < 0) return
        list[exIdx].sets.removeAll { it.setId == setId }
        list[exIdx].sets.forEachIndexed { i, s -> s.setNumber = i + 1 }
        list[exIdx].exercise.setCount = list[exIdx].sets.size
        routineExerciseList.value = list
    }

    // 무게 입력 업데이트 (문자열 → 숫자 안전 변환)
    fun updateSetWeight(itemId: String, setId: String, value: String) {
        val list = routineExerciseList.value.toMutableList()
        val exIdx = list.indexOfFirst { it.exercise.itemId == itemId }
        if (exIdx < 0) return
        val set = list[exIdx].sets.firstOrNull { it.setId == setId } ?: return
        set.weight = value.toDoubleOrNull() ?: 0.0
        routineExerciseList.value = list                 // ✅ 리스트를 다시 넣어 재구성 유도
        // 보기 좋게 출력
        Log.d("TEST", list.joinToString("\n") { ex ->
            """
        운동명: ${ex.exercise.exerciseName}
        운동카테고리: ${ex.exercise.exerciseCategory}
        운동메모: ${ex.exercise.exerciseMemo}
        세트목록:
        ${ex.sets.joinToString("\n") { s -> " - uid ${s.setId} 세트 ${s.setNumber}: ${s.weight}kg x ${s.reps}회" }}
        """.trimIndent()
        })
    }
    // 횟수 입력 업데이트 (문자열 → 숫자 안전 변환)
    fun updateSetReps(itemId: String, setId: String, value: String) {
        val list = routineExerciseList.value.toMutableList()
        val exIdx = list.indexOfFirst { it.exercise.itemId == itemId }
        if (exIdx < 0) return
        val set = list[exIdx].sets.firstOrNull { it.setId == setId } ?: return
        set.reps = value.toIntOrNull() ?: 0
        routineExerciseList.value = list                 // ✅
        // 보기 좋게 출력
        Log.d("TEST", list.joinToString("\n") { ex ->
            """
        운동명: ${ex.exercise.exerciseName}
        운동카테고리: ${ex.exercise.exerciseCategory}
        운동메모: ${ex.exercise.exerciseMemo}
        세트목록:
        ${ex.sets.joinToString("\n") { s -> " - uid ${s.setId} 세트 ${s.setNumber}: ${s.weight}kg x ${s.reps}회" }}
        """.trimIndent()
        })
    }

    // 운동 삭제
    fun removeExercise(itemId: String) {
        val list = routineExerciseList.value.toMutableList()
        val idx = list.indexOfFirst { it.exercise.itemId == itemId }
        if (idx < 0) return
        list.removeAt(idx)
        // order 재정렬
        list.forEachIndexed { i, ex -> ex.exercise.order = i }
        routineExerciseList.value = list
    }

    fun applyReorder(newOrderItemIds: List<String>) {
        val curr = routineExerciseList.value
        // id 기준으로 현재 아이템을 맵핑
        val byId = curr.associateBy { it.exercise.itemId }
        // 새 순서대로 재배열 (누락/불일치는 필터)
        val reordered = newOrderItemIds.mapNotNull { byId[it] }.toMutableList()
        // 혹시 시트에서 빠진 항목이 있다면 맨 뒤에 보존
        curr.forEach { if (it.exercise.itemId !in newOrderItemIds) reordered.add(it) }
        // order 재정렬
        reordered.forEachIndexed { i, ex -> ex.exercise.order = i }
        routineExerciseList.value = reordered
    }


}