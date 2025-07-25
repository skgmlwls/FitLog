package com.nhj.fitlog.presentation.join

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.nhj.fitlog.component.FitLogButton
import com.nhj.fitlog.component.FitLogTextField
import com.nhj.fitlog.component.FitLogTopBar
import com.nhj.fitlog.presentation.join.component.FitLogPhoneTextField

@Composable
fun JoinScreen3(
    viewModel: JoinViewModel = hiltViewModel()
) {
    val focusManager = LocalFocusManager.current

    Scaffold(
        topBar = {
            FitLogTopBar(
                title = "문자인증",
                onBackClick = { viewModel.onNavigateBack() }
            )
        },
        containerColor = Color(0xFF121212)
    ) { innerPadding ->

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(Color(0xFF121212))
                .pointerInput(Unit) {
                    detectTapGestures(onTap = { focusManager.clearFocus() })
                }
        ) {

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // 🔹 휴대폰 번호 입력 + 인증 요청 버튼
                Row(
                    modifier = Modifier
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.Bottom
                ) {
                    FitLogPhoneTextField(
                        value = viewModel.joinPhoneNumber,
                        onValueChange = { viewModel.joinPhoneNumber = it },
                        modifier = Modifier.weight(1f)
                    )

                    FitLogButton(
                        text = "전송하기",
                        onClick = { /* 인증 요청 */ },
                        modifier = Modifier.width(100.dp)
                            .align(Alignment.Bottom), // 텍스트필드 밑줄 기준 맞춤,
                        horizontalPadding = 0.dp, // Row 내부이므로 패딩 제거
                        backgroundColor = Color(0xFF3C3C3C)
                    )
                    Spacer(modifier = Modifier.width(20.dp))
                }

                Spacer(modifier = Modifier.height(30.dp))

                // 🔹 인증번호 입력 + 확인 버튼
                Row(
                    modifier = Modifier
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    FitLogTextField(
                        value = viewModel.joinConfirmPassword,
                        onValueChange = { viewModel.joinConfirmPassword = it },
                        label = "인증하기",
                        modifier = Modifier.weight(1f)
                    )

                    FitLogButton(
                        text = "인증하기",
                        onClick = { /* 인증번호 확인 */ },
                        modifier = Modifier.width(100.dp)
                            .align(Alignment.Bottom), // 텍스트필드 밑줄 기준 맞춤,
                        horizontalPadding = 0.dp,
                        backgroundColor = Color(0xFF3C3C3C)
                    )
                    Spacer(modifier = Modifier.width(20.dp))
                }

            }

            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(bottom = 20.dp)
            ) {
                FitLogButton(
                    text = "다음",
                    onClick = { viewModel.onNavigateToJoinScreen4() },
                    horizontalPadding = 20.dp,
                )
            }
        }
    }
}