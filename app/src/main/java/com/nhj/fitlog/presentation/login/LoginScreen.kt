package com.nhj.fitlog.presentation.login

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.nhj.fitlog.component.FitLogText
import com.nhj.fitlog.component.FitLogTextField
import com.nhj.fitlog.ui.theme.NanumSquareRound

@Composable
fun LoginScreen(
    viewModel: LoginViewModel = hiltViewModel()
) {

    // 아이디 텍스트 상태 저장
    var id by remember { mutableStateOf("") }
    // 비밀번호 텍스트 상태 저장
    var password by remember { mutableStateOf("") }

    // 포커스 제어를 위한 FocusManager
    val focusManager = LocalFocusManager.current


    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF121212))
            // 화면 아무 곳이나 탭하면 포커스 해제되도록 설정
            .pointerInput(Unit) {
                detectTapGestures(onTap = {
                    focusManager.clearFocus() // ✅ 외부 클릭 시 포커스 해제
                })
            },
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            FitLogText(
                text = "FITLOG",
                fontSize = 60,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF47A6FF),
            )

            Spacer(modifier = Modifier.height(40.dp))

            FitLogTextField(
                value = id,
                onValueChange = { id = it },
                label = "아이디",
                horizontalPadding = 40.dp
            )
            FitLogTextField(
                value = password,
                onValueChange = { password = it },
                label = "비밀번호",
                horizontalPadding = 40.dp,
                isPassword = true
            )
        }
    }

}