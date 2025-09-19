package com.nhj.fitlog.presentation.analysis.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import com.nhj.fitlog.component.FitLogText
import kotlin.math.roundToInt

/** 카테고리(한글) → 기본 컬러 매핑 (도트/스택 색) */
fun categoryBaseColor(name: String): Color = when (name) {
    "가슴" -> Color(0xFFFF6B6B) // CHEST
    "등"   -> Color(0xFF26A69A) // BACK
    "어깨" -> Color(0xFFFFB74D) // SHOULDER
    "하체" -> Color(0xFF66BB6A) // LEG
    "팔"   -> Color(0xFFBA68C8) // ARM
    "복부" -> Color(0xFF4DD0E1) // ABDOMEN
    "기타" -> Color(0xFF9E9E9E) // ETC
    else   -> Color(0xFF9E9E9E) // default
}

/**
 * 월별 볼륨 비율 가로 막대 + 하단 퍼센트 + 레전드(색 원 + 카테고리명)
 *
 * @param label "yyyy.MM"
 * @param parts (카테고리명, 볼륨) — 볼륨이 0 초과만 전달
 * @param total parts 합계
 * @param colorProvider 카테고리별 색상
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun MonthStackedBar(
    label: String,
    parts: List<Pair<String, Double>>,
    total: Double,
    colorProvider: (String) -> Color
) {
    Column(Modifier.fillMaxWidth()) {
        // ── 스택 바 ─────────────────────────────────────────────
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            val shape = RoundedCornerShape(8.dp)
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(18.dp)
                    .clip(shape)
                    .background(Color(0xFF1E1E1E))
            ) {
                Row(Modifier.fillMaxSize()) {
                    parts.forEach { (name, vol) ->
                        val weight = if (total > 0.0) vol.toFloat() else 0f
                        if (weight > 0f) {
                            Box(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .weight(weight)
                                    .background(colorProvider(name))
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.width(8.dp))
            Text(
                text = formatValue(total),
                color = Color(0xFFBDBDBD),
                style = MaterialTheme.typography.bodySmall
            )
        }

        // ── 라벨(색 원 + 카테고리명 + 퍼센트) ──────────────────
        Spacer(Modifier.height(30.dp))
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            parts.filter { it.second > 0.0 }.forEach { (name, vol) ->
                val pct = if (total > 0.0) (vol / total) * 100.0 else 0.0
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(15.dp)
                            .clip(CircleShape)
                            .background(colorProvider(name))
                    )
                    Spacer(Modifier.width(6.dp))
                    FitLogText(
                        text = "$name ${formatPercent(pct)}",
                        color = colorProvider(name),
                        fontSize = 15
                    )
                }
            }
        }
    }
}

private fun formatValue(v: Double): String {
    val abs = kotlin.math.abs(v)
    return when {
        abs >= 1_000_000 -> String.format("%.1fm", v / 1_000_000)
        abs >= 1_000     -> String.format("%.1fk", v / 1_000)
        abs >= 100       -> v.toInt().toString()
        else             -> String.format("%.1f", v)
    }
}

private fun formatPercent(pct: Double): String {
    return if (pct >= 10.0 || pct == 0.0) "${pct.roundToInt()}%"
    else String.format("%.1f%%", pct)
}
