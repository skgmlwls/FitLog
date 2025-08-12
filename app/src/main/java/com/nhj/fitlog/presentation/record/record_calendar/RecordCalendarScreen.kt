package com.nhj.fitlog.presentation.record.record_calendar

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.nhj.fitlog.component.FitLogButton
import com.nhj.fitlog.component.FitLogTopBar

val Bg = Color(0xFF121212)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecordCalendarScreen(
    viewModel: RecordCalendarViewModel = hiltViewModel(),
    onBack: (() -> Unit)? = null
) {
    val selected by viewModel.selectedDateMillis.collectAsState()
    val displayedMonth by viewModel.displayedMonthMillis.collectAsState()

    val initialMillis = selected ?: viewModel.todayUtcStartMillis()

    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = initialMillis,
        initialDisplayedMonthMillis = viewModel.computeDisplayedMonthFromSelected(initialMillis)
    )

    // DatePicker 선택 변경 시 ViewModel 반영
    LaunchedEffect(datePickerState.selectedDateMillis) {
        viewModel.onDateSelectedUtc(datePickerState.selectedDateMillis)
    }

    Scaffold(
        containerColor = Bg,
        topBar = {
            FitLogTopBar(
                title = "운동 기록",
                onBackClick = { onBack?.invoke() }
            )
        }
    ) { inner ->
        Column(
            modifier = Modifier
                .padding(inner)
                .fillMaxSize()
                .background(Color.Transparent),
            // verticalArrangement = Arrangement.SpaceBetween // 버튼을 아래로
        ) {
            Column {
                DatePicker(
                    state = datePickerState,
                    showModeToggle = false,
                    title = {}, // ← 기본 '날짜 선택' 텍스트 제거
                    colors = DatePickerDefaults.colors(
                        containerColor = Bg,
                        titleContentColor = Color.White,
                        headlineContentColor = Color.White,
                        weekdayContentColor = Color.White,
                        subheadContentColor = Color.White,
                        dayContentColor = Color.White,
                        disabledDayContentColor = Color(0xFF777777),
                        yearContentColor = Color.White,
                        currentYearContentColor = Color.White,
                        selectedYearContentColor = Color.Black,
                        selectedYearContainerColor = Color.White,
                        selectedDayContainerColor = Color(0xFF47A6FF),
                        selectedDayContentColor = Color.White,
                        todayDateBorderColor = Color.White
                    )
                )
                Spacer(Modifier.height(16.dp))

            }

            // ✅ 날짜 표시 버튼
            FitLogButton(
                text = "${viewModel.formattedDate(selected)} 기록하기",
                onClick = {
                    // 버튼 클릭 시 동작
                },
            )
        }
    }
}