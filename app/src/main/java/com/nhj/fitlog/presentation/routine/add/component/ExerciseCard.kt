package com.nhj.fitlog.presentation.routine.add.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SegmentedButtonDefaults.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.nhj.fitlog.component.FitLogText
import com.nhj.fitlog.presentation.routine.add.RoutineExerciseWithSets
import androidx.compose.material3.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import com.nhj.fitlog.component.FitLogButton
import com.nhj.fitlog.component.FitLogIconButton


/** 스샷과 동일한 레이아웃의 운동 카드 */
@Composable
fun ExerciseCard(
    index: Int,
    ui: RoutineExerciseWithSets,
    onAddSet: () -> Unit,
    onDeleteSet: (String) -> Unit,
    onWeightChange: (String, String) -> Unit,
    onRepsChange: (String, String) -> Unit,
    onDeleteExercise: () -> Unit,
    dragHandle: (Modifier) -> Modifier = { it } // 🔹 기본은 no-op
) {
    var menuExpanded by remember { mutableStateOf(false) }

    var weight by remember { mutableStateOf("") }

    OutlinedCard(colors = CardDefaults.outlinedCardColors(containerColor = Color(0xFF262626))) {
        Column(Modifier.padding(12.dp)) {
            // 헤더: 번호 + 운동명 + 메뉴
            Row(verticalAlignment = Alignment.CenterVertically) {
                FitLogText(
                    text = "$index. ${ui.exercise.exerciseName}",
                    color = Color.White,
                    fontSize = 18
                )
                Spacer(Modifier.weight(1f))


                Box {
                    IconButton(onClick = { menuExpanded = true }) {
                        Icon(Icons.Default.MoreVert, contentDescription = null, tint = Color.White)
                    }
                    DropdownMenu(
                        expanded = menuExpanded,
                        onDismissRequest = { menuExpanded = false },
                        modifier = Modifier.background(Color(0xFF323232))
                    ) {
                        DropdownMenuItem(
                            text = { Text("삭제", color = Color.White) },
                            onClick = {
                                menuExpanded = false
                                onDeleteExercise()
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("순서 변경", color = Color.White) },
                            onClick = {
                                menuExpanded = false
                                // 순서 변경 로직
                            }
                        )
                    }
                }
            }


            Spacer(Modifier.height(8.dp))

            // 테이블 헤더
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text("세트", color = Color.White, modifier = Modifier.width(44.dp), textAlign = TextAlign.Center)
                Text("무게 (kg)", color = Color.White, modifier = Modifier.weight(1f), textAlign = TextAlign.Center)
                Text("횟수", color = Color.White, modifier = Modifier.weight(1f), textAlign = TextAlign.Center)
                // 공간 확보를 위한 버튼 ( 실제로 화면에 보이지 않고 기능도 없음 )
                FilledTonalButton(
                    onClick = { /* 아무 동작 없음 */ },
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = Color(0xFF47A6FF),
                        contentColor = Color.White
                    ),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 8.dp),
                    modifier = Modifier.alpha(0f).height(1.dp), // ✅ 완전 투명
                    enabled = false                 // ✅ 클릭 비활성화
                ) {
                    Text("삭제")
                }
            }
            Spacer(Modifier.height(6.dp))

            // 세트 행
            ui.sets.forEach { set ->
                // 초기 표시 문자열 (0이면 빈칸)
                var weightText by rememberSaveable(set.setId) {
                    mutableStateOf(if (set.weight == 0.0) "" else set.weight.toString())
                }
                var repsText by rememberSaveable(set.setId) {
                    mutableStateOf(if (set.reps == 0) "" else set.reps.toString())
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // 세트 번호 (읽기 전용)
                    OutlinedTextField(
                        value = set.setNumber.toString(),
                        onValueChange = {},
                        readOnly = true,
                        singleLine = true,
                        modifier = Modifier.width(44.dp),
                        colors = TextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedContainerColor = Color(0xFF1E1E1E),
                            unfocusedContainerColor = Color(0xFF1E1E1E),
                            focusedIndicatorColor = Color(0xFF47A6FF),
                            unfocusedIndicatorColor = Color(0xFF3C3C3C),
                            cursorColor = Color.White
                        )
                    )
                    Spacer(Modifier.width(8.dp))

                    // 무게
                    OutlinedTextField(
                        value = weightText,
                        onValueChange = { new ->
                            // 숫자/소수점만 허용 (원하면 제한 제거 가능)
                            if (new.isEmpty() || new.matches(Regex("^\\d*(\\.\\d*)?$"))) {
                                weightText = new
                            }
                            onWeightChange(set.setId, weightText) // ViewModel에서 안전 파싱
                        },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                        colors = TextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedContainerColor = Color(0xFF1E1E1E),
                            unfocusedContainerColor = Color(0xFF1E1E1E),
                            focusedIndicatorColor = Color(0xFF47A6FF),
                            unfocusedIndicatorColor = Color(0xFF3C3C3C),
                            cursorColor = Color.White
                        )
                    )

                    Spacer(Modifier.width(8.dp))

                    // 횟수 (문자열 상태 유지, 포커스 아웃 시 커밋)
                    OutlinedTextField(
                        value = repsText,
                        onValueChange = { new ->
                            if (new.isEmpty() || new.matches(Regex("^\\d+$"))) {
                                repsText = new
                            }
                            onRepsChange(set.setId, repsText)
                        },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                        colors = TextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedContainerColor = Color(0xFF1E1E1E),
                            unfocusedContainerColor = Color(0xFF1E1E1E),
                            focusedIndicatorColor = Color(0xFF47A6FF),
                            unfocusedIndicatorColor = Color(0xFF3C3C3C),
                            cursorColor = Color.White
                        )
                    )
                    Spacer(Modifier.width(8.dp))

                    // 세트 삭제
                    FilledTonalButton(
                        onClick = { onDeleteSet(set.setId) },
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = Color(0xFFDB3F3F),
                            contentColor = Color.White
                        ),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 8.dp)
                    ) { Text("삭제") }
                }
            }

            Spacer(Modifier.height(8.dp))

            // 세트 추가
            Button(
                onClick = onAddSet,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF47A6FF), contentColor = Color.White),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("세트 추가")
            }

//            // 종목 추가
//            Button(
//                onClick = {
//                    onDeleteExercise()
//                },
//                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3c3c3c), contentColor = Color.White),
//                modifier = Modifier.fillMaxWidth()
//            ) {
//                Text("종목 삭제")
//            }
        }
    }
}

