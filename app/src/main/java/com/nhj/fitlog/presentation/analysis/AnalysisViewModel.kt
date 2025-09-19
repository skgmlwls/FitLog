package com.nhj.fitlog.presentation.analysis

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nhj.fitlog.FitLogApplication
import com.nhj.fitlog.data.service.RecordService
import com.nhj.fitlog.domain.model.ExerciseDayRecordModel
import com.nhj.fitlog.utils.ExerciseCategories
import com.nhj.fitlog.utils.RecordScreenName
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import javax.inject.Inject

data class ChartEntry(val label: String, val value: Double)
data class MonthBreakdown(
    val ymLabel: String,                       // "yyyy.MM"
    val parts: List<Pair<String, Double>>,     // (카테고리명, 볼륨)
    val total: Double
)

@HiltViewModel
class AnalysisViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val recordService: RecordService
) : ViewModel() {
    val app = context as FitLogApplication
    private val zoneId = ZoneId.of("Asia/Seoul")

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

    val selectedCategory = MutableStateFlow(ExerciseCategories.TOTAL.str)
    val entries = MutableStateFlow<List<ChartEntry>>(emptyList())

    // ▼ 월 선택용 상태
    val availableMonths = MutableStateFlow<List<YearMonth>>(emptyList())
    val selectedMonth = MutableStateFlow<YearMonth?>(null)
    val selectedMonthBreakdown = MutableStateFlow<MonthBreakdown?>(null)

    val isLoading = MutableStateFlow(false)
    val error = MutableStateFlow<String?>(null)

    private var all: List<ExerciseDayRecordModel> = emptyList()
    private var monthMap: Map<YearMonth, Map<String, Double>> = emptyMap()

    fun start() {
        val uid = app.userUid
        if (uid.isBlank()) return

        viewModelScope.launch {
            try {
                isLoading.value = true
                error.value = null
                all = recordService.getAllDayRecords(uid)
                rebuildLineChart()
                buildMonthMapAndInit()
            } catch (t: Throwable) {
                error.value = t.message
            } finally {
                isLoading.value = false
            }
        }
    }

    fun selectCategory(cat: String) {
        selectedCategory.value = cat
        rebuildLineChart()
    }

    private fun rebuildLineChart() {
        if (all.isEmpty()) { entries.value = emptyList(); return }

        val cat = selectedCategory.value
        val fmt = DateTimeFormatter.ofPattern("MM/dd")
        val sorted = all.sortedBy { it.recordedAt }

        val nonZero = sorted.mapNotNull { r ->
            val dateLabel =
                if (r.date.isNotBlank()) {
                    runCatching {
                        LocalDate.parse(r.date, DateTimeFormatter.ISO_DATE).format(fmt)
                    }.getOrElse {
                        Instant.ofEpochMilli(r.recordedAt).atZone(zoneId).toLocalDate().format(fmt)
                    }
                } else {
                    Instant.ofEpochMilli(r.recordedAt).atZone(zoneId).toLocalDate().format(fmt)
                }

            val value = if (cat == ExerciseCategories.TOTAL.str) {
                r.volumeByCategory.values.sum()
            } else {
                r.volumeByCategory[cat] ?: 0.0
            }

            if (value > 0.0) ChartEntry(dateLabel, value) else null
        }

        entries.value = if (nonZero.size > 10) nonZero.takeLast(10) else nonZero
    }

    /** 월별 합계 맵 구성 + 기본 선택(가장 최신 월) */
    private fun buildMonthMapAndInit() {
        if (all.isEmpty()) { availableMonths.value = emptyList(); selectedMonth.value = null; selectedMonthBreakdown.value = null; return }

        val byMonth = mutableMapOf<YearMonth, MutableMap<String, Double>>()
        all.forEach { r ->
            val ym = yearMonthOf(r)
            val bucket = byMonth.getOrPut(ym) { mutableMapOf() }
            r.volumeByCategory.forEach { (k, v) ->
                if (v > 0.0) bucket[k] = (bucket[k] ?: 0.0) + v
            }
        }

        monthMap = byMonth
        val months = byMonth.keys.sorted()
        availableMonths.value = months
        val defaultYm = months.lastOrNull()
        selectedMonth.value = defaultYm
        buildSelectedMonthBreakdown(defaultYm)
    }

    fun selectMonth(ym: YearMonth) {
        selectedMonth.value = ym
        buildSelectedMonthBreakdown(ym)
    }

    private fun buildSelectedMonthBreakdown(ym: YearMonth?) {
        if (ym == null) { selectedMonthBreakdown.value = null; return }
        val map = monthMap[ym].orEmpty().filterValues { it > 0.0 }
        if (map.isEmpty()) { selectedMonthBreakdown.value = null; return }
        val total = map.values.sum()
        val parts = map.toList().sortedByDescending { it.second }
        val label = ym.format(DateTimeFormatter.ofPattern("yyyy.MM"))
        selectedMonthBreakdown.value = MonthBreakdown(label, parts, total)
    }

    private fun yearMonthOf(r: ExerciseDayRecordModel): YearMonth {
        return if (r.date.isNotBlank()) {
            runCatching {
                val d = LocalDate.parse(r.date, DateTimeFormatter.ISO_DATE)
                YearMonth.of(d.year, d.month)
            }.getOrElse {
                val d = Instant.ofEpochMilli(r.recordedAt).atZone(zoneId).toLocalDate()
                YearMonth.of(d.year, d.month)
            }
        } else {
            val d = Instant.ofEpochMilli(r.recordedAt).atZone(zoneId).toLocalDate()
            YearMonth.of(d.year, d.month)
        }
    }

    fun goRecordToday() {
        val today = LocalDate.now(zoneId).format(DateTimeFormatter.ISO_DATE)
        app.navHostController.navigate("${RecordScreenName.RECORD_EXERCISE_SCREEN.name}/$today")
    }

    fun back() = app.navHostController.popBackStack()
}
