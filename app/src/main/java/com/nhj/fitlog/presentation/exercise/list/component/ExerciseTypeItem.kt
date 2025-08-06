package com.nhj.fitlog.presentation.exercise.list.component

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nhj.fitlog.domain.model.ExerciseTypeModel
import com.nhj.fitlog.domain.vo.ExerciseTypeVO
import com.nhj.fitlog.presentation.exercise.list.ExerciseTypeViewModel

@Composable
fun ExerciseTypeItem(
    exercise: ExerciseTypeModel,
    onClick: () -> Unit,
    viewModel: ExerciseTypeViewModel
) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                Log.d("ExerciseTypeScreen", "${exercise.name} 클릭됨")
            },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF3C3C3C)
        ),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = exercise.name, fontSize = 18.sp, color = Color.White)
                Text(text = exercise.category, fontSize = 14.sp, color = Color.White)
            }

            Box {
                IconButton(onClick = { expanded = true }) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "메뉴",
                        tint = Color.White
                    )
                }

                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false },
                    modifier = Modifier
                        .background(Color(0xFF323232))
                ) {
//                    DropdownMenuItem(
//                        text = {
//                            Text("수정", color = Color.White)
//                        },
//                        onClick = {
//                            expanded = false
//                            Log.d("ExerciseTypeScreen", "${exercise.name} 수정 클릭됨")
//                        }
//                    )
                    DropdownMenuItem(
                        text = {
                            Text("삭제", color = Color.White)
                        },
                        onClick = {
                            expanded = false
                            viewModel.deleteExercise(exercise.id)
                        }
                    )
                }
            }
        }
    }
}
