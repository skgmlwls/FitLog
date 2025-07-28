package com.nhj.fitlog.presentation.join

import androidx.activity.compose.rememberLauncherForActivityResult
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
import com.nhj.fitlog.component.FitLogAlertDialog
import com.nhj.fitlog.component.FitLogTextField
import com.nhj.fitlog.component.FitLogButton
import com.nhj.fitlog.component.FitLogTopBar

@Composable
fun JoinScreen1(
    viewModel: JoinViewModel = hiltViewModel()
) {
    val focusManager = LocalFocusManager.current

    // 에러 메시지
    var showError by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            FitLogTopBar(
                title = "아이디",
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
                    value = viewModel.joinId,
                    onValueChange = {
                        viewModel.joinId = it
                        if (showError && it.isNotBlank()) showError = false // 🔥 입력되면 에러 해제
                    },
                    label = "아이디",
                    isError = showError,
                    errorText = "아이디는 입력하세요."
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
                        if (viewModel.joinId.isBlank()) {
                            showError = true
                        } else {
                            viewModel.checkIdAndNavigate()
                        }
                    },
                    horizontalPadding = 20.dp,
                )
            }

            // 중복 알림
            if (viewModel.showIdDuplicateDialog) {
                FitLogAlertDialog(
                    title = "아이디 중복",
                    message = "이미 사용 중인 아이디입니다.",
                    onConfirm = { viewModel.showIdDuplicateDialog = false },
                    onDismiss = { viewModel.showIdDuplicateDialog = false },
                    showCancelButton = false
                )
            }

        }
    }
}