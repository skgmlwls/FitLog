package com.nhj.fitlog

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.google.accompanist.navigation.animation.AnimatedNavHost
import com.nhj.fitlog.presentation.exercise.add.ExerciseAddScreen
import com.nhj.fitlog.presentation.exercise.detail.ExerciseDetailScreen
import com.nhj.fitlog.presentation.exercise.history.ExerciseHistoryScreen
import com.nhj.fitlog.presentation.exercise.history_detail.ExerciseHistoryDetailScreen
import com.nhj.fitlog.presentation.exercise.list.ExerciseTypeScreen
import com.nhj.fitlog.presentation.home.HomeScreen
import com.nhj.fitlog.presentation.join.JoinScreen1
import com.nhj.fitlog.presentation.join.JoinScreen2
import com.nhj.fitlog.presentation.join.JoinScreen3
import com.nhj.fitlog.presentation.join.JoinScreen4
import com.nhj.fitlog.presentation.login.LoginScreen
import com.nhj.fitlog.presentation.splash.SplashScreen
import com.nhj.fitlog.ui.theme.FitLogTheme
import com.nhj.fitlog.utils.ExerciseScreenName
import com.nhj.fitlog.utils.JoinScreenName
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


@OptIn(ExperimentalAnimationApi::class)
@Composable
fun MyApp() {
    val navController = rememberNavController()
    val app = LocalContext.current.applicationContext as FitLogApplication
    app.navHostController = navController

    AnimatedNavHost(
        navController = navController,
        startDestination = MainScreenName.MAIN_SCREEN_HOME.name,
        enterTransition = {
            slideInHorizontally(
                initialOffsetX = { it }, // 오른쪽에서 들어옴
                animationSpec = tween(300)
            )
        },
        exitTransition = {
            slideOutHorizontally(
                targetOffsetX = { -it }, // 왼쪽으로 나감
                animationSpec = tween(300)
            )
        },
        popEnterTransition = {
            slideInHorizontally(
                initialOffsetX = { -it }, // 왼쪽에서 들어옴
                animationSpec = tween(300)
            )
        },
        popExitTransition = {
            slideOutHorizontally(
                targetOffsetX = { it }, // 오른쪽으로 나감
                animationSpec = tween(300)
            )
        }

    ) {
        // 스플래시 화면
        composable(MainScreenName.MAIN_SCREEN_SPLASH.name) { SplashScreen() }

        // 로그인 화면
        composable(
            MainScreenName.MAIN_SCREEN_LOGIN.name,
            enterTransition = {
                fadeIn(animationSpec = tween(150)) +
                        scaleIn(
                            initialScale = 0.8f,
                            animationSpec = tween(
                                durationMillis = 250,
                                easing = FastOutSlowInEasing // 부드럽고 자연스럽게
                            )
                        )
            }
        ) { LoginScreen() }

        // 홈 화면
        composable(MainScreenName.MAIN_SCREEN_HOME.name) { HomeScreen() }

        // 회원가입
        // 회원가입 화면 1
        composable(JoinScreenName.MAIN_SCREEN_STEP1.name) { JoinScreen1() }
        // 회원가입 화면 2
        composable(JoinScreenName.MAIN_SCREEN_STEP2.name) { JoinScreen2() }
        // 회원가입 화면 3
        composable(JoinScreenName.MAIN_SCREEN_STEP3.name) { JoinScreen3() }
        // 회원가입 화면 4
        composable(
            JoinScreenName.MAIN_SCREEN_STEP4.name,
            exitTransition = {
                fadeOut(animationSpec = tween(100)) +
                        scaleOut(
                            targetScale = 1.1f,
                            animationSpec = tween(
                                durationMillis = 180,
                                easing = FastOutLinearInEasing
                            )
                        )
            }
        ) { JoinScreen4() }

        // 운동 종류 화면
        composable(ExerciseScreenName.EXERCISE_TYPE_SCREEN.name) { ExerciseTypeScreen() }
        // 운동 추가 화면
        composable(ExerciseScreenName.EXERCISE_ADD_SCREEN.name) { ExerciseAddScreen() }
        // 운동 상세 화면
        composable(ExerciseScreenName.EXERCISE_DETAIL_SCREEN.name) { ExerciseDetailScreen() }
        // 운동 이전 기록 화면
        composable(ExerciseScreenName.EXERCISE_HISTORY_SCREEN.name) { ExerciseHistoryScreen() }
        // 운동 이전 기록 상세 화면
        composable(ExerciseScreenName.EXERCISE_HISTORY_DETAIL_SCREEN.name) { ExerciseHistoryDetailScreen() }

    }
}

// 화면 전환 역활
//🧩 1. enterTransition
//역할: 화면이 새로 진입할 때 실행되는 애니메이션
//🧩 2. exitTransition
//역할: 현재 화면이 사라질 때 실행되는 애니메이션
//🧩 3. popEnterTransition
//역할: 뒤로가기(popBackStack)로 돌아올 때 등장하는 화면의 애니메이션
//🧩 4. popExitTransition
//역할: 뒤로가기(popBackStack)로 돌아갈 때 사라지는 화면의 애니메이션