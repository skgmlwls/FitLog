package com.nhj.fitlog.presentation.record.record_edit

import android.content.Context
import android.net.Uri
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
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
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class RecordEditViewModel @Inject constructor(
    @ApplicationContext val context: Context,
    private val routineService: RoutineService,
    private val recordService: RecordService
) : ViewModel() {

    val application = context as FitLogApplication
    val zoneId: ZoneId = ZoneId.of("Asia/Seoul")

    // 편집 대상
    val recordId = mutableStateOf("")
    private var originalCreatedAt: Long = 0L

    // 날짜/시간
    val dateText = mutableStateOf("")        // "yyyy-MM-dd"
    val timeText = mutableStateOf("")        // "HH:mm"

    // 강도/메모
    val intensity = mutableStateOf(RecordIntensity.NORMAL)
    var recordMemo = mutableStateOf("")
    // 강도 컬러
    val intensitiesColor = listOf(
        RecordIntensity.HARD to Color(0xFFFF4D4D),
        RecordIntensity.NORMAL to Color(0xFF3D7DFF),
        RecordIntensity.EASY to Color(0xFFF8FF32)
    )

    // 운동/세트
    val items = mutableStateOf<List<RoutineExerciseWithSets>>(emptyList())

    // 이미지
    /** 서버에 이미 업로드되어 있는 이미지 URL들 (사용자가 삭제하면 여기서 제거) */
    val remoteImageUrls = mutableStateListOf<String>()
    /** 새로 추가하는 로컬 이미지 URI들 (저장 시 업로드) */
    val imageUris = mutableStateListOf<Uri>()

    fun canAddMoreImage(): Boolean = (remoteImageUrls.size + imageUris.size) < 3
    fun addImage(uri: Uri?) { if (uri != null && canAddMoreImage()) imageUris.add(uri) }
    fun removeLocalImageAt(index: Int) { if (index in imageUris.indices) imageUris.removeAt(index) }
    fun removeRemoteImageAt(index: Int) { if (index in remoteImageUrls.indices) remoteImageUrls.removeAt(index) }

    // 루틴 불러오기 (선택 시 편집중 리스트 대체)
    val importRoutines = mutableStateOf<List<ImportRoutine>>(emptyList())
    val importExpanded = mutableStateOf(false)
    val importLabel = mutableStateOf("루틴 불러오기")

    // 로딩/에러
    val loading = mutableStateOf(false)
    val error = mutableStateOf<String?>(null)

    // 다이얼로그 상태
    // 수정 확인 다이얼로그 상태
    val showEditConfirm = mutableStateOf(false)
    // 수정 취소 다이얼로그 상태
    val showEditCancelConfirm = mutableStateOf(false)

    /** 편집용 초기 로드 */
    fun init(recordIdArg: String) {
        if (recordId.value.isNotBlank()) return
        recordId.value = recordIdArg
        loadDetailAndRoutines()
    }

    private fun loadDetailAndRoutines() {
        val uid = application.userUid ?: return
        viewModelScope.launch {
            try {
                loading.value = true
                error.value = null

                // 상세 로드
                val detail = recordService.getDayRecordDetail(uid, recordId.value)
                val rec = detail.record
                originalCreatedAt = rec.createdAt
                dateText.value = rec.date
                timeText.value = Instant.ofEpochMilli(rec.recordedAt)
                    .atZone(zoneId).toLocalTime()
                    .format(DateTimeFormatter.ofPattern("HH:mm"))
                intensity.value = rec.intensity
                recordMemo.value = rec.memo
                items.value = detail.items
                remoteImageUrls.clear()
                remoteImageUrls.addAll(rec.imageUrlList)

                // 루틴 후보 로드
                val routines = routineService.fetchRoutines(uid)
                val importList = routines.map { r ->
                    async {
                        val exList = routineService.getRoutineExercises(uid, r.routineId)
                        val packItems = exList.map { ex ->
                            val sets = routineService.getRoutineSets(uid, r.routineId, ex.itemId)
                            RoutineExerciseWithSets(exercise = ex, sets = mutableStateListOf<RoutineSetModel>().apply { addAll(sets) })
                        }
                        ImportRoutine(routineId = r.routineId, name = r.name, items = packItems)
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

    // Import 적용 (현재 편집중 리스트를 대체)
    fun applyImportRoutine(routineId: String) {
        val pack = importRoutines.value.firstOrNull { it.routineId == routineId } ?: return
        importLabel.value = pack.name
        val cloned = pack.items.map { src ->
            val exCopy = src.exercise.copy(order = 0)
            val setsCopy = mutableStateListOf<RoutineSetModel>().apply { src.sets.forEach { add(it.copy()) } }
            RoutineExerciseWithSets(exercise = exCopy, sets = setsCopy)
        }.onEachIndexed { idx, it -> it.exercise.order = idx }
        items.value = cloned
    }

    // 표기용
    fun displayDate(date: String): String = try {
        LocalDate.parse(date, DateTimeFormatter.ISO_DATE)
            .format(DateTimeFormatter.ofPattern("yyyy / MM / dd"))
    } catch (_: Throwable) { date }

    fun displayTime12h(): String = try {
        val lt = LocalTime.parse(timeText.value, DateTimeFormatter.ofPattern("HH:mm"))
        lt.format(DateTimeFormatter.ofPattern("a hh:mm").withLocale(Locale.KOREAN))
    } catch (_: Throwable) { "--:--" }

    // 외부에서 날짜/시간 설정
    fun setDate(iso: String) { dateText.value = iso }
    fun setTime(h: Int, m: Int) { timeText.value = "%02d:%02d".format(h, m) }

    /** 카테고리별 볼륨(Σ weight * reps) */
    val exerciseVolMap = mutableStateMapOf<String, Double>().apply {
        ExerciseCategoriesVol.entries.forEach { put(it.str, 0.0) }
    }
    fun exerciseVolAdd() {
        exerciseVolMap.keys.forEach { k -> exerciseVolMap[k] = 0.0 }
        items.value.forEach { item ->
            val key = if (exerciseVolMap.containsKey(item.exercise.exerciseCategory))
                item.exercise.exerciseCategory else ExerciseCategories.ETC.str
            val volume = item.sets.sumOf { s ->
                val w = if (s.weight.isFinite()) s.weight else 0.0
                val r = s.reps.coerceAtLeast(0)
                w * r
            }
            exerciseVolMap[key] = (exerciseVolMap[key] ?: 0.0) + volume
        }
    }

    /** 저장 */
    fun onSave() {
        val uid = application.userUid ?: return
        viewModelScope.launch {
            try {
                loading.value = true
                exerciseVolAdd()

                val recordedAt = try {
                    val ld = LocalDate.parse(dateText.value, DateTimeFormatter.ISO_DATE)
                    val parts = timeText.value.split(":")
                    val h = parts.getOrNull(0)?.toIntOrNull() ?: 0
                    val m = parts.getOrNull(1)?.toIntOrNull() ?: 0
                    ld.atTime(h, m).atZone(zoneId).toInstant().toEpochMilli()
                } catch (_: Throwable) { System.currentTimeMillis() }

                val updated = ExerciseDayRecordModel(
                    recordId = recordId.value,
                    date = dateText.value,
                    memo = recordMemo.value,
                    intensity = intensity.value,
                    imageUrlList = remoteImageUrls.toList(),   // 유지할 원격 이미지들
                    exerciseCount = items.value.size,
                    recordedAt = recordedAt,
                    createdAt = originalCreatedAt,             // 생성 시각 보존
                    volumeByCategory = exerciseVolMap
                )

                // ✅ Service에 구현 필요
                recordService.updateDayRecord(
                    uid = uid,
                    recordId = recordId.value,
                    record = updated,
                    items = items.value,
                    localNewImageUris = imageUris.toList(),
                    keptRemoteUrls = remoteImageUrls.toList()
                )

                onBack()
            } catch (t: Throwable) {
                error.value = t.message
            } finally {
                loading.value = false
            }
        }
    }

    // 세트/운동 편집 - 작성 화면과 동일 API 유지
    fun addSet(itemId: String) {
        val list = items.value.toMutableList()
        val idx = list.indexOfFirst { it.exercise.itemId == itemId }
        if (idx < 0) return
        val nextNo = list[idx].sets.size + 1
        list[idx].sets.add(
            RoutineSetModel(
                setId = java.util.UUID.randomUUID().toString(),
                setNumber = nextNo,
                weight = 0.0, reps = 0,
                createdAt = System.currentTimeMillis()
            )
        )
        list[idx].exercise.setCount = list[idx].sets.size
        items.value = list
    }

    fun removeSet(itemId: String, setId: String) {
        val list = items.value.toMutableList()
        val idx = list.indexOfFirst { it.exercise.itemId == itemId }
        if (idx < 0) return
        list[idx].sets.removeAll { it.setId == setId }
        list[idx].sets.forEachIndexed { i, s -> s.setNumber = i + 1 }
        list[idx].exercise.setCount = list[idx].sets.size
        items.value = list
    }

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
        val byId = curr.associateBy { it.exercise.itemId }
        val reordered = newOrderItemIds.mapNotNull { byId[it] }.toMutableList()
        curr.forEach { if (it.exercise.itemId !in newOrderItemIds) reordered.add(it) }
        reordered.forEachIndexed { i, ex -> ex.exercise.order = i }
        items.value = reordered
    }

    fun setIntensity(v: RecordIntensity) { intensity.value = v }

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

    // 운동 선택 화면으로 이동
    fun onAddExerciseClick() {
        application.navHostController.navigate(RoutineScreenName.ROUTINE_ADD_LIST_SCREEN.name)
    }

    fun onBack() = application.navHostController.popBackStack()
}
