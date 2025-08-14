package com.nhj.fitlog.presentation.routine.detail_edit

import android.content.Context
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nhj.fitlog.FitLogApplication
import com.nhj.fitlog.data.service.RoutineService
import com.nhj.fitlog.domain.model.RoutineExerciseModel
import com.nhj.fitlog.domain.model.RoutineModel
import com.nhj.fitlog.domain.model.RoutineSetModel
import com.nhj.fitlog.utils.RoutineExerciseWithSets
import com.nhj.fitlog.utils.RoutineScreenName
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

/** UI 에서 다루기 쉬운 편집용 모델 */
data class EditableExerciseWithSets(
    val exercise: RoutineExerciseModel,
    val sets: MutableList<RoutineSetModel> = mutableStateListOf()
)

@HiltViewModel
class RoutineDetailEditViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val routineService: RoutineService,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {
    private val application = context as FitLogApplication

    // args
    private val routineIdArg: String? = savedStateHandle.get<String>("routineId")

    // 루틴 메타
    var routine = mutableStateOf<RoutineModel?>(null)

    // 입력값
    var name = mutableStateOf("")
    var memo = mutableStateOf("")

    // 운동/세트 편집 리스트
    val items = mutableStateOf<List<EditableExerciseWithSets>>(emptyList())

    // 상태
    val loading = mutableStateOf(false)
    val error = mutableStateOf<String?>(null)

    // 다이얼로그 플래그
    val showNameBlankError = mutableStateOf(false)
    val showDuplicateNameError = mutableStateOf(false)

    private var loadedOnce = false

    // 최초 1회만 서버에서 로드 (force=true면 강제 재로딩)
    fun loadIfNeeded(force: Boolean = false, routineId: String? = routineIdArg) {
        if (loadedOnce && !force) return
        loadedOnce = true
        loadInternal(routineId)
    }

    /** 실제 로딩 로직 (기존 load 내용 이동) */
    fun loadInternal(routineId: String? = routineIdArg) {
        val uid = application.userUid ?: return
        val rid = routineId ?: return

        loading.value = true
        error.value = null

        viewModelScope.launch {
            try {
                val meta = routineService.fetchRoutine(uid, rid) ?: run {
                    error.value = "루틴을 찾을 수 없습니다."
                    loading.value = false
                    return@launch
                }
                routine.value = meta
                name.value = meta.name
                memo.value = meta.memo

                val exercises = routineService.getRoutineExercises(uid, rid)

                val latestMemoMap = routineService.getLatestExerciseMemos(
                    uid,
                    exercises.map { it.exerciseTypeId }.distinct()
                )
                val merged = exercises.map { ex ->
                    val latest = latestMemoMap[ex.exerciseTypeId]
                    if (!latest.isNullOrBlank()) ex.copy(exerciseMemo = latest) else ex
                }

                val editable = merged.map { ex ->
                    viewModelScope.async {
                        val sets = routineService.getRoutineSets(uid, rid, ex.itemId)
                        EditableExerciseWithSets(exercise = ex, sets = sets.toMutableList())
                    }
                }.awaitAll()

                items.value = editable
            } catch (t: Throwable) {
                error.value = t.message
            } finally {
                loading.value = false
            }
        }
    }

    /** 운동 선택값 consume 해서 편집 리스트에 추가 */
    fun consumeSelectedExerciseIfExists() {
        val handle = application.navHostController.currentBackStackEntry?.savedStateHandle ?: return
        val typeId = handle.get<String>("selectedExerciseId") ?: return
        val typeName = handle.get<String>("selectedExerciseName") ?: return
        val typeCategory = handle.get<String>("selectedExerciseCategory") ?: ""
        val typeMemo = handle.get<String>("selectedExerciseMemo") ?: ""

        // 소비 후 제거
        handle.remove<String>("selectedExerciseId")
        handle.remove<String>("selectedExerciseName")
        handle.remove<String>("selectedExerciseCategory")
        handle.remove<String>("selectedExerciseMemo")

        val now = System.currentTimeMillis()
        val newEx = RoutineExerciseModel(
            itemId = UUID.randomUUID().toString(),
            exerciseTypeId = typeId,
            exerciseName = typeName,
            exerciseCategory = typeCategory,
            exerciseMemo = typeMemo,
            order = items.value.size,
            setCount = 3,
            createdAt = now
        )
        val defaultSets = mutableStateListOf(
            RoutineSetModel(UUID.randomUUID().toString(), 1, 0.0, 0, now),
            RoutineSetModel(UUID.randomUUID().toString(), 2, 0.0, 0, now),
            RoutineSetModel(UUID.randomUUID().toString(), 3, 0.0, 0, now)
        )

        val updated = items.value.toMutableList()
        updated.add(EditableExerciseWithSets(newEx, defaultSets))
        items.value = updated
    }

