package com.nhj.fitlog.presentation.login.google_nickname

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.nhj.fitlog.component.FitLogAlertDialog
import com.nhj.fitlog.component.FitLogButton
import com.nhj.fitlog.component.FitLogTextField
import com.nhj.fitlog.component.FitLogTopBar
import com.nhj.fitlog.presentation.join.component.JoinCompleteDialog

@Composable
fun GoogleNickNameScreen(
    userUid: String,
    userEmail: String,
    viewModel: GoogleNickNameViewModel = hiltViewModel()
) {
    val focusManager = LocalFocusManager.current
    var showNicknameError by remember { mutableStateOf(false) }

//    LaunchedEffect(Unit) {
//        viewModel.settingUserData(userUid, userEmail)
//    }

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
            Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(Color(0xFF121212))
                .pointerInput(Unit) { detectTapGestures { focusManager.clearFocus() } }
        ) {
            Column(
                Modifier
                    .fillMaxWidth()
                    .padding(top = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                FitLogTextField(
                    value = viewModel.joinNickname,
                    onValueChange = {
                        viewModel.joinNickname = it
                        if (showNicknameError && it.isNotBlank()) {
                            showNicknameError = false
                        }
                    },
                    label = "닉네임",
                    isError = showNicknameError,
                    errorText = "닉네임을 입력하세요.",
                    enabled = true
                )
            }

            Column(
                Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(bottom = 20.dp)
            ) {
                FitLogButton(
                    text = "다음",
                    onClick = {
                        if (viewModel.joinNickname.isBlank()) {
                            showNicknameError = true
                        } else {
                            showNicknameError = false
                            viewModel.checkNicknameAndComplete()
                        }
                    },
                    horizontalPadding = 20.dp
                )
            }
        }
    }

    // 완료 다이얼로그
    if (viewModel.showCompleteDialog) {
        JoinCompleteDialog(
            onConfirm = {
                viewModel.onNavigateToHomeScreen()
                viewModel.showCompleteDialog = false
            },
            onDismiss = { viewModel.showCompleteDialog = false }
        )
    }

    // 닉네임 중복 알림
    if (viewModel.showNicknameDuplicateDialog) {
        FitLogAlertDialog(
            title = "닉네임 중복",
            message = "이미 사용 중인 닉네임입니다.",
            onConfirm = { viewModel.showNicknameDuplicateDialog = false },
            onDismiss = { viewModel.showNicknameDuplicateDialog = false },
            showCancelButton = false
        )
    }
}