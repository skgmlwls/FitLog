package com.nhj.fitlog.presentation.join

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.nhj.fitlog.FitLogApplication
import com.nhj.fitlog.utils.JoinScreenName
import com.nhj.fitlog.utils.MainScreenName
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

@HiltViewModel
class JoinViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
) : ViewModel() {
    val application = context as FitLogApplication

    // 아이디
    var joinId by mutableStateOf("")
    
    // 비밀번호
    var joinPassword by mutableStateOf("")

    // 비밀번호 확인
    var joinConfirmPassword by mutableStateOf("")
    
    // 전화번호
    var joinPhoneNumber by mutableStateOf("")

    // 인증번호
    var joinVerificationCode by mutableStateOf("")

    // 닉네임
    var joinNickname by mutableStateOf("")

    // 회원가입 화면2로 이동
    fun onNavigateToJoinScreen2() {
        application.navHostController.navigate(JoinScreenName.MAIN_SCREEN_STEP2.name)
    }

    // 회원가입 화면3로 이동
    fun onNavigateToJoinScreen3() {
        application.navHostController.navigate(JoinScreenName.MAIN_SCREEN_STEP3.name)
    }

    // 회원가입 화면4로 이동
    fun onNavigateToJoinScreen4() {
        application.navHostController.navigate(JoinScreenName.MAIN_SCREEN_STEP4.name)
    }

    // 로그인 화면으로 이동
    fun onNavigateToLoginScreen() {
        application.navHostController.navigate(MainScreenName.MAIN_SCREEN_LOGIN.name) {
            popUpTo(0) { inclusive = true } // 🔥 백스택 모두 제거
            launchSingleTop = true
        }
    }

    // 뒤로가기
    fun onNavigateBack() {
        application.navHostController.popBackStack()
    }

}