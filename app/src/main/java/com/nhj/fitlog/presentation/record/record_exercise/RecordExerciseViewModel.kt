package com.nhj.fitlog.presentation.record.record_exercise

import android.content.Context
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nhj.fitlog.FitLogApplication
import com.nhj.fitlog.domain.model.ExerciseRecordModel
import com.nhj.fitlog.domain.model.ExerciseSetModel
import com.nhj.fitlog.domain.model.RoutineExerciseModel
import com.nhj.fitlog.domain.model.RoutineSetModel
import com.nhj.fitlog.utils.RecordIntensity
import com.nhj.fitlog.utils.RoutineExerciseWithSets
import com.nhj.fitlog.utils.RoutineScreenName
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class RecordExerciseViewModel @Inject constructor(
    @ApplicationContext val context: Context,
) : ViewModel() {

    val application = context as FitLogApplication
    val zoneId: ZoneId = ZoneId.of("Asia/Seoul")

    // 선택 날짜 "yyyy-MM-dd"
    val dateText = mutableStateOf("")

    // 강도
    val intensity = mutableStateOf(RecordIntensity.NORMAL)
    // 강도 컬러
    val intensitiesColor = listOf(
        RecordIntensity.HARD to Color(0xFFFF4D4D),
        RecordIntensity.NORMAL to Color(0xFF3D7DFF),
        RecordIntensity.EASY to Color(0xFFBDBDBD)
    )

    // 운동 목록 (요구: ExerciseCard에서 쓰는 타입)
    val items = mutableStateOf<List<RoutineExerciseWithSets>>(emptyList())

    // 로딩/에러
    val loading = mutableStateOf(false)
    val error = mutableStateOf<String?>(null)

    // 초기화: 캘린더에서 넘어온 날짜 적용
    fun init(dateArg: String?) {
        if (dateText.value.isNotBlank()) return
        dateText.value = dateArg ?: LocalDate.now(zoneId).format(DateTimeFormatter.ISO_DATE)
    }

    // 날짜 표시용: "yyyy / MM / dd"
    fun displayDate(date: String): String {
        return try {
            val d = LocalDate.parse(date, DateTimeFormatter.ISO_DATE)
            d.format(DateTimeFormatter.ofPattern("yyyy / MM / dd"))
        } catch (_: Throwable) {
            date
        }
    }

    // 강도 설정
    fun setIntensity(v: RecordIntensity) { intensity.value = v }

    // 사진 선택(자리만)
    fun onPickImage() { /* TODO: 이미지 피커 연동 */ }

    // 운동 선택 화면으로 이동
    fun onAddExerciseClick() {
        application.navHostController.navigate(RoutineScreenName.ROUTINE_ADD_LIST_SCREEN.name)
    }

    // 운동 선택값 consume해서 추가
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
        updated.add(RoutineExerciseWithSets(exercise = newEx, sets = defaultSets))
        items.value = updated
    }

    // 세트 추가
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

    // 세트 삭제
    fun removeSet(itemId: String, setId: String) {
        val list = items.value.toMutableList()
        val exIdx = list.indexOfFirst { it.exercise.itemId == itemId }
        if (exIdx < 0) return
        list[exIdx].sets.removeAll { it.setId == setId }
        list[exIdx].sets.forEachIndexed { i, s -> s.setNumber = i + 1 }
        list[exIdx].exercise.setCount = list[exIdx].sets.size
        items.value = list
    }

    // 무게 변경 (문자열 → Double 안전 파싱)
    fun updateSetWeight(itemId: String, setId: String, v: String) {
        val list = items.value.toMutableList()
        val exIdx = list.indexOfFirst { it.exercise.itemId == itemId }
        val set = list.getOrNull(exIdx)?.sets?.firstOrNull { it.setId == setId } ?: return
        set.weight = v.toDoubleOrNull() ?: 0.0
        items.value = list
    }

    // 횟수 변경 (문자열 → Int 안전 파싱)
    fun updateSetReps(itemId: String, setId: String, v: String) {
        val list = items.value.toMutableList()
        val exIdx = list.indexOfFirst { it.exercise.itemId == itemId }
        val set = list.getOrNull(exIdx)?.sets?.firstOrNull { it.setId == setId } ?: return
        set.reps = v.toIntOrNull() ?: 0
        items.value = list
    }

    // 운동 카드 삭제
    fun removeExercise(itemId: String) {
        val list = items.value.toMutableList()
        val idx = list.indexOfFirst { it.exercise.itemId == itemId }
        if (idx < 0) return
        list.removeAt(idx)
        list.forEachIndexed { i, ex -> ex.exercise.order = i }
        items.value = list
    }

    fun applyReorder(newOrderItemIds: List<String>) {
        val curr = items.value
        // id 기준으로 현재 아이템을 맵핑
        val byId = curr.associateBy { it.exercise.itemId }
        // 새 순서대로 재배열 (누락/불일치는 필터)
        val reordered = newOrderItemIds.mapNotNull { byId[it] }.toMutableList()
        // 혹시 시트에서 빠진 항목이 있다면 맨 뒤에 보존
        curr.forEach { if (it.exercise.itemId !in newOrderItemIds) reordered.add(it) }
        // order 재정렬
        reordered.forEachIndexed { i, ex -> ex.exercise.order = i }
        items.value = reordered
    }

    // 저장: ExerciseRecordModel 리스트로 변환 후 저장 준비
    fun onSave() {
        val uid = application.userUid ?: "" // 필요 시 사용
        viewModelScope.launch {
            try {
                loading.value = true
                val date = dateText.value
                val now = System.currentTimeMillis()

                val records = items.value.map { ex ->
                    ExerciseRecordModel(
                        recordId = UUID.randomUUID().toString(),
                        date = date, // "yyyy-MM-dd"
                        exerciseTypeId = ex.exercise.exerciseTypeId,
                        sets = ex.sets.map {
                            ExerciseSetModel(
                                setNumber = it.setNumber,
                                reps = it.reps,
                                weight = it.weight
                            )
                        },
                        memo = ex.exercise.exerciseMemo,
                        imageUrl = "", // TODO: 업로드 후 URL
                        createdAt = now
                    )
                }

                // TODO: Firestore 저장 (users/{uid}/exerciseRecords 에 records 각각 push)
                // recordService.saveAll(uid, records)

                onBack()
            } catch (t: Throwable) {
                error.value = t.message
            } finally {
                loading.value = false
            }
        }
    }

    // 현재 항목으로 루틴 생성 플로우
    fun onMakeRoutineFromThis() {
        // TODO: 현재 기록 → 루틴으로 저장하는 로직 연결
    }

    // 뒤로가기
    fun onBack() = application.navHostController.popBackStack()
}