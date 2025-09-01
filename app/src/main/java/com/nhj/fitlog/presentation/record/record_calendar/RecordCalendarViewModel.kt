package com.nhj.fitlog.presentation.record.record_calendar

import android.content.Context
import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nhj.fitlog.FitLogApplication
import com.nhj.fitlog.data.service.RecordService
import com.nhj.fitlog.domain.model.ExerciseDayRecordModel // ✅ 상위 문서 메타 모델 사용
import com.nhj.fitlog.utils.RecordScreenName
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import javax.inject.Inject

@HiltViewModel
class RecordCalendarViewModel @Inject constructor(
    @ApplicationContext val context: Context,
    private val recordService: RecordService
) : ViewModel() {
    val application = context as FitLogApplication

    val previousScreen = mutableStateOf<String>("")
    val uid = mutableStateOf<String>("")

    // 대한민국(KST) 시간대
    val zoneId: ZoneId = ZoneId.of("Asia/Seoul")

    // ✅ 모든 '상위 문서(메타)' 기록 (기존 ImportRoutine 리스트 → ExerciseDayRecordModel 리스트)
    val dayRecords = MutableStateFlow<List<ExerciseDayRecordModel>>(emptyList())
    // 로딩/에러
    val recordsLoading = MutableStateFlow(false)
    val recordsError = MutableStateFlow<String?>(null)

    // ✅ 캘린더에 점 찍을 날짜 집합
    val recordDays = MutableStateFlow<Set<LocalDate>>(emptySet())

    // ✅ 상위 문서의 date(yyyy-MM-dd) 또는 recordedAt으로 날짜 생성
    private fun rebuildRecordDaysFrom(all: List<ExerciseDayRecordModel>) {
        val fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        val days = all.mapNotNull { r ->
            // 1순위: date 문자열 파싱 / 2순위: recordedAt 사용
            val fromDate = runCatching { LocalDate.parse(r.date, fmt) }.getOrNull()
            fromDate ?: runCatching {
                Instant.ofEpochMilli(r.recordedAt).atZone(zoneId).toLocalDate()
            }.getOrNull()
        }.toSet()
        recordDays.value = days
    }

    // ✅ 상위 문서 메타만 빠르게 로드 (기존 getAllExerciseRecords → getAllDayRecords)
    fun loadAllDayRecords() {
        viewModelScope.launch {
            recordsLoading.value = true
            recordsError.value = null
            try {
                val list = recordService.getAllDayRecords(uid.value) // Service에 구현됨
                dayRecords.value = list
                rebuildRecordDaysFrom(list)
            } catch (t: Throwable) {
                recordsError.value = t.message
            } finally {
                recordsLoading.value = false
            }
            Log.d("loadAllDayRecords", dayRecords.value.toString())
        }
    }

    // LocalDate → 해당 날짜의 UTC 자정 millis 변환
    fun utcStartOfDayMillisFor(date: LocalDate): Long =
        date.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()

    // 오늘(KST)의 UTC 자정 millis 반환
    fun todayUtcStartMillis(): Long =
        utcStartOfDayMillisFor(LocalDate.now(zoneId))

    // 선택된 날짜(UTC 자정 millis)
    val selectedDateMillis = MutableStateFlow<Long?>(todayUtcStartMillis())

    // 현재 표시 중인 월(1일 UTC 자정 millis)
    val displayedMonthMillis = MutableStateFlow(monthFirstDayUtc(LocalDate.now(zoneId)))

    // DatePicker에서 날짜 선택 시 호출
    fun onDateSelectedUtc(millis: Long?) {
        val applied = millis ?: todayUtcStartMillis()
        selectedDateMillis.value = applied
        displayedMonthMillis.value = computeDisplayedMonthFromSelected(applied)
    }

    // LocalDate → 해당 달 1일의 UTC 자정 millis
    fun monthFirstDayUtc(date: LocalDate): Long =
        date.withDayOfMonth(1).atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()

    // 선택한 날짜(UTC) → KST LocalDate → 해당 달 1일의 UTC 자정 millis
    fun computeDisplayedMonthFromSelected(selectedUtcMillis: Long?): Long {
        val base = selectedUtcMillis ?: todayUtcStartMillis()
        val kstDate = Instant.ofEpochMilli(base).atZone(zoneId).toLocalDate()
        return monthFirstDayUtc(kstDate)
    }

    // millis(KST 변환) → yyyy년 MM월 형식 문자열
    fun formattedYearMonth(millis: Long?): String {
        val date = millis?.let { Instant.ofEpochMilli(it).atZone(zoneId).toLocalDate() }
            ?: LocalDate.now(zoneId)
        return date.format(DateTimeFormatter.ofPattern("yyyy년 MM월"))
    }

    // millis(KST 변환) → yyyy-MM-dd 형식 문자열
    fun formattedDate(millis: Long?): String {
        val date = millis?.let { Instant.ofEpochMilli(it).atZone(zoneId).toLocalDate() }
            ?: LocalDate.now(zoneId)
        return date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
    }

    fun onNavigateToRecordExercise(selected: Long?) {
        val selectedDateString = formattedDate(selected) // "2024-05-15"
        application.navHostController.navigate(
            "${RecordScreenName.RECORD_EXERCISE_SCREEN.name}/$selectedDateString"
        )
    }

    // 기록 상세 화면 이동
    fun onNavigateToRecordDetail(recordId: String) {
        application.navHostController.navigate(
            "${RecordScreenName.RECORD_DETAIL_SCREEN.name}/$recordId/${uid.value}/${previousScreen.value}"
        )
    }

    fun onBackNavigation() = application.navHostController.popBackStack()

    // KST 기준 HH:mm 표시
    fun formatTimeHHmm(millis: Long): String =
        Instant.ofEpochMilli(millis)
            .atZone(zoneId)
            .toLocalTime()
            .format(DateTimeFormatter.ofPattern("HH:mm"))

}
