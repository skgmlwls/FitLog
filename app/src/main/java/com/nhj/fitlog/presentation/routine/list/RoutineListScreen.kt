package com.nhj.fitlog.presentation.routine.list


import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.nhj.fitlog.component.FitLogTopBar
import com.nhj.fitlog.presentation.routine.list.component.RoutineItem

@Composable
fun RoutineListScreen(
    viewModel: RoutineListViewModel = hiltViewModel()
) {
    // Firestore 루틴 목록 구독 시작
    LaunchedEffect(Unit) { viewModel.startRoutineListener() }

    Scaffold(
        containerColor = Color(0xFF121212),
        topBar = {
            FitLogTopBar(
                title = "루틴 목록",
                onBackClick = { viewModel.onBackNavigation() },
                hasActionIcon = true,
                actionIcon = Icons.Default.Add,
                onActionClick = { viewModel.onNavigateRoutineAdd() }
            )
        },
    ) { padding ->

        Spacer(Modifier.height(18.dp))

        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(viewModel.routines.value, key = { it.routineId }) { routine ->
                RoutineItem(
                    routine = routine,
                    onClick = { viewModel.onNavigateRoutineDetail(routine.routineId) },
                    viewModel = viewModel
                )
            }
            item { Spacer(Modifier.height(80.dp)) } // FAB 가림 방지
        }
    }
}