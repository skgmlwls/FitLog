package com.nhj.fitlog.presentation.join

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
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
import com.nhj.fitlog.presentation.join.component.JoinCompleteDialog

@Composable
fun JoinScreen4(
    viewModel: JoinViewModel = hiltViewModel()
) {
    val focusManager = LocalFocusManager.current

    // 다이얼로그 표시 여부를 저장하는 상태 변수
    var showDialog by remember { mutableStateOf(false) }

    // 회원가입 완료 다이얼로그
    if (showDialog) {
        JoinCompleteDialog(
            onConfirm = {
                showDialog = false
                viewModel.onNavigateToLoginScreen()
            },
            onDismiss = {
                showDialog = false
            }
        )
    }

    Scaffold(
        topBar = {
            FitLogTopBar(
                title = "닉네임",
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
                FitLogTextField(
                    value = viewModel.joinNickname,
                    onValueChange = { viewModel.joinNickname = it },
                    label = "닉네임"
                )
            }

            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(bottom = 20.dp)
            ) {
                FitLogButton(
                    text = "다음",
                    onClick = {
                        // 버튼 클릭 시 다이얼로그 표시
                        showDialog = true
                    },
                    horizontalPadding = 20.dp,
                )
            }
        }
    }
}