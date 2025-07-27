package com.nhj.fitlog.presentation.exercise.detail

import android.util.Log
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ModeEdit
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
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

@Composable
fun ExerciseDetailScreen(
    viewModel: ExerciseDetailViewModel = hiltViewModel()
) {
    val focusManager = LocalFocusManager.current

    // 수정 모드
    var isEditMode by remember { mutableStateOf(false) }

    // 다이얼로그 제어 상태
    var showEditConfirmDialog by remember { mutableStateOf(false) }
    var showSaveConfirmDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            FitLogTopBar(
                title = if (isEditMode) "상세 수정" else "운동 상세",
                onBackClick = { viewModel.onNavigateBack() },
                hasActionIcon = true,
                actionIcon = if (isEditMode) Icons.Default.Check else Icons.Default.ModeEdit,
                onActionClick = {
                    if (isEditMode) {
                        // 저장 버튼을 눌렀을 때
                        showSaveConfirmDialog = true
                    } else {
                        // 수정 버튼을 눌렀을 때
                        showEditConfirmDialog = true
                    }
                    // isEditMode = !isEditMode
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
                enabled = isEditMode
            )

            CategoryDropdownMenu(
                options = viewModel.categoryOptions,
                selectedOption = viewModel.exerciseCategory,
                onOptionSelected = { viewModel.exerciseCategory = it },
                modifier = Modifier.fillMaxWidth(),
                enabled = isEditMode
            )

            FitLogOutlinedTextField(
                value = viewModel.exerciseMemo,
                onValueChange = { viewModel.exerciseMemo = it },
                label = "기타 메모",
                singleLine = false,
                enabled = isEditMode
            )

            if (!isEditMode) {
                FitLogButton(
                    text = "기록 보기",
                    onClick = { viewModel.onNavigateToHistory() },
                    horizontalPadding = 0.dp,
                    backgroundColor = Color(0xFF3C3C3C),
                )
            }

            // ✅ 수정 다이얼로그
            if (showEditConfirmDialog) {
                FitLogAlertDialog(
                    title = "수정 시작",
                    message = "이 항목을 수정하시겠습니까?",
                    onConfirm = {
                        isEditMode = true
                        showEditConfirmDialog = false
                    },
                    onDismiss = { showEditConfirmDialog = false }
                )
            }

            // ✅ 저장 다이얼로그
            if (showSaveConfirmDialog) {
                FitLogAlertDialog(
                    title = "수정 저장",
                    message = "변경 내용을 저장하시겠습니까?",
                    onConfirm = {
                        // 저장 로직 실행 가능
                        // viewModel.save()
                        isEditMode = false
                        showSaveConfirmDialog = false
                    },
                    onDismiss = { showSaveConfirmDialog = false }
                )
            }

        }

    }

}