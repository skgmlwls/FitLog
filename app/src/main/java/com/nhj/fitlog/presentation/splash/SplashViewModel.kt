package com.nhj.fitlog.presentation.splash

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nhj.fitlog.FitLogApplication
import com.nhj.fitlog.data.service.UserService
import com.nhj.fitlog.utils.MainScreenName
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SplashViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val userService: UserService
) : ViewModel() {
    val application = context as FitLogApplication


    /**
     * 자동 로그인 체크 + 최소 2초 스플래시 보장
     */
    fun checkAutoLogin() {
        viewModelScope.launch {
            val start = System.currentTimeMillis()

            // 1) DataStore 에서 UID 읽기
            val prefs = application.dataStore.data.first()
            val storedUid = prefs[FitLogApplication.AUTO_LOGIN_UID_KEY]

            // 2) Firestore 에 유저 존재 여부 확인
            val shouldGoHome = !storedUid.isNullOrBlank()
                    && userService.isUserExists(storedUid)

            // 3) 최소 2초 스플래시 보장
            val elapsed = System.currentTimeMillis() - start
            val remaining = 2000 - elapsed
            if (remaining > 0) delay(remaining)

            // 4) 분기 네비게이션
            if (shouldGoHome) {
                application.userUid = storedUid!!
                onNavigateToHome()
            } else {
                onNavigateToLogin()
            }
        }
    }

    // 로그인 화면으로 이동
    fun onNavigateToLogin() {
        application.navHostController.navigate(MainScreenName.MAIN_SCREEN_LOGIN.name) {
            popUpTo(0) { inclusive = true } // 🔥 백스택 모두 제거
            launchSingleTop = true
        }
    }

    // 로그인 화면으로 이동
    fun onNavigateToHome() {
        application.navHostController.navigate(MainScreenName.MAIN_SCREEN_HOME.name) {
            popUpTo(0) { inclusive = true } // 🔥 백스택 모두 제거
            launchSingleTop = true
        }
    }

}