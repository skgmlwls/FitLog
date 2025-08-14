package com.nhj.fitlog.presentation.record.record_calendar

import android.content.Context
import androidx.lifecycle.ViewModel
import com.nhj.fitlog.FitLogApplication
import com.nhj.fitlog.utils.RecordScreenName
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import javax.inject.Inject

@HiltViewModel
class RecordCalendarViewModel @Inject constructor(
    @ApplicationContext val context: Context,
) : ViewModel() {
    val application = context as FitLogApplication

    // 대한민국(KST) 시간대
    val zoneId: ZoneId = ZoneId.of("Asia/Seoul")

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

    fun onBackNavigation() = application.navHostController.popBackStack()
}
