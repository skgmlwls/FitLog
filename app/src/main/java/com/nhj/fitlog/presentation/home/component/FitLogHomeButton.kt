package com.nhj.fitlog.presentation.home.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// 홈 화면에서 사용하는 2x2 대형 버튼 컴포저블
@Composable
fun FitLogHomeButton(
    iconVector: ImageVector? = null,   // 벡터 아이콘 (기본 Material Icons)
    iconPainter: Painter? = null,      // PNG 이미지 등 외부 아이콘
    label: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .height(120.dp)
            .background(Color(0xFF2C2C2C), shape = RoundedCornerShape(16.dp))
            .clickable { onClick() }
            .padding(0.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            when {
                iconVector != null -> {
                    Icon(
                        imageVector = iconVector,
                        contentDescription = label,
                        tint = Color.White,
                        modifier = Modifier.size(36.dp)
                    )
                }
                iconPainter != null -> {
                    Image(
                        painter = iconPainter,
                        contentDescription = label,
                        modifier = Modifier.size(36.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = label,
                color = Color.White,
                fontSize = 18.sp
            )
        }
    }
}