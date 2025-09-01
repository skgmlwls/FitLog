package com.nhj.fitlog.presentation.record.record_calendar.component

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.nhj.fitlog.component.FitLogText
import kotlinx.coroutines.launch
import java.time.YearMonth
import kotlin.math.abs

@Composable
fun YearMonthWheelPickerDialog(
    initial: YearMonth = YearMonth.now(),
    yearRange: IntRange = (initial.year - 30)..(initial.year + 30),
    onConfirm: (YearMonth) -> Unit,
    onDismiss: () -> Unit
) {
    val years = remember(yearRange) { yearRange.toList() }
    val months = remember { (1..12).toList() }

    var selectedYearIndex by remember { mutableStateOf(years.indexOf(initial.year).coerceAtLeast(0)) }
    var selectedMonthIndex by remember { mutableStateOf(initial.monthValue - 1) }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = {
                onConfirm(YearMonth.of(years[selectedYearIndex], months[selectedMonthIndex]))
                }
            ) {
                FitLogText("이동", color = Color(0xFF47A6FF))
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss
            ) {
                FitLogText("취소", color = Color(0xFFFFFFFF))
            }
        },
        title = { Text("월/연도 선택") },
        text = {
            YearMonthWheelContent(
                years = years,
                months = months,
                initialYearIndex = selectedYearIndex,
                initialMonthIndex = selectedMonthIndex,
                onYearIndexChange = { selectedYearIndex = it },
                onMonthIndexChange = { selectedMonthIndex = it },
            )
        },
        containerColor = Color(0xFF121212),
        titleContentColor = Color.White,
        textContentColor = Color.White
    )
}

@Composable
private fun YearMonthWheelContent(
    years: List<Int>,
    months: List<Int>,
    initialYearIndex: Int,
    initialMonthIndex: Int,
    onYearIndexChange: (Int) -> Unit,
    onMonthIndexChange: (Int) -> Unit,
    wheelHeight: Dp = 240.dp,
    itemHeight: Dp = 44.dp
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = wheelHeight)
            .padding(top = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        WheelPicker(
            items = years.map { it.toString() },
            suffix = "년",
            initialIndex = initialYearIndex,
            onSelectedIndexChanged = onYearIndexChange,
            modifier = Modifier.weight(1f),
            wheelHeight = wheelHeight,
            itemHeight = itemHeight
        )
        WheelPicker(
            items = months.map { it.toString() },
            suffix = "월",
            initialIndex = initialMonthIndex,
            onSelectedIndexChanged = onMonthIndexChange,
            modifier = Modifier.weight(1f),
            wheelHeight = wheelHeight,
            itemHeight = itemHeight
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun WheelPicker(
    items: List<String>,
    suffix: String,
    initialIndex: Int,
    onSelectedIndexChanged: (Int) -> Unit,
    modifier: Modifier = Modifier,
    wheelHeight: Dp = 240.dp,
    itemHeight: Dp = 44.dp
) {
    val state = rememberLazyListState(initialFirstVisibleItemIndex = initialIndex)
    val flingBehavior = rememberSnapFlingBehavior(lazyListState = state)
    val scope = rememberCoroutineScope()
    val density = LocalDensity.current

    // 가운데에 가장 가까운 아이템을 계산해 선택 인덱스 알림
    val selectedIndex by remember {
        derivedStateOf { centerClosestIndex(state) ?: initialIndex }
    }
    LaunchedEffect(selectedIndex) {
        onSelectedIndexChanged(selectedIndex.coerceIn(0, items.lastIndex))
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFF1E1E1E))
            .height(wheelHeight)
    ) {
        LazyColumn(
            state = state,
            flingBehavior = flingBehavior,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(vertical = (wheelHeight - itemHeight) / 2)
        ) {
            itemsIndexed(items) { index, label ->
                val isSelected = index == selectedIndex
                val textColor = if (isSelected) Color.White else Color(0xFFBDBDBD)
                val weight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(itemHeight),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "$label$suffix",
                        color = textColor,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = weight)
                    )
                }
            }
        }

        // 가운데 가이드 라인(선택 영역)
        val lineColor = Color(0xFF47A6FF)
        val lineThickness = 1.dp
        val maskHeight = (wheelHeight - itemHeight) / 2

        // 상/하 그라데이션 마스크
        Column(Modifier.fillMaxSize()) {
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(maskHeight)
                    .background(
                        Brush.verticalGradient(
                            0f to Color(0xFF1E1E1E),
                            1f to Color(0xFF1E1E1E).copy(alpha = 0f)
                        )
                    )
            )
            Spacer(Modifier.height(itemHeight))
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(maskHeight)
                    .background(
                        Brush.verticalGradient(
                            0f to Color(0xFF1E1E1E).copy(alpha = 0f),
                            1f to Color(0xFF1E1E1E)
                        )
                    )
            )
        }

        // 선택 라인 2줄
        Box(
            Modifier
                .align(Alignment.Center)
                .fillMaxWidth()
                .height(itemHeight)
        ) {
            Box(
                Modifier
                    .align(Alignment.TopCenter)
                    .fillMaxWidth()
                    .height(lineThickness)
                    .background(lineColor.copy(alpha = 0.6f))
            )
            Box(
                Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .height(lineThickness)
                    .background(lineColor.copy(alpha = 0.6f))
            )
        }
    }

    // 다이얼로그 열렸을 때 정확히 가운데 정렬로 스냅
    LaunchedEffect(Unit) {
        // 살짝 지연 후 스냅(레이아웃 측정 완료 보장)
        scope.launch {
            state.animateScrollToItem(initialIndex)
        }
    }
}

/** 가운데에 가장 가까운 아이템 인덱스 계산 */
private fun centerClosestIndex(state: LazyListState): Int? {
    val layoutInfo = state.layoutInfo
    if (layoutInfo.visibleItemsInfo.isEmpty()) return null
    val viewportCenter = (layoutInfo.viewportStartOffset + layoutInfo.viewportEndOffset) / 2
    return layoutInfo.visibleItemsInfo.minByOrNull {
        val itemCenter = it.offset + it.size / 2
        abs(itemCenter - viewportCenter)
    }?.index
}