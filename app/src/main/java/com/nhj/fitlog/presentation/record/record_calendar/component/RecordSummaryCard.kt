package com.nhj.fitlog.presentation.record.record_calendar.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccessTime
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.nhj.fitlog.component.FitLogText
import com.nhj.fitlog.utils.RecordIntensity
import java.text.DecimalFormat

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun RecordSummaryCard(
    recordId: String,
    memo: String,
    timeText: String,
    categoriesWithVol: List<Pair<String, Double>>,
    intensity: RecordIntensity,
    modifier: Modifier = Modifier,
    accent: Color = Color(0xFF47A6FF),
    onClick: (() -> Unit)? = null
) {
    val shape = RoundedCornerShape(18.dp)
    val bgGrad = Brush.verticalGradient(listOf(Color(0xFF1F1F1F), Color(0xFF181818)))

    val timeChipContainer = Color(0xFF2A2A2A)
    val timeChipLabel = Color.White
    val catChipLabel = Color.White

    Card(
        modifier = modifier.fillMaxWidth(),
        // Card 자체 onClick을 쓰지 않고(자식이 가로채는 경우 회피),
        // 내부에 전체 오버레이 clickable 박스를 얹을 거라서 여기선 non-clickable
        shape = shape,
        colors = CardDefaults.cardColors(containerColor = Color(0xFF181818)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = CardDefaults.outlinedCardBorder(true)
    ) {
        Box(Modifier.fillMaxWidth()) {

            // ---- 원래 카드 내용 ----
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 84.dp)
                    .background(bgGrad)
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 좌측 포인트 바
                Box(
                    Modifier
                        .width(4.dp)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(2.dp))
                        .background(accent.copy(alpha = 0.9f))
                )

                Column(Modifier.weight(1f)) {
                    // 상단: 좌측 시간칩 / 우측 강도칩
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        AssistChip(
                            onClick = { /* no-op */ },
                            enabled = false,
                            label = { FitLogText(timeText, color = Color.White) },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Outlined.AccessTime,
                                    contentDescription = null,
                                    tint = Color.White.copy(alpha = 0.9f)
                                )
                            },
                            colors = AssistChipDefaults.assistChipColors(
                                containerColor = timeChipContainer,
                                labelColor = timeChipLabel,
                                disabledContainerColor = timeChipContainer,
                                disabledLabelColor = timeChipLabel
                            )
                        )
                        Spacer(Modifier.weight(1f))

                        AssistChip(
                            onClick = { /* no-op */ },
                            enabled = false,
                            label = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        Modifier
                                            .size(8.dp)
                                            .clip(CircleShape)
                                            .background(intensityColor(intensity))
                                    )
                                    Spacer(Modifier.width(6.dp))
                                    FitLogText(intensity.name, color = Color.White)
                                }
                            },
                            colors = AssistChipDefaults.assistChipColors(
                                containerColor = timeChipContainer,
                                labelColor = timeChipLabel,
                                disabledContainerColor = timeChipContainer,
                                disabledLabelColor = timeChipLabel
                            )
                        )
                    }

                    // 카테고리 칩들
                    if (categoriesWithVol.isNotEmpty()) {
                        Spacer(Modifier.height(10.dp))
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            categoriesWithVol.forEach { (cat, vol) ->
                                if (vol > 0.0) {
                                    val base = categoryBaseColor(cat)
                                    val container = base.copy(alpha = 0.18f)
                                    AssistChip(
                                        onClick = { /* no-op */ },
                                        enabled = false,
                                        label = {
                                            Column(
                                                horizontalAlignment = Alignment.Start,
                                                modifier = Modifier.padding(vertical = 5.dp)
                                            ) {
                                                FitLogText(text = cat, color = Color.White)
                                                Spacer(Modifier.height(4.dp))
                                                FitLogText(
                                                    text = "${formatVolume(vol)} KG",
                                                    color = Color(0xFFB0B0B0),
                                                    fontSize = 14
                                                )
                                            }
                                        },
                                        colors = AssistChipDefaults.assistChipColors(
                                            containerColor = container,
                                            labelColor = catChipLabel,
                                            disabledContainerColor = container,
                                            disabledLabelColor = catChipLabel
                                        )
                                    )
                                }
                            }
                        }
                    } else {
                        Spacer(Modifier.height(10.dp))
                        AssistChip(
                            onClick = { /* no-op */ },
                            enabled = false,
                            label = { FitLogText("카테고리 없음", color = Color.White) },
                            colors = AssistChipDefaults.assistChipColors(
                                containerColor = Color(0xFF262626),
                                labelColor = Color.White,
                                disabledContainerColor = Color(0xFF262626),
                                disabledLabelColor = Color.White
                            )
                        )
                    }
                }
            }

            // ---- 전체 클릭 오버레이(칩 위 클릭도 전부 카드 onClick으로 연결) ----
            if (onClick != null) {
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .padding(0.dp) // 필요시 터치 영역 조절
                        .background(Color.Transparent)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) { onClick() }
                )
            }
        }
    }
}


/** 강도 → 컬러 매핑 */
@Composable
private fun intensityColor(intensity: RecordIntensity): Color = when (intensity) {
    RecordIntensity.HARD   -> Color(0xFFFF4D4D)
    RecordIntensity.NORMAL -> Color(0xFF3D7DFF)
    RecordIntensity.EASY   -> Color(0xFFF8FF32)
}

/** 카테고리(한글) → 기본 컬러 매핑 (도트용 원색) */
private fun categoryBaseColor(name: String): Color = when (name) {
    "가슴" -> Color(0xFFFF6B6B) // CHEST
    "등"   -> Color(0xFF26A69A) // BACK
    "어깨" -> Color(0xFFFFB74D) // SHOULDER
    "하체" -> Color(0xFF66BB6A) // LEG
    "팔"   -> Color(0xFFBA68C8) // ARM
    "복부" -> Color(0xFF4DD0E1) // ABDOMEN
    "기타" -> Color(0xFF9E9E9E) // ETC
    else   -> Color(0xFF9E9E9E) // default = ETC
}

/** 볼륨 숫자 포맷 (정수면 소수점 제거, 천단위 콤마) */
private fun formatVolume(value: Double): String {
    val intLike = kotlin.math.abs(value - value.toLong()) < 1e-9
    return if (intLike) DecimalFormat("#,###").format(value.toLong())
    else DecimalFormat("#,###.#").format(value)
}