package com.nhj.fitlog.presentation.exercise.add

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.nhj.fitlog.component.FitLogButton
import com.nhj.fitlog.component.FitLogOutlinedTextField
import com.nhj.fitlog.component.FitLogTopBar
import com.nhj.fitlog.presentation.exercise.add.component.CategoryDropdownMenu

@Composable
fun ExerciseAddScreen(
    viewModel: ExerciseAddViewModel = hiltViewModel()
) {
    val focusManager = LocalFocusManager.current

    Scaffold(
        topBar = {
            FitLogTopBar(
                title = "운동 추가",
                onBackClick = { viewModel.onBackNavigation() }
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
                modifier = Modifier.fillMaxWidth()
            )

            FitLogOutlinedTextField(
                value = viewModel.exerciseMemo,
                onValueChange = { viewModel.exerciseMemo = it },
                label = "기타 메모",
                singleLine = false
            )

            FitLogButton(
                text = "저장",
                onClick = { viewModel.onNavigateExerciseType() },
                horizontalPadding = 0.dp,
            )
        }
    }
}