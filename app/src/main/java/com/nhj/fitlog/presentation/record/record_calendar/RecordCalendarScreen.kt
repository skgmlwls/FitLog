package com.nhj.fitlog.presentation.record.record_calendar

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.nhj.fitlog.component.FitLogButton
import com.nhj.fitlog.component.FitLogText
import com.nhj.fitlog.component.FitLogTopBar
import com.nhj.fitlog.component.LottieLoadingOverlay
import com.nhj.fitlog.domain.model.ExerciseDayRecordModel
import com.nhj.fitlog.presentation.record.record_calendar.component.CustomCalendar
import com.nhj.fitlog.presentation.record.record_calendar.component.RecordSummaryCard
import com.nhj.fitlog.presentation.record.record_calendar.component.YearMonthWheelPickerDialog
import com.nhj.fitlog.utils.MainScreenName
import java.time.Instant
import java.time.YearMonth
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

val Bg = Color(0xFF121212)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecordCalendarScreen(
    previousScreen : String,
    uid : String,
    nickName : String,
    viewModel: RecordCalendarViewModel = hiltViewModel(),
) {

    LaunchedEffect(Unit) {
        viewModel.previousScreen.value = previousScreen
        viewModel.uid.value = uid
        Log.d("RecordCalendarScreen", "previousScreen: $previousScreen")
    }

    val dayRecordsState = viewModel.dayRecords.collectAsState()
    val loading = viewModel.recordsLoading.collectAsState()

    val zoneId = viewModel.zoneId
    val selected by viewModel.selectedDateMillis.collectAsState()
    val displayedMonth by viewModel.displayedMonthMillis.collectAsState()
    val recordDays by viewModel.recordDays.collectAsState()

    // YearMonth 상태로 변환
    val displayedYm = remember(displayedMonth) {
        val kst = Instant.ofEpochMilli(displayedMonth).atZone(zoneId).toLocalDate()
        YearMonth.of(kst.year, kst.month)
    }
    val selectedLocalDate = selected?.let { Instant.ofEpochMilli(it).atZone(zoneId).toLocalDate() }

    // 월/연도 점프 다이얼로그
    var showMonthPicker by remember { mutableStateOf(false) }

    // ✅ 바텀시트 상태
    var showSheet by rememberSaveable { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    // ✅ 자동 오픈 여부(한 번만)
    var didAutoOpen by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.loadAllDayRecords()
    }

    // ✅ 아바타에서 진입했다면, 오늘 날짜로 선택 맞추고 바텀시트 1회 자동 오픈
    LaunchedEffect(previousScreen, loading.value) {
        if (!didAutoOpen && previousScreen == "FRIEND_AVATAR" && !loading.value) {
            // 선택 날짜를 오늘(KST)로 맞추고 시트 열기
            viewModel.onDateSelectedUtc(viewModel.todayUtcStartMillis())
            showSheet = true
            didAutoOpen = true
        }
    }

    Scaffold(
        containerColor = Bg,
        topBar = {
            FitLogTopBar(
                title = if (previousScreen == MainScreenName.MAIN_SCREEN_HOME.name) "나의 운동 기록" else "친구 운동 기록",
                onBackClick = { viewModel.onBackNavigation() }
            )
        }
    ) { inner ->
        Box(
            modifier = Modifier
                .padding(inner)
                .fillMaxSize()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Bg),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (previousScreen != MainScreenName.MAIN_SCREEN_HOME.name) {
                    Spacer(Modifier.height(15.dp))
                    FitLogText("$nickName 님의 기록", fontSize = 25, color = Color.White)
                    Spacer(Modifier.height(15.dp))
                }
                
                CustomCalendar(
                    displayedMonth = displayedYm,
                    selectedDate = selectedLocalDate,
                    recordDays = recordDays,
                    zoneId = zoneId,
                    onPrevMonth = {
                        val prev = displayedYm.minusMonths(1)
                        val firstUtc = viewModel.monthFirstDayUtc(prev.atDay(1))
                        viewModel.displayedMonthMillis.value = firstUtc
                    },
                    onNextMonth = {
                        val next = displayedYm.plusMonths(1)
                        val firstUtc = viewModel.monthFirstDayUtc(next.atDay(1))
                        viewModel.displayedMonthMillis.value = firstUtc
                    },
                    onSelectDate = { d ->
                        // 선택일 갱신 + 시트 열기
                        val utc = d.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()
                        viewModel.onDateSelectedUtc(utc)
                        showSheet = true
                    },
                    onOpenMonthPicker = { showMonthPicker = true },
                    modifier = Modifier.fillMaxWidth(),
                    containerColor = Bg,
                    dayTextColor = Color.White,
                    headerTextColor = Color.White,
                    accentColor = Color(0xFF47A6FF),
                )
            }

            // 로딩 오버레이
            LottieLoadingOverlay(
                isVisible = loading.value,
                modifier = Modifier
                    .fillMaxSize()
                    .align(Alignment.Center)
            )
        }
    }

    // ✅ 선택 날짜의 기록 목록을 바텀시트로
    val recordsForDay by remember(selectedLocalDate, dayRecordsState.value) {
        derivedStateOf {
            val key = selectedLocalDate?.format(DateTimeFormatter.ISO_DATE)
            dayRecordsState.value.filter { it.date == key }
        }
    }

    if (showSheet) {
        val configuration = LocalConfiguration.current
        val maxSheetHeight = (configuration.screenHeightDp * 0.8f).dp

        ModalBottomSheet(
            onDismissRequest = { showSheet = false },
            sheetState = sheetState,
            shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
            containerColor = Color(0xFF1B1B1B),
            dragHandle = { BottomSheetDefaults.DragHandle() }
        ) {
            // ✅ 시트 높이: 내용 높이 ~ 화면의 80% 사이로 제한
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = maxSheetHeight) // 최대치만 제한, 최소는 내용 만큼
                    .padding(bottom = 8.dp)         // 제스처영역과 겹치지 않게 약간 여백
            ) {
                // 헤더
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    FitLogText(
                        text = selectedLocalDate?.format(DateTimeFormatter.ofPattern("yyyy년 MM월 dd일")) ?: "",
                        color = Color.White,
                        fontSize = 25,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.weight(1f))
                    if (previousScreen == MainScreenName.MAIN_SCREEN_HOME.name) {
                        if (recordsForDay.isNotEmpty()) {
                            Button(
                                onClick = {
                                    viewModel.onNavigateToRecordExercise(selected)
                                    showSheet = false
                                },
                                enabled = selectedLocalDate != null,
                                shape = RoundedCornerShape(10.dp),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFF47A6FF),
                                    contentColor = Color.White,
                                    disabledContainerColor = Color(0xFF444444),
                                    disabledContentColor = Color.LightGray
                                )
                            ) { Text("기록하기", fontWeight = FontWeight.SemiBold) }
                        }
                    }
                }

                Spacer(Modifier.height(10.dp))

                if (recordsForDay.isNotEmpty()) {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            // ✅ 아이템이 적을 땐 내용 높이만큼만,
                            //    많아지면 상위 Column의 max(80%)까지 커진 뒤 내부 스크롤
                            .weight(1f, fill = false)
                            .padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        contentPadding = PaddingValues(bottom = 24.dp)
                    ) {
                        items(recordsForDay, key = { it.recordId }) { rec ->
                            RecordSummaryCard(
                                recordId = rec.recordId,
                                memo = rec.memo,
                                timeText = viewModel.formatTimeHHmm(rec.recordedAt),
                                categoriesWithVol = rec.volumeByCategory
                                    .asSequence()
                                    .filter { it.value > 0.0 }
                                    .sortedByDescending { it.value }
                                    .take(8)
                                    .map { it.key to it.value }
                                    .toList(),
                                intensity = rec.intensity,
                                onClick = {
                                    showSheet = false
                                    viewModel.onNavigateToRecordDetail(rec.recordId)
                                }   // 👈 상세 이동
                            )
                        }
                    }
                } else {
                    Column(
                        Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        FitLogText(
                            text = "이 날짜의 기록이 없어요",
                            color = Color(0xFFB0B0B0),
                            fontWeight = FontWeight.Normal
                        )
                        Spacer(Modifier.height(12.dp))
                        if (previousScreen == MainScreenName.MAIN_SCREEN_HOME.name) {
                            FitLogButton(
                                "이 날짜에 기록하기",
                                onClick = {
                                    viewModel.onNavigateToRecordExercise(selected)
                                    showSheet = false
                                },
                            )
                        }
                        Spacer(Modifier.height(16.dp))
                    }
                }
            }
        }
    }

    // 월/연도 점프 다이얼로그
    if (showMonthPicker) {
        YearMonthWheelPickerDialog(
            initial = displayedYm,
            onConfirm = { ym ->
                val firstUtc = viewModel.monthFirstDayUtc(ym.atDay(1))
                viewModel.displayedMonthMillis.value = firstUtc
                showMonthPicker = false
            },
            onDismiss = { showMonthPicker = false }
        )
    }
}
