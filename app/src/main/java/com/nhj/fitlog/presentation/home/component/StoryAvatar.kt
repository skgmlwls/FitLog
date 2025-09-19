package com.nhj.fitlog.presentation.home.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
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
    onClick: (() -> Unit)? = null
) {
    val ring = Brush.linearGradient(listOf(Color(0xFF47A6FF), Color(0xFF7BC9FF)))
    Box(
        modifier = Modifier
            .size(size)
            .border(ringWidth, brush = ring, shape = CircleShape),
        contentAlignment = Alignment.Center
    ) {
        ProfileAvatar(
            url = url,
            size = size - (ringWidth * 2),
            onClick = onClick
        )
        // 액티브 도트 (우상단)
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .offset(x = 2.dp, y = (-2).dp)
                .size(9.dp)
                .clip(CircleShape)
                .background(Color(0xFF22C55E))
        )
    }
}