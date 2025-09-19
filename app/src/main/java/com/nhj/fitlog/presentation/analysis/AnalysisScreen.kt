package com.nhj.fitlog.presentation.analysis

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.nhj.fitlog.component.FitLogButton
import com.nhj.fitlog.component.FitLogText
import com.nhj.fitlog.component.FitLogTopBar
import com.nhj.fitlog.presentation.analysis.component.LineChart
import com.nhj.fitlog.presentation.analysis.component.MonthStackedBar
import com.nhj.fitlog.presentation.analysis.component.categoryBaseColor
import com.nhj.fitlog.presentation.record.record_calendar.component.YearMonthWheelPickerDialog
import java.time.YearMonth
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnalysisScreen(
    vm: AnalysisViewModel = hiltViewModel()
) {
    LaunchedEffect(Unit) { vm.start() }

    val cat by vm.selectedCategory.collectAsState()
    val points by vm.entries.collectAsState()
    val loading by vm.isLoading.collectAsState()

    val selectedYm by vm.selectedMonth.collectAsState()
    val selectedMb by vm.selectedMonthBreakdown.collectAsState()

    // 월 휠 다이얼로그
    var showYmPicker by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            FitLogTopBar(
                title = "분석",
                onBackClick = { vm.back() }
            )
        },
        containerColor = Color(0xFF121212)
    ) { inner ->
        Column(
            modifier = Modifier
                .padding(inner)
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            // 카테고리 칩
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(vm.categories) { c ->
                    FilterChip(
                        selected = (cat == c),
                        onClick = { vm.selectCategory(c) },
                        label = { Text(c) },
                        colors = FilterChipDefaults.filterChipColors(
                            containerColor = Color(0xFF3C3C3C),
                            selectedContainerColor = Color(0xFF47A6FF),
                            labelColor = Color.White,
                            selectedLabelColor = Color.White
                        ),
                        border = null
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            FitLogText(
                text = "최근 10개 추이",
                color = Color.White,
                fontWeight = FontWeight.SemiBold,
                fontSize = 20
            )

            Spacer(Modifier.height(8.dp))

            // 라인 차트 카드
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E)),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(240.dp)
            ) {
                if (loading) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                } else if (points.isEmpty()) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            FitLogText("아직 데이터가 없어요", color = Color(0xFFB0B0B0))
                            Spacer(Modifier.height(12.dp))
                            FitLogButton(
                                text = "오늘 기록하러 가기",
                                onClick = { vm.goRecordToday() }
                            )
                        }
                    }
                } else {
                    LineChart(
                        entries = points,
                        lineColor = Color(0xFF47A6FF),
                        axisLabelColor = Color(0xFFBDBDBD),
                        pointColor = Color.White,
                        minStepWidthDp = 64.dp
                    )
                }
            }

            // ─────────────── 월 선택 칩 + 선택 월 비율 ───────────────
            Spacer(Modifier.height(20.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                FitLogText(
                    text = "월별 볼륨 비율",
                    color = Color.White,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 20
                )

                Spacer(Modifier.width(12.dp))

                // ✅ "YYYY년 MM월" 칩 (누르면 휠 다이얼로그)
                AssistChip(
                    onClick = { showYmPicker = true },
                    label = {
                        FitLogText(
                            text = formatYmKorean(selectedYm ?: YearMonth.now()),
                            color = Color.White
                        )
                    },
                    colors = AssistChipDefaults.assistChipColors(
                        containerColor = Color(0xFF1E1E1E),
                        labelColor = Color.White
                    )
                )
            }

            Spacer(Modifier.height(12.dp))

            if (selectedMb == null) {
                FitLogText("해당 월 데이터가 없어요", color = Color(0xFFB0B0B0))
            } else {
                MonthStackedBar(
                    label = selectedMb!!.ymLabel,                // "yyyy.MM"
                    parts = selectedMb!!.parts,                  // (카테고리, 볼륨)
                    total = selectedMb!!.total,
                    colorProvider = { name -> categoryBaseColor(name) }
                )
            }
        }
    }

    // 월/연도 휠 다이얼로그
    if (showYmPicker) {
        YearMonthWheelPickerDialog(
            initial = selectedYm ?: YearMonth.now(),
            onConfirm = { ym ->
                vm.selectMonth(ym)
                showYmPicker = false
            },
            onDismiss = { showYmPicker = false }
        )
    }
}

private fun formatYmKorean(ym: YearMonth): String =
    ym.format(DateTimeFormatter.ofPattern("yyyy년 MM월"))
