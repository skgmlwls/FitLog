package com.nhj.fitlog.presentation.exercise.history.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nhj.fitlog.presentation.exercise.history.HistoryItem

@Composable
fun ExerciseHistoryItem(
    item: HistoryItem,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(50.dp)
            .background(color = Color(0xFF3C3C3C), shape = RoundedCornerShape(8.dp))
            .clickable { onClick() }, // ✅ 클릭 허용,
        contentAlignment = Alignment.CenterStart
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = item.date, color = Color.White, fontSize = 16.sp)
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .background(
                        color = when (item.color) {
                            "red" -> Color.Red
                            "blue" -> Color.Blue
                            "yellow" -> Color.Yellow
                            else -> Color.Gray
                        },
                        shape = RoundedCornerShape(50)
                    )
            )
        }
    }
}
