package com.nhj.fitlog.presentation.routine.exercise_list

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.nhj.fitlog.component.FitLogTopBar
import androidx.compose.foundation.lazy.items   // 리스트 버전 임포트
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Card
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Text

@Composable
fun RoutineAddListScreen(
    viewModel: RoutineAddListViewModel = hiltViewModel()
) {
    LaunchedEffect(Unit) { viewModel.load() }

    Scaffold(
        containerColor = Color(0xFF121212),
        topBar = {
            FitLogTopBar(
                title = "운동 선택",
                onBackClick = { viewModel.application.navHostController.popBackStack() },
                hasActionIcon = true,
                actionIcon = Icons.Default.Add,
                onActionClick = { viewModel.onNavigateRoutineAdd() }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // ✅ 카테고리 칩
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(viewModel.categories) { cat ->
                    val selected = viewModel.selectedCategory.value == cat
                    FilterChip(
                        selected = selected,
                        onClick = { viewModel.selectedCategory.value = cat },
                        label = {
                            Text(cat)
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            containerColor = Color(0xFF3C3C3C),
                            selectedContainerColor = Color(0xFF47A6FF),
                            labelColor = Color.White,
                            selectedLabelColor = Color.White
                        )
                    )
                }
            }

            // ✅ 필터된 리스트
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(viewModel.filteredList(), key = { it.id }) { ex ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF3C3C3C)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { viewModel.selectAndBack(ex.id, ex.name, ex.category, ex.memo) }
                    ) {
                        Column(Modifier.padding(14.dp)) {
                            Text(ex.name, color = Color.White)
                            Text(ex.category, color = Color(0xFFBDBDBD))
                        }
                    }
                }
            }
        }
    }
}