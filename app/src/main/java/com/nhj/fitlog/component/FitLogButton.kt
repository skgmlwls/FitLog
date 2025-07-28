package com.nhj.fitlog.component

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nhj.fitlog.ui.theme.NanumSquareRound

@Composable
fun FitLogButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,                         // ← 여기 추가
    backgroundColor: Color = Color(0xFF47A6FF),
    disabledBackgroundColor: Color = Color(0xFF444444), // 비활성 시 배경
    textColor: Color = Color.White,
    disabledTextColor: Color = Color.LightGray,        // 비활성 시 텍스트
    cornerRadius: Dp = 12.dp,
    horizontalPadding: Dp = 40.dp,
    fontSize: Int = 16
) {
    var isClickable by remember { mutableStateOf(true) }
    var trigger by remember { mutableStateOf(false) }

    if (trigger) {
        LaunchedEffect(trigger) {
            kotlinx.coroutines.delay(500)
            isClickable = true
            trigger = false
        }
    }

    Button(
        onClick = {
            if (isClickable) {
                isClickable = false
                trigger = true
                onClick()
            }
        },
        enabled = enabled && isClickable,               // ← enabled 반영
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = horizontalPadding),
        shape = RoundedCornerShape(cornerRadius),
        colors = ButtonDefaults.buttonColors(
            containerColor = backgroundColor,
            contentColor = textColor,
            disabledContainerColor = disabledBackgroundColor,
            disabledContentColor = disabledTextColor
        ),
        contentPadding = PaddingValues(0.dp)
    ) {
        Text(
            text = text,
            fontFamily = NanumSquareRound,
            fontSize = fontSize.sp,
            fontWeight = FontWeight.Bold
        )
    }
}