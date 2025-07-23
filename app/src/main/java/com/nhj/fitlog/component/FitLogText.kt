package com.nhj.fitlog.component

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
import com.nhj.fitlog.ui.theme.NanumSquareRound

@Composable
fun FitLogText(
    text: String,
    fontSize: Int = 16, // ✅ Int로 받음
    fontWeight: FontWeight = FontWeight.Normal,
    color: Color = Color(0xFF47A6FF),
) {
    Text(
        text = text,
        color = color,
        fontSize = fontSize.sp, // ✅ 내부에서 sp로 변환
        fontWeight = fontWeight,
        fontFamily = NanumSquareRound,
        style = TextStyle()
    )
}