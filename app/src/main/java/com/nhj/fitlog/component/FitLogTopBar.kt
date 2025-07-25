package com.nhj.fitlog.component


import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.TextUnit
import com.nhj.fitlog.ui.theme.NanumSquareRound

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FitLogTopBar(
    title: String,
    onBackClick: () -> Unit,
    fontSize: TextUnit = 24.sp
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

    TopAppBar(
        title = {
            // 상단 타이틀 텍스트 설정
            Text(
                text = title,
                color = Color.White,
                fontSize = fontSize,
                fontFamily = NanumSquareRound
            )
        },
        navigationIcon = {
            // 뒤로가기 버튼 클릭 이벤트 처리
            IconButton(
                onClick = {
                if (isClickable) {
                    isClickable = false       // 다시 클릭 막기
                    trigger = true            // LaunchedEffect 작동시키기
                    onBackClick()             // 뒤로가기 콜백 실행
                }
            }) {
                Icon(
                    imageVector = Icons.Filled.ArrowBackIosNew,
                    contentDescription = "뒤로가기",
                    tint = Color.White
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = Color.Transparent,
        )
    )
}
