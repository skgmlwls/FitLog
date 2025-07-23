package com.nhj.fitlog

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.nhj.fitlog.presentation.login.LoginScreen
import com.nhj.fitlog.presentation.splash.SplashScreen
import com.nhj.fitlog.ui.theme.FitLogTheme
import com.nhj.fitlog.utils.MainScreenName
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FitLogTheme {
                MyApp()
            }
        }
    }
}

@Composable
fun MyApp() {
    // 네비게이션 객체
    val rememberNavHostController = rememberNavController()

    val tripApplication = LocalContext.current.applicationContext as FitLogApplication
    tripApplication.navHostController = rememberNavHostController
    
    NavHost(
        navController = rememberNavHostController,
        startDestination = MainScreenName.MAIN_SCREEN_SPLASH.name
    ) {
        // 스플래시 화면
        composable(MainScreenName.MAIN_SCREEN_SPLASH.name) { SplashScreen() }

        // 로그인 화면
        composable(MainScreenName.MAIN_SCREEN_LOGIN.name) { LoginScreen() }

        
    }
}