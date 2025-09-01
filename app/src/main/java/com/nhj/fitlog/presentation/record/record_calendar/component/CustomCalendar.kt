package com.nhj.fitlog.presentation.record.record_calendar.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import java.time.*
import java.time.format.TextStyle
import java.util.*

@Composable
fun CustomCalendar(
    // 상태
    displayedMonth: YearMonth,
    selectedDate: LocalDate?,
    recordDays: Set<LocalDate>,     // 점 표시용
    zoneId: ZoneId,
    // 액션
    onPrevMonth: () -> Unit,
    onNextMonth: () -> Unit,
    onSelectDate: (LocalDate) -> Unit,
    onOpenMonthPicker: () -> Unit, // 추가: 월/연도 점프 다이얼로그 열기
    // 스타일
    modifier: Modifier = Modifier,
    containerColor: Color = Color(0xFF121212),
    dayTextColor: Color = Color.White,
    headerTextColor: Color = Color.White,
    accentColor: Color = Color(0xFF47A6FF),
    showLeadingTrailing: Boolean = false, // 앞/뒤 달 날짜 흐리게 보여줄지
    cellSize: Dp = 44.dp
) {
    val today = remember(zoneId) { LocalDate.now(zoneId) }
    val firstOfMonth = displayedMonth.atDay(1)
    val lastOfMonth = displayedMonth.atEndOfMonth()

    val startDow = DayOfWeek.SUNDAY
    val firstDowIndex = ((firstOfMonth.dayOfWeek.value % 7) - (startDow.value % 7) + 7) % 7

    Column(
        modifier = modifier
            .background(containerColor)
            .padding(12.dp)
    ) {
        // 헤더
        CalendarHeader(
            monthLabel = "${displayedMonth.year}년 ${displayedMonth.monthValue}월",
            onPrevMonth = onPrevMonth,
            onNextMonth = onNextMonth,
            headerTextColor = headerTextColor,
            onCenterClick = onOpenMonthPicker // ✅ 추가: 중앙 라벨 클릭 시 열기
        )

        Spacer(Modifier.height(8.dp))

        // 요일 헤더
        WeekdaysRow(headerTextColor)

        Spacer(Modifier.height(4.dp))

        // 6주 x 7열
        Column(Modifier.fillMaxWidth()) {
            (0 until 6).forEach { week ->
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    (0 until 7).forEach { col ->
                        val idx = week * 7 + col
                        val dayNum = idx - firstDowIndex + 1
                        val inMonth = dayNum in 1..lastOfMonth.dayOfMonth

                        val date = when {
                            inMonth -> firstOfMonth.withDayOfMonth(dayNum)
                            showLeadingTrailing -> {
                                if (dayNum < 1) firstOfMonth.minusDays((1 - dayNum).toLong())
                                else firstOfMonth.plusDays((dayNum - lastOfMonth.dayOfMonth).toLong())
                            }
                            else -> null
                        }

                        val isWeekend = date?.dayOfWeek == DayOfWeek.SATURDAY || date?.dayOfWeek == DayOfWeek.SUNDAY
                        val weekendColor = when (date?.dayOfWeek) {
                            DayOfWeek.SUNDAY -> Color(0xFFFF6B6B)
                            DayOfWeek.SATURDAY -> Color(0xFF6BA8FF)
                            else -> dayTextColor
                        }

                        DayCell(
                            date = date,
                            isInThisMonth = inMonth,
                            cellSize = cellSize,
                            today = today,
                            selected = (date != null && date == selectedDate),
                            hasRecord = (date != null && date in recordDays),
                            onClick = { d -> if (inMonth || showLeadingTrailing) onSelectDate(d) },
                            dayTextColor = if (isWeekend) weekendColor else dayTextColor,
                            faded = (date != null && !inMonth),
                            accentColor = accentColor
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CalendarHeader(
    monthLabel: String,
    onPrevMonth: () -> Unit,
    onNextMonth: () -> Unit,
    headerTextColor: Color,
    onCenterClick: () -> Unit // ✅ 추가
) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = Color(0xFF1B1B1B),
        tonalElevation = 2.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            RoundIconButton(onClick = onPrevMonth) {
                Icon(Icons.Default.ChevronLeft, contentDescription = "이전 달", tint = headerTextColor)
            }

            // ✅ 중앙 라벨을 클릭 가능하게 변경 (Month/Year Picker 오픈)
            Text(
                monthLabel,
                color = headerTextColor,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .clickable { onCenterClick() }
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            )

            RoundIconButton(onClick = onNextMonth) {
                Icon(Icons.Default.ChevronRight, contentDescription = "다음 달", tint = headerTextColor)
            }
        }
    }
}

@Composable
private fun RoundIconButton(
    onClick: () -> Unit,
    content: @Composable () -> Unit
) {
    Surface(
        shape = CircleShape,
        color = Color(0xFF2A2A2A),
        shadowElevation = 6.dp,
        modifier = Modifier.size(36.dp),
        onClick = onClick,
        tonalElevation = 0.dp
    ) {
        Box(contentAlignment = Alignment.Center) { content() }
    }
}

@Composable
private fun WeekdaysRow(headerTextColor: Color) {
    val firstDay = DayOfWeek.SUNDAY
    val days = (0..6).map { firstDay.plus(it.toLong()) }

    Row(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        days.forEach { dow ->
            val col = when (dow) {
                DayOfWeek.SUNDAY -> Color(0xFFFF6B6B)
                DayOfWeek.SATURDAY -> Color(0xFF6BA8FF)
                else -> headerTextColor.copy(alpha = 0.8f)
            }
            Box(Modifier.weight(1f), contentAlignment = Alignment.Center) {
                Text(
                    dow.getDisplayName(TextStyle.NARROW, Locale.getDefault()),
                    color = col,
                    style = MaterialTheme.typography.labelLarge
                )
            }
        }
    }
}

@Composable
private fun DayCell(
    date: LocalDate?,
    isInThisMonth: Boolean,
    cellSize: Dp,
    today: LocalDate,
    selected: Boolean,
    hasRecord: Boolean,
    onClick: (LocalDate) -> Unit,
    dayTextColor: Color,
    faded: Boolean,
    accentColor: Color
) {
    val isToday = (date != null && date == today)
    val shape = RoundedCornerShape(10.dp)

    // 배경색은 부드럽게 전환
    val targetBg = if (selected) accentColor else Color.Transparent
    val bgColor by animateColorAsState(
        targetValue = targetBg,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "bg"
    )

    // 선택 시 살짝 커지는 스케일
    val scale by animateFloatAsState(
        targetValue = if (selected) 1.06f else 1f,
        animationSpec = spring(dampingRatio = 0.9f, stiffness = Spring.StiffnessMediumLow),
        label = "scale"
    )

    // 오늘 링의 알파도 살짝 페이드
    val todayRingAlpha by animateFloatAsState(
        targetValue = if (isToday && !selected) 0.7f else 0f,
        animationSpec = spring(stiffness = Spring.StiffnessLow),
        label = "ring"
    )

    val textColor =
        if (selected) Color.White
        else if (faded) dayTextColor.copy(alpha = 0.35f)
        else dayTextColor

    Box(
        modifier = Modifier
            .size(cellSize)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clip(shape)
            .background(bgColor, shape)
            .then(
                if (todayRingAlpha > 0f)
                    Modifier.border(1.dp, Color.White.copy(alpha = todayRingAlpha), shape)
                else Modifier
            )
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) { if (date != null) onClick(date) },
        contentAlignment = Alignment.Center
    ) {
        if (date != null) {
            Text(
                text = date.dayOfMonth.toString(),
                color = textColor,
                style = MaterialTheme.typography.bodyMedium
            )

            // 기록 dot는 살짝 확대되며 페이드 인/아웃
            AnimatedVisibility(
                visible = hasRecord,
                enter = fadeIn() + scaleIn(),
                exit = fadeOut() + scaleOut(),
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 6.dp)
            ) {
                Box(
                    Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.verticalGradient(
                                listOf(Color.White, Color.White.copy(alpha = 0.85f))
                            )
                        )
                )
            }
        }
    }
}