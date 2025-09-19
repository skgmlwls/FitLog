// LineChart.kt
package com.nhj.fitlog.presentation.analysis.component

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.nhj.fitlog.presentation.analysis.ChartEntry
import kotlin.math.max
import kotlin.math.roundToInt

@Composable
fun LineChart(
    entries: List<ChartEntry>,
    lineColor: Color,
    axisLabelColor: Color,
    pointColor: Color,
    gridColor: Color = Color.Unspecified,            // (호환용, 사용 안 함)
    minStepWidthDp: Dp = 64.dp,                      // 포인트 간 최소 간격 → 가로 스크롤
    modifier: Modifier = Modifier
        .fillMaxWidth()
        .height(240.dp),
    backgroundColor: Color = Color(0xFF1E1E1E)
) {
    val density = LocalDensity.current

    // 여백: 아래 날짜 라벨, 위 포인트 라벨용
    val leftPadDp = 16.dp
    val bottomPadDp = 32.dp
    val rightPadDp = 16.dp
    val topPadDp = 12.dp

    // 텍스트 크기 (조금 키움)
    val labelPx = with(density) { 14.dp.toPx() }
    val dateYOffset = 6.dp

    // 포인트 라벨(버블) 스타일 — 둥근 사각형(꼬리 없음)
    val bubbleHPad = 8.dp
    val bubbleVPad = 6.dp
    val bubbleCorner = 8.dp
    val bubbleOffsetAbovePoint = 10.dp
    val minGapFromPoint = 6.dp
    val bubbleColor = Color(0xFF2A2A2A)

    // 포인트 스타일
    val haloRadius = 10f
    val ringRadius = 7f
    val ringStroke = 3f
    val centerRadius = 4.5f

    val scrollState = rememberScrollState()
    var autoCentered by remember { mutableStateOf(false) }

    BoxWithConstraints(modifier) {
        val maxW = maxWidth
        val maxH = maxHeight

        val steps = (entries.size - 1).coerceAtLeast(1)
        val neededWidthDp = leftPadDp + rightPadDp + (minStepWidthDp * steps)
        val contentWidth = if (neededWidthDp > maxW) neededWidthDp else maxW

        // ▶ 초기 중앙 정렬 계산 (엔트리 변경 시 1회 수행)
        LaunchedEffect(entries) {
            if (entries.isEmpty()) return@LaunchedEffect
            // 뷰포트/콘텐츠 폭(px)
            val viewportPx = with(density) { maxW.toPx() }
            val contentPx = with(density) { contentWidth.toPx() }

            // 내부 패딩(px)
            val lp = with(density) { leftPadDp.toPx() }
            val rp = with(density) { rightPadDp.toPx() }

            // 차트 폭(px)
            val chartW = (contentPx - lp - rp).coerceAtLeast(1f)

            // X 간격(px)
            val n = entries.size
            val stepX = if (n > 1) chartW / (n - 1) else 0f

            // 마지막 포인트 X(px) — n==1이면 중앙
            val lastX = if (n > 1) {
                lp + stepX * (n - 1)
            } else {
                lp + chartW / 2f
            }

            // 중앙에 보이도록 스크롤 오프셋(px)
            val target = (lastX - viewportPx / 2f)
                .coerceIn(0f, max(0f, contentPx - viewportPx))
                .roundToInt()

            scrollState.scrollTo(target)
            autoCentered = true
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(scrollState)
        ) {
            Canvas(
                modifier = Modifier
                    .width(contentWidth)
                    .height(maxH).padding(horizontal = leftPadDp)
            ) {
                val w = size.width
                val h = size.height

                val lp = with(density) { leftPadDp.toPx() }
                val bp = with(density) { bottomPadDp.toPx() }
                val rp = with(density) { rightPadDp.toPx() }
                val tp = with(density) { topPadDp.toPx() }

                val chartW = (w - lp - rp).coerceAtLeast(1f)
                val chartH = (h - tp - bp).coerceAtLeast(1f)

                if (entries.isEmpty()) return@Canvas

                // Y 범위(축은 숨김, 좌표 계산용)
                val minY = entries.minOf { it.value }
                val maxY = entries.maxOf { it.value }
                val pad = (maxY - minY) * 0.1
                val yMin = (minY - pad)
                val yMax = if (maxY == minY) maxY + 1.0 else maxY + pad
                val yRange = (yMax - yMin).coerceAtLeast(1e-6)

                // X 간격
                val n = entries.size
                val stepX = if (n > 1) chartW / (n - 1) else 0f

                // X 좌표 (1개일 땐 중앙)
                fun pointX(index: Int): Float =
                    if (n > 1) lp + stepX * index
                    else lp + chartW / 2f

                // 라인 경로
                val path = Path()
                entries.forEachIndexed { i, e ->
                    val x = pointX(i)
                    val ratio = ((e.value - yMin) / yRange).toFloat()
                    val y = tp + chartH * (1f - ratio)
                    if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
                }

                // 1) 라인
                drawPath(
                    path = path,
                    color = lineColor,
                    style = Stroke(width = 3f)
                )

                // 2) 날짜 라벨(아래)
                entries.forEachIndexed { i, e ->
                    val x = pointX(i)
                    val paint = android.graphics.Paint().apply {
                        color = android.graphics.Color.argb(
                            (axisLabelColor.alpha * 255).toInt(),
                            (axisLabelColor.red * 255).toInt(),
                            (axisLabelColor.green * 255).toInt(),
                            (axisLabelColor.blue * 255).toInt()
                        )
                        textSize = labelPx
                        isAntiAlias = true
                    }
                    val tw = paint.measureText(e.label)
                    drawContext.canvas.nativeCanvas.drawText(
                        e.label,
                        x - tw / 2f,
                        h - with(density) { dateYOffset.toPx() },
                        paint
                    )
                }

                // 3) 값 라벨 — 포인트 “위” 둥근 사각형(꼬리 없음)
                entries.forEachIndexed { i, e ->
                    val x = pointX(i)
                    val ratio = ((e.value - yMin) / yRange).toFloat()
                    val pointY = tp + chartH * (1f - ratio)

                    val valueText = formatValue(e.value)
                    val textPaint = android.graphics.Paint().apply {
                        color = android.graphics.Color.WHITE
                        textSize = labelPx
                        isAntiAlias = true
                    }
                    val textW = textPaint.measureText(valueText)
                    val textH = textPaint.fontMetrics.let { it.descent - it.ascent }

                    val bubblePadH = with(density) { bubbleHPad.toPx() }
                    val bubblePadV = with(density) { bubbleVPad.toPx() }
                    val bubbleW = textW + bubblePadH * 2
                    val bubbleH = textH + bubblePadV * 2

                    val bubbleX = (x - bubbleW / 2f).coerceIn(lp, w - rp - bubbleW)
                    val desiredTop = pointY
                    - with(density) { bubbleOffsetAbovePoint.toPx() }
                    - with(density) { minGapFromPoint.toPx() }
                    - bubbleH
                    val bubbleY = desiredTop.coerceAtLeast(tp + 2f)

                    // 둥근 사각형 라벨
                    drawRoundRect(
                        color = bubbleColor,
                        topLeft = Offset(bubbleX, bubbleY),
                        size = Size(bubbleW, bubbleH),
                        cornerRadius = CornerRadius(with(density) { bubbleCorner.toPx() })
                    )

                    // 값 텍스트 (중앙)
                    drawContext.canvas.nativeCanvas.drawText(
                        valueText,
                        bubbleX + (bubbleW - textW) / 2f,
                        bubbleY + bubblePadV - textPaint.fontMetrics.ascent,
                        textPaint
                    )
                }

                // 4) 포인트 — halo → ring → center (가장 위에)
                entries.forEachIndexed { i, e ->
                    val x = pointX(i)
                    val ratio = ((e.value - yMin) / yRange).toFloat()
                    val y = tp + chartH * (1f - ratio)

                    // Halo
                    drawCircle(
                        color = lineColor.copy(alpha = 0.18f),
                        radius = haloRadius,
                        center = Offset(x, y)
                    )
                    // Ring
                    drawCircle(
                        color = lineColor,
                        radius = ringRadius,
                        center = Offset(x, y),
                        style = androidx.compose.ui.graphics.drawscope.Stroke(width = ringStroke)
                    )
                    // Center
                    drawCircle(
                        color = pointColor,
                        radius = centerRadius,
                        center = Offset(x, y)
                    )
                }
            }
        }
    }
}

private fun formatValue(v: Double): String {
    if (v.isNaN() || !v.isFinite()) return "0"
    val abs = kotlin.math.abs(v)
    return when {
        abs >= 1_000_000 -> String.format("%.1fm", v / 1_000_000)
        abs >= 1_000     -> String.format("%.1fk", v / 1_000)
        abs >= 100       -> v.toInt().toString()
        else             -> String.format("%.1f", v)
    }
}
