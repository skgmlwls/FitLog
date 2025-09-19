package com.nhj.fitlog.presentation.friends.list

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PeopleAlt
import androidx.compose.material.icons.filled.PersonAddAlt1
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.nhj.fitlog.component.FitLogAlertDialog
import com.nhj.fitlog.component.FitLogText
import com.nhj.fitlog.component.FitLogTopBar
import com.nhj.fitlog.data.service.FriendItem
import com.nhj.fitlog.presentation.friends.list.component.AddFriendDialog
import com.nhj.fitlog.presentation.friends.list.component.FriendRow
import com.nhj.fitlog.presentation.friends.list.component.ProfileAvatar
import com.nhj.fitlog.presentation.friends.list.component.SearchFriendListField

private val Bg = Color(0xFF121212)
private val Subtle = Color(0xFFB5B5B5)
@Composable
fun FriendsListScreen(
    viewModel: FriendsListViewModel = hiltViewModel()
) {
    val focus = LocalFocusManager.current

    var addFriendDialogState by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) { viewModel.start() }

    Scaffold(
        topBar = {
            FitLogTopBar(
                title = "친구 목록",
                onBackClick = { viewModel.onBack() },
                actionsContent = {
                    // 1) 친구 추가 다이얼로그 열기
                    IconButton(
                        onClick = {
                            addFriendDialogState = true
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.PersonAddAlt1, // 기존과 동일 or Add 아이콘
                            contentDescription = "친구 추가",
                            tint = Color.White
                        )
                    }
                    // 2) 친구 요청 목록으로 이동
                    IconButton(
                        onClick = {
                            viewModel.onNavigateToFriendRequests()
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.PeopleAlt, // 메일/인박스 아이콘으로 바꿔도 됨
                            contentDescription = "친구 요청 목록",
                            tint = Color.White
                        )
                    }
                }
            )
        },
        containerColor = Bg
    ) { inner ->
        Column(
            modifier = Modifier
                .padding(inner)
                .fillMaxSize()
                .background(Bg)
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .pointerInput(Unit) {
                    detectTapGestures { focus.clearFocus() }
                }
        ) {
            // 친구 목록 중에 검색 필드
            SearchFriendListField(
                value = viewModel.searchText.value,
                onValueChange = { viewModel.searchText.value = it }
            )

            Spacer(Modifier.height(12.dp))

            if (viewModel.isLoading.value) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                val items = viewModel.filtered()
                if (items.isEmpty()) {
                    // 추가된 친구가 없을 경우
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Bg),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("친구가 없습니다. 우측 상단 + 로 추가하세요.", color = Subtle)
                    }
                } else {
                    // 추가된 친구 목록
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(items, key = { it.docId }) { friend ->
                            FriendRow(
                                friendItem = friend,
                                onDelete = {
                                    // ✅ 실제 삭제 호출
                                    viewModel.deleteFriend(friend.docId)
                                },
                                onClick = {
                                    if (!friend.recordPublic && !friend.picturePublic) {
                                        viewModel.showPublicNoneDialog.value = true
                                    } else {
                                        viewModel.onNavigateToRecord(friend.friendUid, friend.nickname)
                                    }
                                }
                            )
                        }
                    }
                }
            }

            viewModel.error.value?.let { msg ->
                Spacer(Modifier.height(8.dp))
                AssistChip(
                    onClick = {  },
                    label = { Text(text = msg) },
                    colors = AssistChipDefaults.assistChipColors(
                        labelColor = Color.Red,
                        leadingIconContentColor = Color.Red
                    )
                )
            }
        }

        if (addFriendDialogState) {
            // 친구 추가 다이얼로그
            AddFriendDialog(
                onDismiss = { addFriendDialogState = false },
                onConfirm = { uid ->
                    addFriendDialogState = false
                    viewModel.addFriend(viewModel.application.userUid, uid)
                }
            )
        }
        
        if (viewModel.showPublicNoneDialog.value) {
            FitLogAlertDialog(
                title = "열람 불가",
                message = "해당 친구는 기록과 사진을 비공개로 설정했습니다.",
                onConfirm = { viewModel.showPublicNoneDialog.value = false },
                onDismiss = {  },
                showCancelButton = false,
            )
        }

    }
}