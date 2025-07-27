package com.nhj.fitlog.presentation.exercise.history

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.nhj.fitlog.component.FitLogTopBar
import com.nhj.fitlog.presentation.exercise.history.component.ExerciseHistoryItem

@Composable
fun ExerciseHistoryScreen(
    viewModel: ExerciseHistoryViewModel = hiltViewModel(),
) {
    Scaffold(
        topBar = {
            FitLogTopBar(
                title = viewModel.exerciseName,
                onBackClick = { viewModel.onBackNavigation() }
            )
        },
        containerColor = Color(0xFF121212)
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(viewModel.historyList.size) { index ->
                    val item = viewModel.historyList[index]
                    ExerciseHistoryItem(
                        item = item,
                        onClick = { viewModel.onNavigateToHistoryDetail(item) }
                    )
                }
            }
        }
    }
}