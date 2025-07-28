package com.nhj.fitlog.presentation.join

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.nhj.fitlog.component.FitLogTextField
import com.nhj.fitlog.component.FitLogButton
import com.nhj.fitlog.component.FitLogTopBar

@Composable
fun JoinScreen2(
    viewModel: JoinViewModel = hiltViewModel()
) {
    val focusManager = LocalFocusManager.current

    // 비밀번호 빈칸 에러
    var showPasswordError by remember { mutableStateOf(false) }
    // 확인 빈칸/불일치 에러
    var showConfirmError by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            FitLogTopBar(
                title = "비밀번호",
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
                // 비밀번호 입력
                FitLogTextField(
                    value = viewModel.joinPassword,
                    onValueChange = {
                        viewModel.joinPassword = it
                        if (showPasswordError && it.isNotBlank()) showPasswordError = false
                    },
                    label = "비밀번호",
                    isPassword = true,
                    isError = showPasswordError,
                    errorText = "비밀번호를 입력하세요."
                )

                // 비밀번호 확인 입력
                FitLogTextField(
                    value = viewModel.joinConfirmPassword,
                    onValueChange = {
                        viewModel.joinConfirmPassword = it
                        if (showConfirmError && it.isNotBlank()) showConfirmError = false
                    },
                    label = "비밀번호 확인",
                    isPassword = true,
                    isError = showConfirmError,
                    errorText = when {
                        viewModel.joinConfirmPassword.isBlank() -> "비밀번호 확인을 입력하세요."
                        viewModel.joinConfirmPassword != viewModel.joinPassword -> "비밀번호가 일치하지 않습니다."
                        else -> ""
                    }
                )
            }

            // 다음 버튼
            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(bottom = 20.dp)
            ) {
                FitLogButton(
                    text = "다음",
                    onClick = {
                        // 에러 초기화
                        showPasswordError = false
                        showConfirmError = false

                        // 검증
                        when {
                            viewModel.joinPassword.isBlank() ->
                                showPasswordError = true
                            viewModel.joinConfirmPassword.isBlank() ->
                                showConfirmError = true
                            viewModel.joinPassword != viewModel.joinConfirmPassword ->
                                showConfirmError = true
                            else ->
                                viewModel.onNavigateToJoinScreen3()
                        }
                    },
                    horizontalPadding = 20.dp,
                )
            }
        }
    }
}