package com.nhj.fitlog.presentation.record.record_exercise

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.nhj.fitlog.component.FitLogButton
import com.nhj.fitlog.component.FitLogText
import com.nhj.fitlog.component.FitLogTopBar
import com.nhj.fitlog.presentation.routine.add.component.ExerciseCard
import com.nhj.fitlog.presentation.routine.add.component.RoutineReorderBottomSheet
import com.nhj.fitlog.utils.RoutineExerciseWithSets

val RecordBg = Color(0xFF121212)
val RecordCardBg = Color(0xFF1E1E1E)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecordExerciseScreen(
    dateArg: String?,                                // 캘린더에서 넘어온 "yyyy-MM-dd"
    viewModel: RecordExerciseViewModel = hiltViewModel()
) {
    // 초기 로딩 + 복귀 시 선택값 consume
    LaunchedEffect(Unit) {
        viewModel.init(dateArg)
        viewModel.consumeSelectedExerciseIfExists()
    }

    val focus = LocalFocusManager.current

    var showMenuFor by rememberSaveable { mutableStateOf<String?>(null) }

    // ▶ AssistChip 블록을 아래로 교체
    var expanded by remember { mutableStateOf(false) }

    // 바텀 시트 상태
    var showReorderSheet by rememberSaveable { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    Scaffold(
        containerColor = RecordBg,
        topBar = {
            FitLogTopBar(
                title = "운동 기록 하기",
                hasActionIcon = true,
                actionIcon = Icons.Default.Save,
                onBackClick = { viewModel.onBack() },
                onActionClick = { viewModel.onSave() }
            )
        }
    ) { padding ->
        Box(
            Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(RecordBg)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp)
                    .imePadding()
                    .pointerInput(Unit) { detectTapGestures { focus.clearFocus() } }
            ) {
                Spacer(Modifier.height(12.dp))

                // 날짜 + 새 루틴 버튼
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    FitLogText(text = viewModel.displayDate(viewModel.dateText.value), fontSize = 20, color = Color.White)
                    Spacer(Modifier.weight(1f))
                    AssistChip(
                        onClick = { viewModel.onMakeRoutineFromThis() },
                        label = {
                            Icon(
                                imageVector = Icons.Default.ArrowDropDown,
                                contentDescription = null,
                                tint = Color.White
                            )
                            FitLogText("루틴 불러오기", color = Color.White, fontSize = 14)
                        }
                    )
                }

                Spacer(Modifier.height(16.dp))

                // 사진 업로드 자리(플레이스홀더)
                Card(
                    modifier = Modifier.size(84.dp),
                    colors = CardDefaults.cardColors(containerColor = RecordCardBg),
                    shape = RoundedCornerShape(8.dp),
                    onClick = { viewModel.onPickImage() }
                ) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("+", color = Color.White, style = MaterialTheme.typography.titleLarge)
                    }
                }

                Spacer(Modifier.height(20.dp))

                // 강도 섹션
                FitLogText("강도", 16, color = Color.White)
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    viewModel.intensitiesColor.forEach { (intensity, dotColor) ->
                        FilterChip(
                            selected = viewModel.intensity.value == intensity,
                            onClick = { viewModel.setIntensity(intensity) },
                            label = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        Modifier
                                            .size(8.dp)
                                            .background(dotColor, RoundedCornerShape(50))
                                    )
                                    Spacer(Modifier.width(6.dp))
                                    Text(intensity.name)
                                }
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                containerColor = Color(0xFF3C3C3C),
                                selectedContainerColor = Color(0xFF47A6FF),
                                labelColor = Color.White,
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                }

                Spacer(Modifier.height(16.dp))

                // 운동 카드들 (요구: 기존 ExerciseCard 사용)
                viewModel.items.value.forEachIndexed { index, ui: RoutineExerciseWithSets ->
                    ExerciseCard(
                        index = index + 1,
                        ui = ui,
                        onAddSet = { viewModel.addSet(ui.exercise.itemId) },
                        onDeleteSet = { setId -> viewModel.removeSet(ui.exercise.itemId, setId) },
                        onWeightChange = { setId, v -> viewModel.updateSetWeight(ui.exercise.itemId, setId, v) },
                        onRepsChange = { setId, v -> viewModel.updateSetReps(ui.exercise.itemId, setId, v) },
                        onDeleteExercise = { viewModel.removeExercise(ui.exercise.itemId) },
                        onReorderClick = { showReorderSheet = true }
                    )
                    Spacer(Modifier.height(12.dp))
                }

                // 운동 추가 버튼 (+)
                FitLogButton(
                    text = "+",
                    onClick = { viewModel.onAddExerciseClick() },
                    backgroundColor = Color(0xFF3C3C3C),
                    horizontalPadding = 0.dp,
                    fontSize = 26
                )

                Spacer(Modifier.height(32.dp))
            }

            // 바텀시트: 운동 이름만 보여주고 드래그-드롭으로 순서 변경
            if (showReorderSheet) {
                RoutineReorderBottomSheet(
                    sheetState = sheetState,
                    items = viewModel.items.value.map { it.exercise.itemId to it.exercise.exerciseName },
                    onSave = { newOrderIds ->
                        viewModel.applyReorder(newOrderIds)
                        showReorderSheet = false
                    },
                    onDismiss = { showReorderSheet = false }
                )
            }

            if (viewModel.loading.value) {

            }
        }
    }
}