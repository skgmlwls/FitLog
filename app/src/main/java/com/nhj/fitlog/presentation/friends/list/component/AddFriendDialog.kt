package com.nhj.fitlog.presentation.friends.list.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.navigation.compose.hiltViewModel
import com.nhj.fitlog.component.FitLogButton
import com.nhj.fitlog.component.FitLogText
import com.nhj.fitlog.presentation.friends.list.FriendsListViewModel
import com.nhj.fitlog.ui.theme.NanumSquareRound
import kotlinx.coroutines.delay

@Composable
fun AddFriendDialog(
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit,
    viewModel: FriendsListViewModel = hiltViewModel()
) {
    val focus = LocalFocusManager.current
    var keyword by remember { mutableStateOf("") }
    val results = viewModel.searchResults
    val loading by viewModel.searchLoading

    // ✅ 현재 친구 uid 집합
    val currentFriendUids by remember(viewModel.friends) {
        mutableStateOf(viewModel.friends.map { it.friendUid }.toSet())
    }

    // 입력 디바운스 후 검색
    LaunchedEffect(keyword) {
        delay(300)
        viewModel.searchUsers(keyword.trim())
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(dismissOnClickOutside = false),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(Color(0xFF1E1E1E))
                .padding(24.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Title
                FitLogText(
                    text = "친구 추가",
                    fontSize = 20,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )

                Spacer(Modifier.height(16.dp))

                // 입력 필드 (닉네임)
                TextField(
                    value = keyword,
                    onValueChange = { keyword = it },
                    singleLine = true,
                    placeholder = { Text("추가할 닉네임을 입력하세요", fontFamily = NanumSquareRound) },
                    shape = RoundedCornerShape(12.dp),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color(0xFF2A2A2A),
                        unfocusedContainerColor = Color(0xFF2A2A2A),
                        cursorColor = Color.White,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedPlaceholderColor = Color(0xFFB5B5B5),
                        unfocusedPlaceholderColor = Color(0xFFB5B5B5)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 56.dp)
                )

                Spacer(Modifier.height(16.dp))

                // 결과 영역
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 120.dp, max = 360.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF141414))
                        .padding(8.dp)
                ) {
                    when {
                        loading -> {
                            Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                                CircularProgressIndicator()
                            }
                        }
                        keyword.isNotBlank() && results.isEmpty() -> {
                            Spacer(Modifier.height(16.dp))
                            Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                                FitLogText(
                                    text = "검색 결과가 없습니다.",
                                    color = Color(0xFFB5B5B5),
                                    fontSize = 14
                                )
                            }
                        }
                        else -> {
                            LazyColumn(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {

                                items(results, key = { it.uid }) { user ->
                                    val isAdded = currentFriendUids.contains(user.uid)
                                    val isIncoming = viewModel.incomingRequestMap.containsKey(user.uid)         // 그쪽이 나에게 보냄
                                    val isOutgoing = viewModel.pendingOutgoingUids.contains(user.uid)           // 내가 그쪽에 보냄

                                    AddFriendListResultCard(
                                        nickname = user.nickname.ifBlank { "(이름 없음)" },
                                        profileImageUrl = user.profileImageUrl,
                                        // 상태 전달
                                        isAdded = isAdded,
                                        isIncomingPending = isIncoming,
                                        isOutgoingPending = isOutgoing,
                                        // 액션들
                                        onAdd = {
                                            val myUid = viewModel.application.userUid ?: return@AddFriendListResultCard
                                            viewModel.sendFriendRequest(myUid, user.uid)
                                            onDismiss()
                                        },
                                        onAccept = { viewModel.acceptIncoming(user.uid) },
                                        onDecline = { viewModel.declineIncoming(user.uid) },
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(Modifier.height(16.dp))

                // 하단 버튼 (닫기 전용)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(Modifier.weight(1f)) {
                        FitLogButton(
                            text = "닫기",
                            onClick = {
                                viewModel.clearSearchResults()
                                onDismiss()
                            },
                            textColor = Color.White,
                            backgroundColor = Color(0xFF444444),
                            horizontalPadding = 0.dp
                        )
                    }
                }
            }
        }
    }
}