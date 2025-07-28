package com.nhj.fitlog.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import com.airbnb.lottie.compose.*
import com.nhj.fitlog.R

@Composable
fun LottieLoadingOverlay(
    isVisible: Boolean,
    modifier: Modifier = Modifier
) {
    if (!isVisible) return
    Box(
        modifier = modifier
            .background(Color.Black.copy(alpha = 0.5f))
            .pointerInput(Unit) {
                // 모든 터치 이벤트를 소비합니다.
                awaitPointerEventScope {
                    while (true) { awaitPointerEvent() }
                }
            },
        contentAlignment = Alignment.Center
    ) {
        val comp by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.dumbell_animation))
        LottieAnimation(comp, iterations = LottieConstants.IterateForever, modifier = Modifier.size(200.dp))
    }
}