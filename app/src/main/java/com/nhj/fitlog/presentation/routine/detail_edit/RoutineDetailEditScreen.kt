package com.nhj.fitlog.presentation.routine.detail_edit

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.nhj.fitlog.component.*
import com.nhj.fitlog.presentation.routine.add.component.ExerciseCard
import com.nhj.fitlog.presentation.routine.add.component.RoutineReorderBottomSheet

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoutineDetailEditScreen(
    routineId: String? = null,
    viewModel: RoutineDetailEditViewModel = hiltViewModel()
) {
    // ✅ 최초 1회만 서버 로드 + 복귀 때는 선택값만 consume
    LaunchedEffect(Unit) {
        viewModel.loadIfNeeded(force = false, routineId = routineId)
    }
    // ✅ 돌아올 때 선택값 반영만
    LaunchedEffect(Unit) {
        viewModel.consumeSelectedExerciseIfExists()
    }

    val focus = LocalFocusManager.current
    var showReorderSheet by rememberSaveable { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val loading = viewModel.loading.value

    Scaffold(
        containerColor = Color(0xFF121212),
        topBar = {
            FitLogTopBar(
                title = "루틴 수정",
                hasActionIcon = true,
                actionIcon = Icons.Default.Save,
                onBackClick = { viewModel.onBack() },
                onActionClick = { viewModel.save() }
            )
        }
    ) { padding ->
        Box(Modifier.padding(padding).fillMaxSize()) {

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp)
                    .verticalScroll(rememberScrollState())
                    .imePadding()
                    .pointerInput(Unit) { detectTapGestures { focus.clearFocus() } }
            ) {
                Spacer(Modifier.height(12.dp))
                FitLogText("루틴 이름", 20, color = Color.White)
                Spacer(Modifier.height(8.dp))
                FitLogOutlinedTextField(
                    value = viewModel.name.value,
                    onValueChange = { viewModel.name.value = it },
                    label = "루틴 이름"
                )
                Spacer(Modifier.height(14.dp))
                FitLogOutlinedTextField(
                    value = viewModel.memo.value,
                    onValueChange = { viewModel.memo.value = it },
                    label = "루틴 메모 (선택)",
                    singleLine = false
                )

                Spacer(Modifier.height(24.dp))
                FitLogText("운동", 20, color = Color.White)
                Spacer(Modifier.height(10.dp))

                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    viewModel.items.value.forEachIndexed { idx, exUi ->
                        ExerciseCard(
                            index = idx + 1,
                            ui = com.nhj.fitlog.presentation.routine.add.RoutineExerciseWithSets(
                                exercise = exUi.exercise,
                                sets = exUi.sets
                            ),
                            onAddSet = { viewModel.addSet(exUi.exercise.itemId) },
                            onDeleteSet = { setId -> viewModel.removeSet(exUi.exercise.itemId, setId) },
                            onWeightChange = { setId, v -> viewModel.updateSetWeight(exUi.exercise.itemId, setId, v) },
                            onRepsChange = { setId, v -> viewModel.updateSetReps(exUi.exercise.itemId, setId, v) },
                            onDeleteExercise = { viewModel.removeExercise(exUi.exercise.itemId) },
                            onReorderClick = { showReorderSheet = true }
                        )
                    }
                    Spacer(Modifier.height(8.dp))
                }

                // ✅ 하단 “운동 추가” 버튼
                FitLogIconButton(
                    text = "",
                    icon = Icons.Default.Add,
                    onClick = { viewModel.onNavigateToRoutineAddList() },
                    color = Color(0xFF3C3C3C)
                )

                Spacer(Modifier.height(80.dp))
            }

            // 로딩 오버레이
            LottieLoadingOverlay(
                isVisible = loading,
                modifier = Modifier.fillMaxSize()
            )
        }
    }

    if (showReorderSheet) {
        RoutineReorderBottomSheet(
            sheetState = sheetState,
            items = viewModel.items.value.map { it.exercise.itemId to it.exercise.exerciseName },
            onSave = { order -> viewModel.applyReorder(order); showReorderSheet = false },
            onDismiss = { showReorderSheet = false }
        )
    }

    if (viewModel.showNameBlankError.value) {
        FitLogAlertDialog(
            title = "입력 오류",
            message = "루틴 이름을 입력해주세요.",
            onConfirm = { viewModel.showNameBlankError.value = false },
            onDismiss = { },
            showCancelButton = false
        )
    }
    if (viewModel.showDuplicateNameError.value) {
        FitLogAlertDialog(
            title = "중복 오류",
            message = "이미 같은 이름의 루틴이 있습니다.",
            onConfirm = { viewModel.showDuplicateNameError.value = false },
            onDismiss = { },
            showCancelButton = false
        )
    }
}