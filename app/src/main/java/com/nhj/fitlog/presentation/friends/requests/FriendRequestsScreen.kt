package com.nhj.fitlog.presentation.friends.requests

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.nhj.fitlog.component.FitLogTopBar
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.ui.text.font.FontWeight
import com.nhj.fitlog.component.FitLogButton
import com.nhj.fitlog.component.FitLogText
import com.nhj.fitlog.presentation.friends.list.component.ProfileAvatar

@Composable
fun FriendRequestsScreen(
    vm: FriendRequestsViewModel = hiltViewModel()
) {
    LaunchedEffect(Unit) { vm.start() }

    Scaffold(
        topBar = {
            FitLogTopBar(
                title = "친구 요청",
                onBackClick = { vm.back() }
            )
        },
        containerColor = Color(0xFF121212)
    ) { inner ->
        if (vm.isLoading.value) {
            Box(Modifier.fillMaxSize().padding(inner), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .padding(inner)
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(
                    items = vm.requests,
                    key = { it.requestId }
                ) { req ->
                    Surface(
                        color = Color(0xFF1E1E1E),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Row(
                            Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // 왼쪽: 아바타
                            ProfileAvatar(url = req.profileImageUrl)

                            Spacer(Modifier.width(12.dp))

                            // 오른쪽: 이름(첫줄) + 버튼들(둘째줄)
                            Column(Modifier.weight(1f)) {
                                // 1줄: 이름
                                FitLogText(
                                    text = req.nickname.ifBlank { "(알 수 없음)" },
                                    color = Color.White,
                                    fontWeight = FontWeight.SemiBold,
                                    maxLines = 1
                                )

                                Spacer(Modifier.height(8.dp))

                                // 2줄: 수락/거절 버튼 (나란히)
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    FitLogButton(
                                        text = "거절",
                                        onClick = { vm.decline(req.requestId) },
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(30.dp),
                                        backgroundColor = Color(0xFF444444),
                                        textColor = Color.White,
                                        horizontalPadding = 0.dp  // ⬅ 내부 패딩 제거해서 꽉 차게
                                    )
                                    FitLogButton(
                                        text = "수락",
                                        onClick = { vm.accept(req.requestId, req.fromUid) },
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(30.dp),
                                        backgroundColor = Color(0xFF47A6FF),
                                        textColor = Color.White,
                                        horizontalPadding = 0.dp  // ⬅ 내부 패딩 제거해서 꽉 차게
                                    )
                                }
                            }
                        }
                    }
                }

            }
        }
    }
}