package com.nhj.fitlog.presentation.login

import android.content.Context
import androidx.lifecycle.ViewModel
import com.nhj.fitlog.FitLogApplication
import com.nhj.fitlog.utils.JoinScreenName
import com.nhj.fitlog.utils.MainScreenName
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
) : ViewModel() {
    val application = context as FitLogApplication

    // 회원가입 화면으로 이동
    fun onNavigateToJoin() {
        application.navHostController.navigate(JoinScreenName.MAIN_SCREEN_STEP1.name)
    }

    // 홈 화면으로 이동
    fun onNavigateToHome() {
        application.navHostController.navigate(MainScreenName.MAIN_SCREEN_HOME.name) {
            popUpTo(0) { inclusive = true } // 🔥 백스택 모두 제거
            launchSingleTop = true
        }
    }

}