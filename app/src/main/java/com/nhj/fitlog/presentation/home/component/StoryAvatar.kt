package com.nhj.fitlog.presentation.home.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.nhj.fitlog.presentation.friends.list.component.ProfileAvatar

private val StoryBlue = Color(0xFF47A6FF)

@Composable
fun StoryAvatar(
    url: String,
    size: Dp = 64.dp,
    ringWidth: Dp = 3.dp,
    ringColor: Color = StoryBlue,
    onClick: (() -> Unit)? = null
) {
    // 바깥 원형 테두리 + 안쪽 실제 아바타
    Box(
        modifier = Modifier
            .size(size)
            .border(BorderStroke(ringWidth, ringColor), CircleShape),
        contentAlignment = Alignment.Center
    ) {
        ProfileAvatar(
            url = url,
            size = size - (ringWidth * 2),
            onClick = onClick
        )
    }
}