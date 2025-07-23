package com.nhj.fitlog.presentation.splash

import android.content.Context
import androidx.lifecycle.ViewModel
import com.nhj.fitlog.FitLogApplication
import com.nhj.fitlog.utils.MainScreenName
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

@HiltViewModel
class SplashViewModel @Inject constructor(
    @ApplicationContext private val context: Context
) : ViewModel() {
    val application = context as FitLogApplication

    // 로그인 화면으로 이동
    fun onNavigateToLogin() {
        application.navHostController.navigate(MainScreenName.MAIN_SCREEN_LOGIN.name)
    }

}