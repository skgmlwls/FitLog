package com.nhj.fitlog.presentation.login

import android.app.Activity
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.nhj.fitlog.R
import com.nhj.fitlog.component.FitLogAlertDialog
import com.nhj.fitlog.component.FitLogButton
import com.nhj.fitlog.component.FitLogText
import com.nhj.fitlog.component.FitLogTextField
import com.nhj.fitlog.component.LottieLoadingOverlay
import com.nhj.fitlog.presentation.login.component.FitLogAutoLoginCheckbox
import com.nhj.fitlog.presentation.login.component.FitLogIconButton2
import com.nhj.fitlog.presentation.login.launcher.rememberGoogleSignInLauncher

@Composable
fun LoginScreen(
    viewModel: LoginViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val activity = context as Activity

    // 아이디 텍스트 상태 저장
    var id by remember { mutableStateOf("") }
    // 비밀번호 텍스트 상태 저장
    var password by remember { mutableStateOf("") }

    // 에러 메시지 상태
    var idError by remember { mutableStateOf("") }
    var passwordError by remember { mutableStateOf("") }

    // 포커스 제어를 위한 FocusManager
    val focusManager = LocalFocusManager.current

    // Google 로그인 런처
    // stringResource로 webClientId를 가져와 Google 로그인 런처를 생성하고,
    // 성공 시 viewModel.onGoogleSignInSuccess 호출
    val launchGoogleSignIn = rememberGoogleSignInLauncher { acct ->
        viewModel.onGoogleSignInSuccess(acct)
    }

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

            // 아이디 텍스트 필드
            FitLogTextField(
                value = id,
                onValueChange = {
                    id = it
                    if (idError.isNotEmpty() && it.isNotBlank()) idError = ""
                },
                label = "아이디",
                isError = idError.isNotEmpty(),
                errorText = idError,
                horizontalPadding = 40.dp
            )
            // 비밀번호 텍스트 필드
            FitLogTextField(
                value = password,
                onValueChange = { password = it },
                label = "비밀번호",
                isPassword = true,
                isError = passwordError.isNotEmpty(),
                errorText = passwordError,
                horizontalPadding = 40.dp,
            )

            Spacer(modifier = Modifier.height(5.dp))

            // 자동 로그인 체크박스
            FitLogAutoLoginCheckbox(
                checked = viewModel.autoLoginEnabled,
                onCheckedChange = { viewModel.autoLoginEnabled = it }
            )

            Spacer(modifier = Modifier.height(10.dp))
            
            // 로그인 버튼
            FitLogButton(
                text = "로그인",
                onClick = {
                    // 빈칸 검증
                    var valid = true
                    if (id.isBlank()) {
                        idError = "아이디를 입력하세요."
                        valid = false
                    }
                    if (password.isBlank()) {
                        passwordError = "비밀번호를 입력하세요."
                        valid = false
                    }
                    if (valid) {
                        viewModel.onIdPasswordLogin(id, password)
                    }
                }
            )
            
            // 회원가입 버튼
            FitLogButton(
                text = "회원가입",
                onClick = { viewModel.onNavigateToJoin() },
                backgroundColor = Color(0xFF3C3C3C),
            )

            Spacer(modifier = Modifier.height(20.dp))

            HorizontalDivider(
                modifier = Modifier.padding(horizontal = 40.dp),
                thickness = 1.dp,
                color = Color(0xFFFFFFFF)
            )

            Spacer(modifier = Modifier.height(10.dp))
            
            FitLogText(
                text = "또는",
                fontSize = 12,
                fontWeight = FontWeight.Normal,
                color = Color(0xFFFFFFFF)
            )

            Spacer(modifier = Modifier.height(20.dp))
            
            // 카카오 로그인 버튼
            FitLogIconButton2(
                text = "카카오 로그인",
                icon = painterResource(id = R.drawable.kakao_icon),
                onClick = { viewModel.onKakaoSignIn(activity) },
                containerColor = Color(0xFFFFE812),
                contentColor = Color(0xFF191919)
            )

            Spacer(modifier = Modifier.height(5.dp))

            // 구글 로그인 버튼 클릭 시
            FitLogIconButton2(
                text = "구글 로그인",
                icon = painterResource(id = R.drawable.google_icon),
                onClick = launchGoogleSignIn,
                containerColor = Color.White,
                contentColor = Color(0xFF191919)
            )

            // -- 로그인 실패 다이얼로그 --
            if (viewModel.showLoginErrorDialog) {
                FitLogAlertDialog(
                    title = "로그인 실패",
                    message = "아이디 또는 비밀번호가 일치하지 않습니다.",
                    onConfirm = { viewModel.showLoginErrorDialog = false },
                    onDismiss = { viewModel.showLoginErrorDialog = false },
                    showCancelButton = false  // 확인 버튼만 표시
                )
            }
            
        }

        // 로딩 오버레이
        LottieLoadingOverlay(
            isVisible = viewModel.isLoading,
            modifier = Modifier.fillMaxSize()
        )

    }

}