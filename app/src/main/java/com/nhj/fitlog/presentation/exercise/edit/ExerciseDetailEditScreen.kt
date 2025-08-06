package com.nhj.fitlog.presentation.exercise.edit

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ModeEdit
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.nhj.fitlog.component.FitLogAlertDialog
import com.nhj.fitlog.component.FitLogButton
import com.nhj.fitlog.component.FitLogOutlinedTextField
import com.nhj.fitlog.component.FitLogTopBar
import com.nhj.fitlog.presentation.exercise.add.component.CategoryDropdownMenu
import com.nhj.fitlog.presentation.exercise.detail.ExerciseDetailViewModel

@Composable
fun ExerciseDetailEditScreen(
    id: String,
    name: String,
    category: String,
    memo: String,
    viewModel: ExerciseDetailEditViewModel = hiltViewModel(),
) {
    val focusManager = LocalFocusManager.current

    LaunchedEffect(Unit) {
        viewModel.originalName = name
        viewModel.exerciseId = id
        viewModel.exerciseName = name
        viewModel.exerciseCategory = category
        viewModel.exerciseMemo = memo
    }

    Scaffold(
        topBar = {
            FitLogTopBar(
                title = "운동 상세",
                onBackClick = { viewModel.onNavigateBack() },
                hasActionIcon = true,
                actionIcon = Icons.Default.Check,
                onActionClick = {
                    // 수정
                    viewModel.updateExerciseType()
                }
            )
        },
        containerColor = Color(0xFF121212)
    ) { innerPadding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
                .pointerInput(Unit) {
                    detectTapGestures(onTap = { focusManager.clearFocus() })
                },
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            FitLogOutlinedTextField(
                value = viewModel.exerciseName,
                onValueChange = { viewModel.exerciseName = it },
                label = "운동 이름",
            )

            CategoryDropdownMenu(
                options = viewModel.categoryOptions,
                selectedOption = viewModel.exerciseCategory,
                onOptionSelected = { viewModel.exerciseCategory = it },
                modifier = Modifier.fillMaxWidth(),
            )

            FitLogOutlinedTextField(
                value = viewModel.exerciseMemo,
                onValueChange = { viewModel.exerciseMemo = it },
                label = "기타 메모",
                singleLine = false,
            )

            // 빈 이름 오류
            if (viewModel.showNameBlankError) {
                FitLogAlertDialog(
                    title = "입력 오류",
                    message = "운동 이름을 입력해주세요.",
                    onConfirm = { viewModel.showNameBlankError = false },
                    onDismiss = {  },
                    showCancelButton = false
                )
            }

            // 중복 이름 오류
            if (viewModel.showNameDuplicateError) {
                FitLogAlertDialog(
                    title = "중복 오류",
                    message = "이미 같은 이름의 운동이 있습니다.",
                    onConfirm = { viewModel.showNameDuplicateError = false },
                    onDismiss = {  },
                    showCancelButton = false
                )
            }

            // 수정 확인 다이얼로그
            if (viewModel.showSaveConfirm) {
                FitLogAlertDialog(
                    title = "수정 완료",
                    message = "운동 정보가 정상적으로 수정되었습니다.",
                    onConfirm = { viewModel.onNavigateBack() },
                    onDismiss = {  },
                    showCancelButton = false
                )
            }

        }

    }

}