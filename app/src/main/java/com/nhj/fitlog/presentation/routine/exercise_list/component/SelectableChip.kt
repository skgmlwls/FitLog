package com.nhj.fitlog.presentation.routine.exercise_list.component

import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

@Composable
fun SelectableChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = { Text(label) },
        colors = FilterChipDefaults.filterChipColors(
            containerColor = Color(0xFF3C3C3C),
            selectedContainerColor = Color(0xFF47A6FF),
            labelColor = Color.White,
            selectedLabelColor = Color.White
        )
    )
}