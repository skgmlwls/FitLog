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
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navigation
import com.google.accompanist.navigation.animation.AnimatedNavHost
import com.nhj.fitlog.presentation.analysis.AnalysisScreen
import com.nhj.fitlog.presentation.coach.CoachScreen
import com.nhj.fitlog.presentation.exercise.add.ExerciseAddScreen
import com.nhj.fitlog.presentation.exercise.detail.ExerciseDetailScreen
import com.nhj.fitlog.presentation.exercise.edit.ExerciseDetailEditScreen
import com.nhj.fitlog.presentation.exercise.history.ExerciseHistoryScreen
import com.nhj.fitlog.presentation.exercise.history_detail.ExerciseHistoryDetailScreen
import com.nhj.fitlog.presentation.exercise.list.ExerciseTypeScreen
import com.nhj.fitlog.presentation.friends.list.FriendsListScreen
import com.nhj.fitlog.presentation.friends.requests.FriendRequestsScreen
import com.nhj.fitlog.presentation.home.HomeScreen
import com.nhj.fitlog.presentation.join.JoinScreen1
import com.nhj.fitlog.presentation.join.JoinScreen2
import com.nhj.fitlog.presentation.join.JoinScreen3
import com.nhj.fitlog.presentation.join.JoinScreen4
import com.nhj.fitlog.presentation.join.JoinViewModel
import com.nhj.fitlog.presentation.login.LoginScreen
import com.nhj.fitlog.presentation.login.social_nickname.SocialNickNameScreen
import com.nhj.fitlog.presentation.record.record_calendar.RecordCalendarScreen
import com.nhj.fitlog.presentation.record.record_detail.RecordDetailScreen
import com.nhj.fitlog.presentation.record.record_edit.RecordEditScreen
import com.nhj.fitlog.presentation.record.record_exercise.RecordExerciseScreen
import com.nhj.fitlog.presentation.routine.add.RoutineAddScreen
import com.nhj.fitlog.presentation.routine.detail.RoutineDetailScreen
import com.nhj.fitlog.presentation.routine.detail_edit.RoutineDetailEditScreen
import com.nhj.fitlog.presentation.routine.exercise_list.RoutineAddListScreen
import com.nhj.fitlog.presentation.routine.list.RoutineListScreen
import com.nhj.fitlog.presentation.setting.SettingsScreen
import com.nhj.fitlog.presentation.splash.SplashScreen
import com.nhj.fitlog.presentation.user.profile.ProfileScreen
import com.nhj.fitlog.ui.theme.FitLogTheme
import com.nhj.fitlog.utils.AnalysisScreenName
import com.nhj.fitlog.utils.CoachScreenName
import com.nhj.fitlog.utils.ExerciseScreenName
import com.nhj.fitlog.utils.FriendScreenName
import com.nhj.fitlog.utils.JoinScreenName
import com.nhj.fitlog.utils.MainScreenName
import com.nhj.fitlog.utils.RecordScreenName
import com.nhj.fitlog.utils.RoutineScreenName
import com.nhj.fitlog.utils.UserProfileScreenName
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
        startDestination = MainScreenName.MAIN_SCREEN_SPLASH.name,
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

        // ─── 회원가입 그래프 ───────────────────
        navigation(
            startDestination = JoinScreenName.MAIN_SCREEN_STEP1.name,
            route = "join_graph"
        ) {
            // 모든 JoinScreen이 "join_graph" 스코프의 같은 ViewModel을 사용합니다.
            composable(JoinScreenName.MAIN_SCREEN_STEP1.name) { backStackEntry ->
                val parentEntry = remember(backStackEntry) {
                    navController.getBackStackEntry("join_graph")
                }
                val vm: JoinViewModel = hiltViewModel(parentEntry)
                JoinScreen1(viewModel = vm)
            }
            composable(JoinScreenName.MAIN_SCREEN_STEP2.name) { backStackEntry ->
                val parentEntry = remember(backStackEntry) {
                    navController.getBackStackEntry("join_graph")
                }
                val vm: JoinViewModel = hiltViewModel(parentEntry)
                JoinScreen2(viewModel = vm)
            }
            composable(JoinScreenName.MAIN_SCREEN_STEP3.name) { backStackEntry ->
                val parentEntry = remember(backStackEntry) {
                    navController.getBackStackEntry("join_graph")
                }
                val vm: JoinViewModel = hiltViewModel(parentEntry)
                JoinScreen3(viewModel = vm)
            }
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
            ) { backStackEntry ->
                val parentEntry = remember(backStackEntry) {
                    navController.getBackStackEntry("join_graph")
                }
                val vm: JoinViewModel = hiltViewModel(parentEntry)
                JoinScreen4(viewModel = vm)
            }
        }

        // 소셜 닉네임 화면
        composable(
            route = "${JoinScreenName.SOCIAL_NICKNAME_SCREEN.name}/{joinMethod}",
        ) { backStackEntry ->
            val joinMethod = backStackEntry.arguments?.getString("joinMethod") ?: ""
            SocialNickNameScreen(joinMethod)
        }
        
        // 설정 화면
        composable(MainScreenName.MAIN_SCREEN_SETTING.name) { SettingsScreen() }
        // 프로필 화면
        composable(UserProfileScreenName.PROFILE_SCREEN.name) { ProfileScreen() }

        // 운동 종류 화면
        composable(ExerciseScreenName.EXERCISE_TYPE_SCREEN.name) { ExerciseTypeScreen() }
        // 운동 추가 화면
        composable(ExerciseScreenName.EXERCISE_ADD_SCREEN.name) { ExerciseAddScreen() }
        // 운동 상세 화면
        composable(
            route = "${ExerciseScreenName.EXERCISE_DETAIL_SCREEN.name}/{id}"
        ) {
            val id = it.arguments?.getString("id") ?: ""

            ExerciseDetailScreen(id)
        }
        // 운동 상세 수정 화면
        composable(
            route = "${ExerciseScreenName.EXERCISE_DETAIL_EDIT_SCREEN.name}/{id}/{name}/{category}/{memo}"
        ) {
            val id = it.arguments?.getString("id") ?: ""
            val name = it.arguments?.getString("name") ?: ""
            val category = it.arguments?.getString("category") ?: ""
            val memo = it.arguments?.getString("memo") ?: ""

            ExerciseDetailEditScreen(id, name, category, memo)
        }
        // 운동 이전 기록 화면
        composable(ExerciseScreenName.EXERCISE_HISTORY_SCREEN.name) { ExerciseHistoryScreen() }
        // 운동 이전 기록 상세 화면
        composable(ExerciseScreenName.EXERCISE_HISTORY_DETAIL_SCREEN.name) { ExerciseHistoryDetailScreen() }

        // 루틴 리스트 화면
        composable(RoutineScreenName.ROUTINE_LIST_SCREEN.name) { RoutineListScreen() }
        // 루틴 추가 화면
        composable(RoutineScreenName.ROUTINE_ADD_SCREEN.name) { RoutineAddScreen() }
        // 루틴 추가 운동 리스트 화면
        composable(RoutineScreenName.ROUTINE_ADD_LIST_SCREEN.name) { RoutineAddListScreen() }
        // 루틴 상세 화면
        composable(
            route = "${RoutineScreenName.ROUTINE_DETAIL_SCREEN.name}/{routineId}"
        ) {
            val routineId = it.arguments?.getString("routineId") ?: ""

            RoutineDetailScreen(routineId)
        }
        // 루틴 수정 화면
        composable(
            route = "${RoutineScreenName.ROUTINE_DETAIL_EDIT_SCREEN.name}/{routineId}"
        ) {
            val routineId = it.arguments?.getString("routineId") ?: ""

            RoutineDetailEditScreen(routineId)
        }

        // 운동 기록 목록 화면
        composable(
            route = "${RecordScreenName.RECORD_CALENDAR_SCREEN.name}/{previousScreen}/{uid}/{nickName}",
        ) {
            val previousScreen = it.arguments?.getString("previousScreen") ?: RecordScreenName.RECORD_CALENDAR_SCREEN.name
            val uid = it.arguments?.getString("uid") ?: ""
            val nickName = it.arguments?.getString("nickName") ?: ""
            RecordCalendarScreen(previousScreen, uid, nickName)
        }
        // 운동 기록 화면
        composable(
            route = "${RecordScreenName.RECORD_EXERCISE_SCREEN.name}/{selectedDateString}"
        ) {
            val selectedDateString = it.arguments?.getString("selectedDateString") ?: ""

            RecordExerciseScreen(selectedDateString)
        }
        // 운동 상세 기록 화면
        composable(
            route = "${RecordScreenName.RECORD_DETAIL_SCREEN.name}/{recordId}/{uid}/{previousScreen}"
        ) { backStackEntry ->
            val recordId = backStackEntry.arguments?.getString("recordId") ?: return@composable
            val uid = backStackEntry.arguments?.getString("uid") ?: return@composable
            val previousScreen = backStackEntry.arguments?.getString("previousScreen") ?: MainScreenName.MAIN_SCREEN_HOME.name
            RecordDetailScreen(recordId = recordId, uid = uid, previousScreen = previousScreen)
        }
        // 운동 기록 수정 화면
        composable(
            route = "${RecordScreenName.RECORD_EDIT_SCREEN.name}/{recordId}"
        ) {
            val recordId = it.arguments?.getString("recordId") ?: return@composable
            RecordEditScreen(recordId = recordId)
        }

        // 친구 목록 리스트 화면
        composable(
            FriendScreenName.FRIEND_LIST_SCREEN.name
        ) {
            FriendsListScreen()
        }
        // 친구 요청 리스트 화면
        composable(
            FriendScreenName.FRIEND_REQUESTS_SCREEN.name
        ) {
            FriendRequestsScreen()
        }

        // 분석 화면
        composable(
            AnalysisScreenName.ANALYSIS_SCREEN.name
        ) {
            AnalysisScreen()
        }

        // 코치 화면
        composable(
            CoachScreenName.COACH_SCREEN.name
        ) {
            CoachScreen()
        }

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