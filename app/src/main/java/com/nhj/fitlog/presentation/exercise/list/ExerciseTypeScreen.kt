package com.nhj.fitlog.presentation.exercise.list

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.nhj.fitlog.component.FitLogTopBar
import com.nhj.fitlog.presentation.exercise.list.component.ExerciseTypeItem

// 운동 타입 스크린
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExerciseTypeScreen(
    viewModel: ExerciseTypeViewModel = hiltViewModel()
) {
    LaunchedEffect(Unit) {
        viewModel.startExerciseListener()   // 초기 데이터 로드
    }

    val selectedCategory by viewModel.selectedCategory.collectAsState()
    val exerciseList by viewModel.exerciseList.collectAsState()

    Scaffold(
        containerColor = Color(0xFF121212),
        topBar = {
            FitLogTopBar(
                title = "운동 종류",
                onBackClick = { viewModel.onBackNavigation() },
                hasActionIcon = true,
                actionIcon = Icons.Default.Add,
                onActionClick = { viewModel.onNavigateExerciseAdd() }
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(start = 16.dp, end = 16.dp)
        ) {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(viewModel.categories) { category ->
                    FilterChip(
                        colors = FilterChipDefaults.filterChipColors(
                            containerColor = Color(0xFF3C3C3C),
                            selectedContainerColor = Color(0xFF47A6FF),
                            labelColor = Color.White,
                            selectedLabelColor = Color.White
                        ),
                        selected = selectedCategory == category,
                        onClick  = { viewModel.selectCategory(category) },
                        label = { Text(category) },
                        border = null // 테두리 제거
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(exerciseList) { exercise ->
                    ExerciseTypeItem(
                        exercise = exercise,
                        onClick = { viewModel.onNavigateExerciseDetail(exercise) },
                        viewModel
                    )
                }
            }
        }
    }
}
