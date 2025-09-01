package com.nhj.fitlog.presentation.record.record_exercise

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nhj.fitlog.FitLogApplication
import com.nhj.fitlog.data.service.RecordService
import com.nhj.fitlog.data.service.RoutineService
import com.nhj.fitlog.domain.model.ExerciseDayRecordModel
import com.nhj.fitlog.domain.model.RoutineExerciseModel
import com.nhj.fitlog.domain.model.RoutineSetModel
import com.nhj.fitlog.utils.ExerciseCategories
import com.nhj.fitlog.utils.ExerciseCategoriesVol
import com.nhj.fitlog.utils.ImportRoutine
import com.nhj.fitlog.utils.RecordIntensity
import com.nhj.fitlog.utils.RoutineExerciseWithSets
import com.nhj.fitlog.utils.RoutineScreenName
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class RecordExerciseViewModel @Inject constructor(
    @ApplicationContext val context: Context,
    private val routineService: RoutineService,
    private val recordService: RecordService
) : ViewModel() {

    val application = context as FitLogApplication
    val zoneId: ZoneId = ZoneId.of("Asia/Seoul")

    // 선택 날짜 "yyyy-MM-dd"
    val dateText = mutableStateOf("")                // "yyyy-MM-dd"
    val timeText = mutableStateOf("")                // "HH:mm" (24시간제)

    // 강도
    val intensity = mutableStateOf(RecordIntensity.NORMAL)
    // 강도 컬러
    val intensitiesColor = listOf(
        RecordIntensity.HARD to Color(0xFFFF4D4D),
        RecordIntensity.NORMAL to Color(0xFF3D7DFF),
        RecordIntensity.EASY to Color(0xFFF8FF32)
    )

    // 오늘 운동 메모
    var recordMemo by mutableStateOf("")

    // 운동 목록 (요구: ExerciseCard에서 쓰는 타입)
    val items = mutableStateOf<List<RoutineExerciseWithSets>>(emptyList())

    val exerciseVolMap = mutableStateMapOf<String, Double>().apply {
        ExerciseCategoriesVol.entries.forEach { put(it.str, 0.0) }
    }

    /** 카테고리별 볼륨(Σ weight * reps) 재계산 */
    fun exerciseVolAdd() {
        // 1) 초기화
        exerciseVolMap.keys.forEach { k -> exerciseVolMap[k] = 0.0 }

        // 2) 합산
        items.value.forEach { item ->
            // item.exercise.exerciseCategory 는 "가슴"/"등" 같은 한글 문자열이라고 가정
            val key = if (exerciseVolMap.containsKey(item.exercise.exerciseCategory))
                item.exercise.exerciseCategory
            else
                ExerciseCategories.ETC.str // 등록되지 않은 값은 "기타"로

            val volume = item.sets.sumOf { s ->
                val w = if (s.weight.isFinite()) s.weight else 0.0
                val r = s.reps.coerceAtLeast(0)
                w * r
            }

            exerciseVolMap[key] = (exerciseVolMap[key] ?: 0.0) + volume
        }

        // 전체 합계도 계산
        // val total = exerciseVolMap.values.sum()
        // Log.d("test", "total=$total, map=$exerciseVolMap")

        Log.d("test", exerciseVolMap.toString())
    }

    // ▼▼▼ 루틴 불러오기용 상태 ▼▼▼
    // 드롭다운에 보여줄 루틴들(이름 + 미리 가져온 운동/세트들)
    val importRoutines = mutableStateOf<List<ImportRoutine>>(emptyList())
    // 드롭다운 펼침 상태
    val importExpanded = mutableStateOf(false)
    // 드롭다운에 표시되는 현재 라벨
    val importLabel = mutableStateOf("루틴 불러오기")

    // 로딩/에러
    val loading = mutableStateOf(false)
    val error = mutableStateOf<String?>(null)

    // 초기화: 캘린더에서 넘어온 날짜 적용 + 루틴 불러오기 데이터 로드
    fun init(dateArg: String?) {
        if (dateText.value.isBlank()) {
            val today = LocalDate.now(zoneId).format(DateTimeFormatter.ISO_DATE)
            dateText.value = dateArg ?: today
        }
        if (timeText.value.isBlank()) {
            // 현재 시각 "HH:mm" 세팅
            val now = LocalTime.now(zoneId)
            timeText.value = now.format(DateTimeFormatter.ofPattern("HH:mm"))
        }
        // 최초 1회 루틴 목록/본문 로드
        if (importRoutines.value.isEmpty()) {
            loadImportRoutines()
        }
    }

    // 모든 루틴을 불러와 ‘루틴명 + 운동/세트들’을 importRoutines 에 보관
    fun loadImportRoutines() {
        val uid = application.userUid ?: return
        viewModelScope.launch {
            try {
                loading.value = true
                error.value = null

                val routines = routineService.fetchRoutines(uid) // List<RoutineModel>

                // 각 루틴에 대해 운동/세트 로드 (exercise → sets)
                val importList = routines.map { r ->
                    async {
                        val exList = routineService.getRoutineExercises(uid, r.routineId)
                        val items: List<RoutineExerciseWithSets> = exList.map { ex ->
                            val sets = routineService.getRoutineSets(uid, r.routineId, ex.itemId)
                            // Compose 입력 필드를 위해 set 리스트는 mutableStateListOf 로 보유
                            RoutineExerciseWithSets(
                                exercise = ex,
                                sets = mutableStateListOf<RoutineSetModel>().apply { addAll(sets) }
                            )
                        }
                        ImportRoutine(
                            routineId = r.routineId,
                            name = r.name,
                            items = items
                        )
                    }
                }.awaitAll()

                importRoutines.value = importList
            } catch (t: Throwable) {
                error.value = t.message
            } finally {
                loading.value = false
            }
        }
    }

    // 드롭다운에서 루틴 선택 시: 현재 화면의 items 에 ‘깊은 복사’로 주입
    fun applyImportRoutine(routineId: String) {
        val pack = importRoutines.value.firstOrNull { it.routineId == routineId } ?: return
        importLabel.value = pack.name

        // 깊은 복사(세트들의 setId는 유지, 값만 복제) - 입력 상태 충돌 방지
        val cloned: List<RoutineExerciseWithSets> = pack.items.map { src ->
            val exCopy = src.exercise.copy(
                // order 는 다시 계산
                order = 0
            )
            val setsCopy = mutableStateListOf<RoutineSetModel>().apply {
                src.sets.forEach { s ->
                    add(s.copy()) // number/weight/reps 동일 복제
                }
            }
            RoutineExerciseWithSets(exercise = exCopy, sets = setsCopy)
        }.onEachIndexed { index, item ->
            item.exercise.order = index
        }

        items.value = cloned
    }

    // 날짜 시간 표시용: "yyyy / MM / dd"
    // 표시용
    fun displayDate(date: String): String = try {
        LocalDate.parse(date, DateTimeFormatter.ISO_DATE)
            .format(DateTimeFormatter.ofPattern("yyyy / MM / dd"))
    } catch (_: Throwable) { date }

    fun displayTime(): String = timeText.value.ifBlank { "--:--" }

    // 외부에서 날짜/시간 설정 (DatePicker/TimePicker 결과 연결)
    fun setDate(isoDate: String) { dateText.value = isoDate }
    fun setTime(hh: Int, mm: Int) { timeText.value = "%02d:%02d".format(hh, mm) }

    /** 날짜+시간 → epochMillis(KST) */
    private fun buildRecordedAtMillis(): Long {
        return try {
            val ld = LocalDate.parse(dateText.value, DateTimeFormatter.ISO_DATE)
            val parts = timeText.value.split(":")
            val h = parts.getOrNull(0)?.toIntOrNull() ?: 0
            val m = parts.getOrNull(1)?.toIntOrNull() ?: 0
            ld.atTime(h, m).atZone(zoneId).toInstant().toEpochMilli()
        } catch (_: Throwable) {
            System.currentTimeMillis()
        }
    }

    fun displayTime12h(): String = try {
        val lt = LocalTime.parse(timeText.value, DateTimeFormatter.ofPattern("HH:mm"))
        lt.format(DateTimeFormatter.ofPattern("a hh:mm").withLocale(Locale.KOREAN)) // → "오전 09:05"
    } catch (_: Throwable) {
        "--:--"
    }

    // 강도 설정
    fun setIntensity(v: RecordIntensity) { intensity.value = v }

    // 사진 선택(자리만)
    fun onPickImage() { /* TODO: 이미지 피커 연동 */ }

    // 운동 선택 화면으로 이동
    fun onAddExerciseClick() {
        application.navHostController.navigate(RoutineScreenName.ROUTINE_ADD_LIST_SCREEN.name)
    }

    // 운동 추가 부분 ///////////////////////////////////////////////////////////////////////////////
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
        loading.value = true
        val uid = application.userUid ?: return
        viewModelScope.launch {
            exerciseVolAdd()
            try {
                loading.value = true
                val now = System.currentTimeMillis()
                val day = ExerciseDayRecordModel(
                    recordId = "", // 자동 생성
                    date = dateText.value,
                    memo = recordMemo,
                    intensity = intensity.value,
                    imageUrlList = emptyList(),     // 업로드 후 채워짐
                    exerciseCount = items.value.size,
                    recordedAt = buildRecordedAtMillis(), // 사용자가 고른 날짜+시간
                    createdAt = now,
                    volumeByCategory = exerciseVolMap
                )

                // ⬇️ Repository/Service 레이어 분리형 저장
                recordService.addDayRecord(
                    uid = uid,
                    record = day,
                    items = items.value,
                    localImageUris = imageUris.toList()
                )
                loading.value = false
                onBack()
            } catch (t: Throwable) {
                loading.value = false
                error.value = t.message
            } finally {
                loading.value = false
            }
        }
    }

    // 사진 관련 ////////////////////////////////////////////////////////////////////////////
    // 사진 URI 목록 (최대 3장)
    val imageUris = mutableStateListOf<Uri>()

    fun canAddMoreImage(): Boolean = imageUris.size < 3

    fun addImage(uri: Uri?) {
        if (uri != null && imageUris.size < 3) {
            imageUris.add(uri)
        }
    }

    fun removeImageAt(index: Int) {
        if (index in imageUris.indices) imageUris.removeAt(index)
    }
    ////////////////////////////////////////////////////////////////////////////////////////

    // 현재 항목으로 루틴 생성 플로우
    fun onMakeRoutineFromThis() {
        // TODO: 현재 기록 → 루틴으로 저장하는 로직 연결
    }

    // 뒤로가기
    fun onBack() = application.navHostController.popBackStack()
}