package com.nhj.fitlog.presentation.routine.detail.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.nhj.fitlog.component.FitLogText
import com.nhj.fitlog.presentation.routine.detail.RoutineDetailViewModel
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowDropUp
import com.nhj.fitlog.presentation.routine.detail.RoutineExerciseWithSets

@Composable
fun ExerciseReadOnlyCard(
    index: Int,
    ui: RoutineExerciseWithSets
) {
    val hasMemo = ui.exercise.exerciseMemo.isNotBlank()
    var showMemo by rememberSaveable(ui.exercise.itemId) { mutableStateOf(false) }

    OutlinedCard(colors = CardDefaults.outlinedCardColors(containerColor = Color(0xFF262626))) {
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
                        text = "       ${ui.exercise.exerciseCategory} 운동",
                        color = Color.LightGray,
                        fontSize = 12
                    )
                }
                // (읽기 전용: 메뉴 없음)
            }

            // 메모 토글
            if (hasMemo) {
                Spacer(Modifier.height(8.dp))
                Row(
                    modifier = Modifier
                        .wrapContentWidth()
                        .wrapContentHeight()
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) { showMemo = !showMemo },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (showMemo) Icons.Default.ArrowDropUp else Icons.Default.ArrowDropDown,
                        contentDescription = null,
                        tint = Color.White
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text = if (showMemo) "메모 접기" else "메모 보기",
                        color = Color.White
                    )
                }

                if (showMemo) {
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = ui.exercise.exerciseMemo,
                        color = Color.White,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 12.dp, end = 12.dp)
                    )
                }
            }

            Spacer(Modifier.height(8.dp))

            // 테이블 헤더
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text("세트", color = Color.White, modifier = Modifier.width(44.dp), textAlign = TextAlign.Center)
                Text("무게 (kg)", color = Color.White, modifier = Modifier.weight(1f), textAlign = TextAlign.Center)
                Text("횟수", color = Color.White, modifier = Modifier.weight(1f), textAlign = TextAlign.Center)
                // 정렬 맞춤용 더미 공간(삭제 버튼 자리)
                // Spacer(Modifier.width(44.dp))
            }
            Spacer(Modifier.height(6.dp))

            // 세트 행 (읽기 전용: TextField 비활성화)
            ui.sets.forEach { set ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // 세트 번호
                    OutlinedTextField(
                        value = set.setNumber.toString(),
                        onValueChange = {},
                        readOnly = true,
                        enabled = false,
                        singleLine = true,
                        modifier = Modifier.width(44.dp),
                        colors = TextFieldDefaults.colors(
                            disabledTextColor = Color.White,
                            disabledContainerColor = Color(0xFF1E1E1E),
                            disabledIndicatorColor = Color(0xFF3C3C3C),
                            cursorColor = Color.White
                        )
                    )
                    Spacer(Modifier.width(8.dp))

                    // 무게
                    OutlinedTextField(
                        value = if (set.weight == 0.0) "" else set.weight.toString(),
                        onValueChange = {},
                        readOnly = true,
                        enabled = false,
                        singleLine = true,
                        keyboardOptions = KeyboardOptions.Default,
                        modifier = Modifier.weight(1f),
                        colors = TextFieldDefaults.colors(
                            disabledTextColor = Color.White,
                            disabledContainerColor = Color(0xFF1E1E1E),
                            disabledIndicatorColor = Color(0xFF3C3C3C),
                            cursorColor = Color.White
                        )
                    )

                    Spacer(Modifier.width(8.dp))

                    // 횟수
                    OutlinedTextField(
                        value = if (set.reps == 0) "" else set.reps.toString(),
                        onValueChange = {},
                        readOnly = true,
                        enabled = false,
                        singleLine = true,
                        keyboardOptions = KeyboardOptions.Default,
                        modifier = Modifier.weight(1f),
                        colors = TextFieldDefaults.colors(
                            disabledTextColor = Color.White,
                            disabledContainerColor = Color(0xFF1E1E1E),
                            disabledIndicatorColor = Color(0xFF3C3C3C),
                            cursorColor = Color.White
                        )
                    )

                    Spacer(Modifier.width(8.dp))

                    // 읽기 전용: 삭제 버튼 숨김 대신 공간만 맞추고 싶으면 투명 버튼 사용 가능
                    // Spacer(Modifier.width(44.dp))
                }
            }
        }
    }
}