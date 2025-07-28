package com.nhj.fitlog.presentation.join

import android.app.Activity
import android.util.Log
import androidx.activity.compose.LocalActivity
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.nhj.fitlog.component.FitLogAlertDialog
import com.nhj.fitlog.component.FitLogButton
import com.nhj.fitlog.component.FitLogTextField
import com.nhj.fitlog.component.FitLogTopBar
import com.nhj.fitlog.component.LottieLoadingOverlay
import com.nhj.fitlog.presentation.join.component.FitLogPhoneTextField

@Composable
fun JoinScreen3(
    viewModel: JoinViewModel = hiltViewModel()
) {
    val focusManager = LocalFocusManager.current

    // LocalActivity 로부터 직접 Activity 가져오기
    val activity: Activity? = LocalActivity.current

    // isLoading 이 true가 되면 키보드 내려주기
    LaunchedEffect(viewModel.isLoading) {
        if (viewModel.isLoading) {
            focusManager.clearFocus()
        }
    }

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
                // ── 휴대폰 입력 + 전송 ───────────────────
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.Bottom) {
                    FitLogPhoneTextField(
                        value = viewModel.joinPhoneNumber,
                        onValueChange = { viewModel.joinPhoneNumber = it },
                        modifier = Modifier.weight(1f),
                        enabled = viewModel.verificationId.isBlank() // 전송 후 비활성
                    )
                    FitLogButton(
                        text = "전송하기",
                        onClick = {
                            if (activity != null) {
                                viewModel.sendPhoneCode(activity)
                            }
                        },
                        enabled = viewModel.verificationId.isBlank(),
                        modifier = Modifier.width(100.dp).align(Alignment.Bottom),
                        horizontalPadding = 0.dp,
                        backgroundColor = Color(0xFF3C3C3C)
                    )
                    Spacer(Modifier.width(20.dp))
                }

                Spacer(modifier = Modifier.height(30.dp))

                // ── 인증번호 입력 + 확인 ─────────────────
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    FitLogTextField(
                        value = viewModel.joinVerificationCode,
                        onValueChange = { viewModel.joinVerificationCode = it },
                        label = "인증 번호",
                        modifier = Modifier.weight(1f),
                        enabled = !viewModel.phoneVerified     // 인증 후 비활성
                    )
                    FitLogButton(
                        text = "인증하기",
                        onClick = { viewModel.verifyCode() },
                        enabled = !viewModel.phoneVerified,
                        modifier = Modifier.width(100.dp).align(Alignment.Bottom),
                        horizontalPadding = 0.dp,
                        backgroundColor = Color(0xFF3C3C3C)
                    )
                    Spacer(Modifier.width(20.dp))
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
            
            // 로딩 화면
            LottieLoadingOverlay(
                isVisible = viewModel.isLoading,
                modifier = Modifier.fillMaxSize()
            )

            // ── 다이얼로그들 ─────────────────────────
            if (viewModel.showPhoneDuplicateDialog) {
                FitLogAlertDialog(
                    "번호 중복",
                    "이미 가입된 번호입니다.",
                    onConfirm = { viewModel.showPhoneDuplicateDialog = false },
                    onDismiss = { viewModel.showPhoneDuplicateDialog = false },
                    showCancelButton = false
                )
            }
            if (viewModel.showPhoneSendSuccessDialog) {
                FitLogAlertDialog(
                    "전송 완료",
                    "인증번호가 전송되었습니다.",
                    onConfirm = { viewModel.showPhoneSendSuccessDialog = false },
                    onDismiss = { viewModel.showPhoneSendSuccessDialog = false },
                    showCancelButton = false
                )
            }
            if (viewModel.showPhoneSendErrorDialog) {
                FitLogAlertDialog(
                    "전송 실패",
                    "인증번호 전송에 실패했습니다.",
                    onConfirm = { viewModel.showPhoneSendErrorDialog = false },
                    onDismiss = { viewModel.showPhoneSendErrorDialog = false },
                    showCancelButton = false
                )
            }
            if (viewModel.showCodeVerifySuccessDialog) {
                FitLogAlertDialog(
                    "인증 성공",
                    "휴대폰 인증이 완료되었습니다.",
                    onConfirm = {
                        viewModel.showCodeVerifySuccessDialog = false
                        viewModel.onNavigateToJoinScreen4()
                    },
                    onDismiss = {
                        viewModel.showCodeVerifySuccessDialog = false
                        viewModel.onNavigateToJoinScreen4()
                    },
                    showCancelButton = false
                )
            }
            if (viewModel.showCodeVerifyErrorDialog) {
                FitLogAlertDialog(
                    "인증 실패",
                    "인증번호가 일치하지 않습니다.",
                    onConfirm = { viewModel.showCodeVerifyErrorDialog = false },
                    onDismiss = { viewModel.showCodeVerifyErrorDialog = false },
                    showCancelButton = false
                )
            }
        }
    }
}