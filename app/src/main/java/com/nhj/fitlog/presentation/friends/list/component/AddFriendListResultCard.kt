package com.nhj.fitlog.presentation.friends.list.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.PersonAddAlt1
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.nhj.fitlog.ui.theme.NanumSquareRound

// AddFriendListResultCard.kt
@Composable
fun AddFriendListResultCard(
    nickname: String,
    profileImageUrl: String,
    isAdded: Boolean,
    isIncomingPending: Boolean,          // 내가 ‘받은’ 요청 (수락/거절 가능)
    isOutgoingPending: Boolean,          // 내가 ‘보낸’ 요청 (요청 보냄 배지)
    onAdd: () -> Unit,
    onAccept: () -> Unit,                // 수락
    onDecline: () -> Unit                // 거절
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 12.dp, vertical = 10.dp)
                .wrapContentHeight(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 아바타
            ProfileAvatar(url = profileImageUrl, size = 44.dp)

            Spacer(Modifier.width(12.dp))

            // 오른쪽: 이름(첫 줄) + 상태(둘째 줄)
            Column(modifier = Modifier.fillMaxWidth()) {
                // 1줄: 이름
                Text(
                    text = nickname.ifBlank { "(이름 없음)" },
                    color = Color.White,
                    fontWeight = FontWeight.SemiBold,
                    fontFamily = NanumSquareRound,
                    maxLines = 1
                )

                // 2줄: 상태 UI
                when {
                    isAdded -> {
                        AssistChip(
                            onClick = {},
                            label = { Text("추가됨", color = Color.White) },
                            colors = AssistChipDefaults.assistChipColors(
                                containerColor = Color(0xFF2A2A2A),
                                labelColor = Color.White
                            )
                        )
                    }
                    isIncomingPending -> {
                        // 상대가 나에게 요청 → 수락/거절
                        Row {
                            FilledIconButton(
                                onClick = onDecline,
                                modifier = Modifier.weight(1f).height(30.dp),
                                shape = RoundedCornerShape(8.dp),
                                colors = IconButtonDefaults.filledIconButtonColors(
                                    containerColor = Color(0xFF444444),
                                    contentColor = Color.White
                                )
                            ) { Text("거절") }

                            Spacer(Modifier.width(8.dp))

                            FilledIconButton(
                                onClick = onAccept,
                                modifier = Modifier.weight(1f).height(30.dp),
                                shape = RoundedCornerShape(8.dp),
                                colors = IconButtonDefaults.filledIconButtonColors(
                                    containerColor = Color(0xFF47A6FF),
                                    contentColor = Color.White
                                )
                            ) { Text("수락") }
                        }
                    }
                    isOutgoingPending -> {
                        // 내가 보낸 요청 → 요청 보냄
                        AssistChip(
                            onClick = {},
                            label = { Text("요청 보냄", color = Color.White) },
                            colors = AssistChipDefaults.assistChipColors(
                                containerColor = Color(0xFF2A2A2A),
                                labelColor = Color.White
                            )
                        )
                    }
                    else -> {
                        Row {
                            // 아무 관계 아님 → 추가
                            FilledIconButton(
                                onClick = onAdd,
                                modifier = Modifier.weight(1f).height(30.dp),
                                shape = RoundedCornerShape(8.dp),
                                colors = IconButtonDefaults.filledIconButtonColors(
                                    containerColor = Color(0xFF47A6FF),
                                    contentColor = Color.White
                                )
                            ) { Text("추가 하기") }
                        }
                    }
                }
            }
        }
    }
}
