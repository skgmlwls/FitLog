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
    backgroundColor: Color = Color(0xFF47A6FF),
    textColor: Color = Color.White,
    cornerRadius: Dp = 12.dp,
    horizontalPadding: Dp = 40.dp, // ✅ 양옆 패딩 파라미터 추가
    fontSize : Int = 16,
) {

    // 클릭 가능 여부 상태 저장
    var isClickable by remember { mutableStateOf(true) }

    // 클릭 방지 타이머를 제어할 trigger 변수
    var trigger by remember { mutableStateOf(false) }

    // trigger가 true일 때만 실행되는 지연 효과 (0.5초 후 클릭 가능 상태로 복귀)
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
                isClickable = false       // 다시 클릭 막기
                trigger = true            // LaunchedEffect 작동시키기
                onClick()             // 뒤로가기 콜백 실행
            }
        },
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = horizontalPadding), // ✅ 적용
        shape = RoundedCornerShape(cornerRadius),
        colors = ButtonDefaults.buttonColors(
            containerColor = backgroundColor,
            contentColor = textColor
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