package com.nhj.fitlog.presentation.routine.detail

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowDropUp
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.nhj.fitlog.component.FitLogTopBar
import com.nhj.fitlog.component.LottieLoadingOverlay
import com.nhj.fitlog.presentation.routine.detail.component.ExerciseReadOnlyCard

@Composable
fun RoutineDetailScreen(
    routineId: String? = null,
    viewModel: RoutineDetailViewModel = hiltViewModel(),
) {
    LaunchedEffect(routineId) { viewModel.load(routineId) }

    val routine = viewModel.routine.value
    val items = viewModel.items.value
    val loading = viewModel.loading.value
    val error = viewModel.error.value

    // 메모 토글 상태
    var memoExpanded by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = Color(0xFF121212),
        topBar = {
            FitLogTopBar(
                title = routine?.name ?: "루틴 상세",
                onBackClick = { viewModel.onBack() },
                hasActionIcon = true,
                actionIcon = Icons.Default.Edit,
                onActionClick = {
                    routine?.routineId?.let { viewModel.onNavigateToEdit(it) }
                },
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            // 본문
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Spacer(Modifier.height(12.dp))

                when {
                    error != null -> {
                        Text("오류: $error", color = Color(0xFFFF8080))
                    }
                    routine == null && !loading -> {
                        Text("루틴 정보를 찾을 수 없습니다.", color = Color.White)
                    }
                    else -> {
                        if (!routine?.memo.isNullOrBlank()) {
                            // 제목 + 토글 아이콘
                            Row(
                                modifier = Modifier
                                    .wrapContentWidth()
                                    .wrapContentHeight()
                                    .clickable { memoExpanded = !memoExpanded },
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = if (memoExpanded) Icons.Default.ArrowDropUp else Icons.Default.ArrowDropDown,
                                    contentDescription = null,
                                    tint = Color.White,
                                )
                                Text(
                                    text = "메모",
                                    color = Color.White,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontSize = 20.sp
                                )
                            }

                            // 메모 내용 (펼쳐진 경우에만 표시)
                            if (memoExpanded) {
                                Spacer(Modifier.height(4.dp))
                                routine?.let {
                                    Text(
                                        text = it.memo,
                                        color = Color(0xFFDDDDDD),
                                        fontSize = 20.sp,
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .padding(start = 12.dp, end = 12.dp)
                                    )
                                }
                                Spacer(Modifier.height(16.dp))
                            }
                        }

                        Spacer(Modifier.height(16.dp))

                        items.forEachIndexed { idx, item ->
                            ExerciseReadOnlyCard(
                                index = idx + 1,
                                ui = item
                            )
                            Spacer(Modifier.height(12.dp))
                        }

                        Spacer(Modifier.height(80.dp))
                    }
                }
            }

            // 로딩 오버레이 (최상단)
            LottieLoadingOverlay(
                isVisible = loading,
                modifier = Modifier
                    .fillMaxSize()
                    .align(Alignment.Center)
            )
        }
    }
}