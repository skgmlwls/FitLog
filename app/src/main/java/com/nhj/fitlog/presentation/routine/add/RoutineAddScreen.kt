package com.nhj.fitlog.presentation.routine.add

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.nhj.fitlog.component.*
import com.nhj.fitlog.presentation.routine.add.component.ExerciseCard

@Composable
fun RoutineAddScreen(
    viewModel: RoutineAddViewModel = hiltViewModel()
) {
    val scrollState = rememberScrollState()
    val focusManager = LocalFocusManager.current

    LaunchedEffect(Unit) { viewModel.consumeSelectedExerciseIfExists() }

    Scaffold(
        containerColor = Color(0xFF121212),
        topBar = {
            FitLogTopBar(
                title = "루틴",
                hasActionIcon = true,
                actionIcon = Icons.Default.Save,
                onBackClick = { viewModel.application.navHostController.popBackStack() },
                onActionClick = { /* TODO: 저장 연결 (루틴/운동/세트 저장) */ }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(horizontal = 16.dp)
                .verticalScroll(scrollState) // 전체 화면 스크롤
                // ✅ 키보드 올라오면 그만큼 패딩
                .imePadding()
                .pointerInput(Unit) {
                    detectTapGestures(onTap = { focusManager.clearFocus() })
                },
        ) {
            // 루틴 이름
            FitLogText(text = "루틴 이름", fontSize = 20, color = Color.White)
            Spacer(Modifier.height(10.dp))
            FitLogOutlinedTextField(
                value = viewModel.name.value,
                onValueChange = { viewModel.name.value = it },
                label = "루틴 이름"
            )

            Spacer(Modifier.height(24.dp))
            FitLogText(text = "운동", fontSize = 20, color = Color.White)
            Spacer(Modifier.height(10.dp))

            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                viewModel.routineExerciseList.value.forEachIndexed { idx, exUi ->
                    ExerciseCard(
                        index = idx + 1,
                        ui = exUi,
                        onAddSet = { viewModel.addSet(exUi.exercise.itemId) },
                        onDeleteSet = { setId -> viewModel.removeSet(exUi.exercise.itemId, setId) },
                        onWeightChange = { setId, v -> viewModel.updateSetWeight(exUi.exercise.itemId, setId, v) },
                        onRepsChange = { setId, v -> viewModel.updateSetReps(exUi.exercise.itemId, setId, v) },
                        onDeleteExercise = { viewModel.removeExercise(exUi.exercise.itemId) } // ✅ 추가
                    )
                }
                Spacer(Modifier.height(8.dp))
            }

            // 하단 큰 + 버튼 (운동 추가)
            FitLogIconButton(
                text = "",
                icon = Icons.Default.Add,
                onClick = { viewModel.onAddExerciseClick() },
                color = Color(0xFF3C3C3C)
            )
        }
    }
}