    /** 세트 추가 */
    fun addSet(itemId: String) {
        val list = items.value.toMutableList()
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
        items.value = list
    }

    /** 세트 삭제 */
    fun removeSet(itemId: String, setId: String) {
        val list = items.value.toMutableList()
        val exIdx = list.indexOfFirst { it.exercise.itemId == itemId }
        if (exIdx < 0) return
        list[exIdx].sets.removeAll { it.setId == setId }
        list[exIdx].sets.forEachIndexed { i, s -> s.setNumber = i + 1 }
        list[exIdx].exercise.setCount = list[exIdx].sets.size
        items.value = list
    }

    /** 무게/횟수 업데이트 */
    fun updateSetWeight(itemId: String, setId: String, v: String) {
        val list = items.value.toMutableList()
        val exIdx = list.indexOfFirst { it.exercise.itemId == itemId }
        val set = list.getOrNull(exIdx)?.sets?.firstOrNull { it.setId == setId } ?: return
        set.weight = v.toDoubleOrNull() ?: 0.0
        items.value = list
    }
    fun updateSetReps(itemId: String, setId: String, v: String) {
        val list = items.value.toMutableList()
        val exIdx = list.indexOfFirst { it.exercise.itemId == itemId }
        val set = list.getOrNull(exIdx)?.sets?.firstOrNull { it.setId == setId } ?: return
        set.reps = v.toIntOrNull() ?: 0
        items.value = list
    }

    /** 카드(운동) 삭제 */
    fun removeExercise(itemId: String) {
        val list = items.value.toMutableList()
        val idx = list.indexOfFirst { it.exercise.itemId == itemId }
        if (idx < 0) return
        list.removeAt(idx)
        list.forEachIndexed { i, ex -> ex.exercise.order = i }
        items.value = list
    }

    /** 순서 적용 (바텀시트에서 받아온 itemId 순서) */
    fun applyReorder(newOrderItemIds: List<String>) {
        val curr = items.value
        val map = curr.associateBy { it.exercise.itemId }
        val reordered = newOrderItemIds.mapNotNull { map[it] }.toMutableList()
        curr.forEach { if (it.exercise.itemId !in newOrderItemIds) reordered.add(it) }
        reordered.forEachIndexed { i, ex -> ex.exercise.order = i }
        items.value = reordered
    }

    // 저장
    fun save() {
        val uid = application.userUid ?: return
        val rid = routine.value?.routineId ?: return

        // 1) 이름 공백 검증
        if (name.value.isBlank()) {
            showNameBlankError.value = true
            return
        }

        viewModelScope.launch {
            try {
                loading.value = true

                // 2) 중복 검사(자기 자신 제외)
                val available = routineService.isNameAvailableForEdit(uid, name.value, rid)
                if (!available) {
                    showDuplicateNameError.value = true
                    loading.value = false
                    return@launch
                }

                // 3) 메타 + exercises/sets 치환 저장
                val toSave = (routine.value ?: RoutineModel()).copy(
                    name = name.value,
                    memo = memo.value,
                    exerciseCount = items.value.size
                )
                routineService.replaceRoutine(uid, toSave, items.value.map {
                    RoutineExerciseWithSets(
                        exercise = it.exercise,
                        sets = it.sets
                    )
                })

                onBack()
            } catch (t: Throwable) {
                error.value = t.message
            } finally {
                loading.value = false
            }
        }
    }

    fun onBack() = application.navHostController.popBackStack()

    // 운동 선택 화면으로 이동
    fun onNavigateToRoutineAddList() {
        application.navHostController.navigate(RoutineScreenName.ROUTINE_ADD_LIST_SCREEN.name)
    }
}