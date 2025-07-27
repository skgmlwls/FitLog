package com.nhj.fitlog.presentation.exercise.history_detail


import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.nhj.fitlog.component.FitLogOutlinedTextField
import com.nhj.fitlog.component.FitLogText
import com.nhj.fitlog.component.FitLogTopBar

@Composable
fun ExerciseHistoryDetailScreen(
    viewModel: ExerciseHistoryDetailViewModel = hiltViewModel()
) {
    Scaffold(
        topBar = {
            FitLogTopBar(
                title = viewModel.date,
                onBackClick = { viewModel.onBackNavigation() }
            )
        },
        containerColor = Color(0xFF121212)
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // 이름
            FitLogOutlinedTextField(
                value = viewModel.exerciseName,
                onValueChange = {},
                label = "이름",
                enabled = false
            )

            Spacer(modifier = Modifier.height(8.dp))

            // 강도
            FitLogText(
                text = "강도",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 20
            )
            Box(
                modifier = Modifier
                    .size(16.dp)
                    .background(
                        color = when (viewModel.intensityColor) {
                            "red" -> Color.Red
                            "blue" -> Color.Blue
                            "yellow" -> Color.Yellow
                            else -> Color.Gray
                        },
                        shape = RoundedCornerShape(50)
                    )
            )

            Spacer(modifier = Modifier.height(8.dp))

            FitLogText(
                text = "기록",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 20
            )

            // 세트별 입력
            viewModel.exerciseSets.forEachIndexed { index, set ->
                Text("${index + 1} Set", color = Color.White)

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FitLogOutlinedTextField(
                        value = set.weight,
                        onValueChange = {},
                        label = "무게 (kg)",
                        modifier = Modifier.weight(1f),
                        enabled = false
                    )
                    FitLogOutlinedTextField(
                        value = set.reps,
                        onValueChange = {},
                        label = "횟수 (개)",
                        modifier = Modifier.weight(1f),
                        enabled = false
                    )
                }
            }
        }
    }
}