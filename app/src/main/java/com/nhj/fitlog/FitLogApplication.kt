package com.nhj.fitlog

import android.app.Application
import androidx.navigation.NavHostController
import dagger.hilt.android.HiltAndroidApp

// Hilt를 사용하려면 이 어노테이션을 애플리케이션의 Application 클래스에 선언해야 함
@HiltAndroidApp
class FitLogApplication : Application() {

    // 네비게이션
    lateinit var navHostController: NavHostController

}