// HomeScreen.kt
package com.nhj.fitlog.presentation.home

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import androidx.hilt.navigation.compose.hiltViewModel
import com.nhj.fitlog.R
import com.nhj.fitlog.component.FitLogIconButton
import com.nhj.fitlog.component.FitLogText
import com.nhj.fitlog.presentation.home.component.AnalysisCallOutCard
import com.nhj.fitlog.presentation.home.component.FitLogHomeButton
import com.nhj.fitlog.presentation.home.component.StoryAvatar
import com.nhj.fitlog.presentation.home.component.UpdatedFriendsEmpty
import com.nhj.fitlog.utils.MainScreenName

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel = hiltViewModel()
) {
    val focusManager = LocalFocusManager.current

    LaunchedEffect(Unit) {
        viewModel.startUserListener()
        viewModel.startUpdatedFriendsListener()
    }

    Scaffold(containerColor = Color(0xFF121212)) { innerPadding ->

        PullToRefreshBox(
            isRefreshing = viewModel.isRefreshing.value,
            onRefresh = { viewModel.refreshUpdatedFriends() },        // 당겨서 새로고침 액션
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp)
                    .pointerInput(Unit) { detectTapGestures { focusManager.clearFocus() } },
                verticalArrangement = Arrangement.spacedBy(0.dp)
            ) {
                // ── TopBar 영역 ─────────────────────────────────────────
                item {
                    Box(modifier = Modifier.fillMaxWidth()) {
                        IconButton(
                            onClick = { viewModel.onNavigateToSettings() },
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(top = 16.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = "설정",
                                tint = Color.White
                            )
                        }
                        FitLogText(
                            text = "FITLOG",
                            color = Color(0xFF47A6FF),
                            fontSize = 40,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier
                                .align(Alignment.BottomStart)
                                .padding(top = 16.dp)
                        )
                        Spacer(Modifier.height(10.dp))
                    }
                }

                // ── 오늘 업데이트 스토리 행 ─────────────────────────────
                if (viewModel.updatedFriends.isNotEmpty()) {
                    item {
                        FitLogText(text = "오늘 업데이트", color = Color.White, fontSize = 16)
                        Spacer(Modifier.height(10.dp))
                    }
                    item {
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            items(viewModel.updatedFriends, key = { it.friendUid }) { friend ->
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    StoryAvatar(
                                        url = friend.profileImageUrl,
                                        size = 64.dp,
                                        ringWidth = 3.dp,
                                        onClick = {
                                            viewModel.onNavigateToRecordCalendar(
                                                previousScreen = "FRIEND_AVATAR",      // 친구 아바타에서 왔음을 표시
                                                targetUid = friend.friendUid,          // 친구 UID
                                                targetNickname = friend.nickname       // 친구 닉네임
                                            )
                                        }
                                    )
                                    Spacer(Modifier.height(10.dp))
                                    FitLogText(
                                        text = friend.nickname.ifBlank { "(알 수 없음)" },
                                        color = Color.White,
                                        fontSize = 12,
                                        maxLines = 1,
                                    )
                                }
                            }
                        }
                        Spacer(Modifier.height(10.dp))
                    }
                } else {
                    // ✅ 빈 상태
                    item {
                        UpdatedFriendsEmpty(
                            onRefresh = { viewModel.refreshUpdatedFriends() },
                            onGoFriends = { viewModel.onNavigateToFriendsList() }
                        )
                        Spacer(Modifier.height(24.dp))
                    }
                }

                // ── 분석 화면 CTA ──────────────────────────────────────────────
                item {
                    AnalysisCallOutCard(
                        onClick = {
                            viewModel.onNavigateToAnalysis()
                        }
                    )
                    Spacer(Modifier.height(16.dp))
                }

                // ── 오늘 운동 기록하기 CTA ──────────────────────────────
                item {
                    FitLogIconButton(
                        text = "오늘 운동 기록하기",
                        icon = Icons.Default.AddCircleOutline,
                        onClick = { viewModel.onNavigateToRecordExerciseToday() },
                        color = Color(0xFF2C2C2C)
                    )
                    Spacer(Modifier.height(16.dp))
                }

                // ── 2x2 버튼 그리드 ─────────────────────────────────────
                item {
                    Column {
                        Row(modifier = Modifier.fillMaxWidth()) {
                            FitLogHomeButton(
                                iconVector = Icons.Default.Edit,
                                label = "운동 기록",
                                modifier = Modifier.weight(1f),
                                onClick = { viewModel.onNavigateToRecordCalendar(MainScreenName.MAIN_SCREEN_HOME.name) }
                            )
                            Spacer(Modifier.width(8.dp))
                            FitLogHomeButton(
                                iconVector = Icons.Default.Group,
                                label = "친구 목록",
                                modifier = Modifier.weight(1f),
                                onClick = { viewModel.onNavigateToFriendsList() }
                            )
                        }
                        Spacer(Modifier.height(8.dp))
                        Row(modifier = Modifier.fillMaxWidth()) {
                            FitLogHomeButton(
                                iconVector = Icons.Default.Cached,
                                label = "루틴 목록",
                                modifier = Modifier.weight(1f),
                                onClick = { viewModel.onNavigateToRoutineList() }
                            )
                            Spacer(Modifier.width(8.dp))
                            FitLogHomeButton(
                                iconPainter = painterResource(id = R.drawable.ic_dumbbell),
                                label = "운동 종류",
                                modifier = Modifier.weight(1f),
                                onClick = { viewModel.onNavigateToExerciseTypeScreen() }
                            )
                        }
                    }
                    Spacer(Modifier.height(16.dp))
                }

                item {
                    FitLogIconButton(
                        text = "AI 운동 코치",
                        icon = Icons.Default.ChecklistRtl,
                        onClick = { viewModel.onNavigateToCoach() }
                    )
                }
            }
        }
    }
}
