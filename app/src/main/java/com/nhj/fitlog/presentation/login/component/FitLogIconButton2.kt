package com.nhj.fitlog.presentation.login.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nhj.fitlog.ui.theme.NanumSquareRound

@Composable
fun FitLogIconButton2(
    text: String,
    icon: Painter, // ✅ ImageVector → Painter
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    containerColor: Color = Color.LightGray,
    contentColor: Color = Color.Black,
    horizontalPadding: Dp = 40.dp,
    cornerRadius: Dp = 12.dp
) {
    Button(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = horizontalPadding),
        shape = RoundedCornerShape(cornerRadius),
        colors = ButtonDefaults.buttonColors(
            containerColor = containerColor,
            contentColor = contentColor
        ),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            // 아이콘 왼쪽 정렬
            Image(
                painter = icon,
                contentDescription = null,
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .size(20.dp)
            )

            // 텍스트 가운데 정렬
            Text(
                text = text,
                fontSize = 16.sp,
                fontFamily = NanumSquareRound,
                fontWeight = FontWeight.Bold,
                color = contentColor,
                modifier = Modifier.align(Alignment.Center)
            )
        }
    }
}