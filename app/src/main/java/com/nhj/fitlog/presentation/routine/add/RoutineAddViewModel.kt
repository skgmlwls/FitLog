package com.nhj.fitlog.presentation.routine.add

import android.content.Context
import android.util.Log
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.nhj.fitlog.FitLogApplication
import com.nhj.fitlog.domain.model.RoutineExerciseModel
import com.nhj.fitlog.domain.model.RoutineSetModel
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.UUID
import javax.inject.Inject

/** UI에서 다루기 편하도록 '운동 + 세트들'을 묶은 모델 */
data class RoutineExerciseWithSets(
    val exercise: RoutineExerciseModel,
    val sets: MutableList<RoutineSetModel> = mutableStateListOf()
)

@HiltViewModel
class RoutineAddViewModel @Inject constructor(
    @ApplicationContext context: Context
) : ViewModel() {

    // 공개 프로퍼티만 사용
    val application: FitLogApplication = context as FitLogApplication

    var name = mutableStateOf("")
    var memo = mutableStateOf("")

    // 화면 표시용: 운동 카드 목록 (각 카드에 세트 포함)
    val routineExerciseList = mutableStateOf<List<RoutineExerciseWithSets>>(emptyList())

    /** + 버튼 → 운동 선택 화면 이동 */
    fun onAddExerciseClick() {
        application.navHostController.navigate("ROUTINE_ADD_LIST_SCREEN")
    }

    /** 선택 화면에서 돌아오면 savedStateHandle 값을 소비하여 카드 추가 */
    fun consumeSelectedExerciseIfExists() {
        val handle = application.navHostController.currentBackStackEntry?.savedStateHandle ?: return
        val typeId = handle.get<String>("selectedExerciseId") ?: return
        val typeName = handle.get<String>("selectedExerciseName") ?: return
        handle.remove<String>("selectedExerciseId")
        handle.remove<String>("selectedExerciseName")
        handle.remove<String>("selectedExerciseCategory")
        handle.remove<String>("selectedExerciseMemo")

        val now = System.currentTimeMillis()
        val ex = RoutineExerciseModel(
            itemId = UUID.randomUUID().toString(),
            exerciseTypeId = typeId,
            exerciseName = typeName,
            order = routineExerciseList.value.size,
            memo = "",
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
        세트목록:
        ${ex.sets.joinToString("\n") { s -> " - 세트 ${s.setNumber}: ${s.weight}kg x ${s.reps}회" }}
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
        세트목록:
        ${ex.sets.joinToString("\n") { s -> " - 세트 ${s.setNumber}: ${s.weight}kg x ${s.reps}회" }}
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

    // 운동 순서 변경
    fun moveExercise(from: Int, to: Int) {
        val list = routineExerciseList.value.toMutableList()
        if (from !in list.indices || to !in list.indices) return
        val item = list.removeAt(from)
        list.add(to, item)
        // order 재정렬
        list.forEachIndexed { i, ex -> ex.exercise.order = i }
        routineExerciseList.value = list
    }
    
}