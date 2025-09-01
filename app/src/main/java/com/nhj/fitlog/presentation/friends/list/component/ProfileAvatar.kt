package com.nhj.fitlog.presentation.friends.list.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.SubcomposeAsyncImage
import coil.compose.SubcomposeAsyncImageContent
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
import com.nhj.fitlog.R

private val Subtle = Color(0xFFB5B5B5)
@Composable
fun ProfileAvatar(
    url: String,
    size: Dp = 44.dp,
    onClick: (() -> Unit)? = null
) {
    // Lottie 로더 준비 (raw 리소스는 프로젝트에 추가해 주세요)
    // 예: app/src/main/res/raw/fitlog_loading_avatar.json
    val composition by rememberLottieComposition(
        LottieCompositionSpec.RawRes(R.raw.shimmer) // ← 파일명은 프로젝트에 맞게
    )
    val progress by animateLottieCompositionAsState(
        composition = composition,
        iterations = LottieConstants.IterateForever
    )

    if (url.isBlank()) {
        // URL 없으면 기본 아이콘
        Box(
            modifier = Modifier
                .size(size)
                .clip(CircleShape)
                .background(Color(0xFF2E2E2E))
                .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.Person, contentDescription = null, tint = Subtle)
        }
        return
    }

    // URL 있을 때: 로딩 중에는 Lottie, 성공 시 이미지, 실패 시 기본 아이콘
    Box(
        modifier = Modifier
            .size(size)
            .clip(CircleShape)
            .background(Color.DarkGray)
            .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier),
        contentAlignment = Alignment.Center
    ) {
        SubcomposeAsyncImage(
            model = url,
            contentDescription = "프로필 사진",
            modifier = Modifier
                .matchParentSize()
                .clip(CircleShape),
            contentScale = ContentScale.Crop,
            loading = {
                // 로딩 중에는 Lottie 애니메이션
                LottieAnimation(
                    composition = composition,
                    progress = { progress },
                    modifier = Modifier.matchParentSize()
                )
            },
            success = {
                SubcomposeAsyncImageContent() // 정상 표시
            },
            error = {
                // 에러 시 기본 아이콘
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = "기본 프로필 아이콘",
                    tint = Color.LightGray,
                    modifier = Modifier.size(size * 0.55f)
                )
            }
        )
    }
}