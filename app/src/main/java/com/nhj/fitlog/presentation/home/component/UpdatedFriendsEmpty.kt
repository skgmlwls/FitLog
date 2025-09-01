package com.nhj.fitlog.presentation.home.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.PersonAddAlt1
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.nhj.fitlog.component.FitLogText

private val CardBg = Color(0xFF1E1E1E)
private val Subtle = Color(0xFFB5B5B5)
private val StoryRing = Color(0x3347A6FF)   // 파란 링의 옅은 버전
private val StoryInner = Color(0xFF2A2A2A)  // 안쪽 원 배경
private val Accent = Color(0xFF47A6FF)

/** ‘오늘 업데이트’가 비어있을 때 보여줄 카드 */
@Composable
fun UpdatedFriendsEmpty(
    onRefresh: () -> Unit,
    onGoFriends: () -> Unit
) {
    Column(Modifier.fillMaxWidth()) {
        FitLogText(text = "오늘 업데이트", color = Color.White, fontSize = 16)
        Spacer(Modifier.height(10.dp))

        Column(
            modifier = Modifier
                .fillMaxWidth()
        ) {
            // Placeholder 스토리들 (3개)
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                StoryAvatarPlaceholder()
                Spacer(Modifier.width(12.dp))
            }

            Spacer(Modifier.height(14.dp))

            FitLogText(
                text = "오늘 업데이트한 친구가 없어요",
                color = Subtle,
                fontSize = 13
            )
        }
    }
}

/** 둥근 파란 링을 흉내낸 플레이스홀더 아바타 */
@Composable
fun StoryAvatarPlaceholder(
    size: Dp = 64.dp,
    ringWidth: Dp = 3.dp
) {
    Box(
        modifier = Modifier
            .size(size)
            .border(BorderStroke(ringWidth, StoryInner), CircleShape)
            .padding(ringWidth)
            .clip(CircleShape)
            .background(StoryInner)
    )
}