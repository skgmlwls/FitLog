package com.nhj.fitlog.presentation.record.record_detail.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.nhj.fitlog.component.FitLogText
import com.nhj.fitlog.utils.RoutineExerciseWithSets

@Composable
fun ReadonlyExerciseCard(
    index: Int,
    ui: RoutineExerciseWithSets
) {
    OutlinedCard(
        colors = CardDefaults.outlinedCardColors(containerColor = Color(0xFF262626))
    ) {
        Column(Modifier.padding(12.dp)) {
            // 헤더: 번호 + 운동명
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    FitLogText(
                        text = "$index. ${ui.exercise.exerciseName}",
                        color = Color.White,
                        fontSize = 20
                    )
                    Spacer(Modifier.height(5.dp))
                    FitLogText(
                        text = "       ${ui.exercise.exerciseCategory.ifBlank { "기타" }} 운동",
                        color = Color.LightGray,
                        fontSize = 12
                    )
                }
            }

            Spacer(Modifier.height(8.dp))

            // 테이블 헤더
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                ReadonlyHeaderCell(text = "세트",  modifier = Modifier.width(44.dp))
                ReadonlyHeaderCell(text = "무게 (kg)", modifier = Modifier.weight(1f))
                ReadonlyHeaderCell(text = "횟수",    modifier = Modifier.weight(1f))
                // 편집용 버튼 자리는 제거
            }
            Spacer(Modifier.height(6.dp))

            // 세트 행들 (읽기 전용)
            ui.sets.sortedBy { it.setNumber }.forEach { set ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    ReadonlyCell(text = set.setNumber.toString(), modifier = Modifier.width(44.dp))
                    Spacer(Modifier.width(8.dp))
                    ReadonlyCell(text = trimZero(set.weight), modifier = Modifier.weight(1f))
                    Spacer(Modifier.width(8.dp))
                    ReadonlyCell(text = set.reps.toString(), modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun ReadonlyHeaderCell(
    text: String,
    modifier: Modifier = Modifier
) {
    // 헤더는 배경만 살짝 다르게
    Surface(
        color = Color.Transparent,
        contentColor = Color.White,
        modifier = modifier
    ) {
        Box(
            modifier = Modifier
                .heightIn(min = 36.dp)
                .fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            Text(text)
        }
    }
}

@Composable
private fun ReadonlyCell(
    text: String,
    modifier: Modifier = Modifier
) {
    // 입력창 느낌을 주되 포커스/편집 불가능한 고정 셀
    Surface(
        color = Color(0xFF1E1E1E),
        contentColor = Color.White,
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, Color(0xFF3C3C3C)),
        modifier = modifier
            .heightIn(min = 44.dp)
            .fillMaxWidth()
    ) {
        Box(
            contentAlignment = Alignment.Center
        ) {
            Text(text)
        }
    }
}

// 이미 아래 존재하던 헬퍼 재사용
private fun trimZero(v: Double): String =
    if (kotlin.math.abs(v - v.toLong()) < 1e-9) v.toLong().toString() else v.toString()
