package com.nhj.fitlog.presentation.login

import android.app.Activity
import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.datastore.preferences.core.edit
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.OAuthProvider
import com.nhj.fitlog.FitLogApplication
import com.nhj.fitlog.data.service.UserService
import com.nhj.fitlog.utils.JoinMethod
import com.nhj.fitlog.utils.JoinScreenName
import com.nhj.fitlog.utils.MainScreenName
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val userService: UserService         // Firestore 저장용 서비스
) : ViewModel() {
    val application = context as FitLogApplication

    // 인증된 사용자의 Firebase UID
    var userUid by mutableStateOf("")

    // 인증된 사용자의 Firebase Email
    private var userEmail by mutableStateOf("")

    // 로그인 실패 다이얼로그
    var showLoginErrorDialog by mutableStateOf(false)

    // 자동 로그인 여부
    var autoLoginEnabled by mutableStateOf(false)

    // 로딩 상태
    var isLoading by mutableStateOf(false)

    /** 이메일/비번 로그인 */
    fun onIdPasswordLogin(id: String, password: String) {
        viewModelScope.launch {
            val user = userService.login(id, password)
            if (user != null) {
                // 1) UID 세팅
                application.userUid = user.uid

                // 2) 자동 로그인 체크 시 DataStore 에 UID 저장
                if (autoLoginEnabled) {
                    application.dataStore.edit { prefs ->
                        // Application 의 companion object 에 정의된 키를 사용
                        prefs[FitLogApplication.AUTO_LOGIN_UID_KEY] = user.uid
                    }
                }

                // Uid 기반으로 유저 정보 가져오기
                userService.getUserByUid(application.userUid!!)?.let { application.userModel = it }

                // 3) 홈으로 이동
                onNavigateToHome()
            } else {
                showLoginErrorDialog = true
            }
        }
    }

    /** Google 로그인 후 Firebase 인증 */
    fun onGoogleSignInSuccess(account: GoogleSignInAccount) {
        // 1) 로딩 시작
        isLoading = true

        val idToken = account.idToken ?: run {
            isLoading = false
            return
        }
        val credential = GoogleAuthProvider.getCredential(idToken, null)

        FirebaseAuth.getInstance()
            .signInWithCredential(credential)
            .addOnSuccessListener { authResult ->
                viewModelScope.launch {
                    // 2) UID 세팅
                    val uid = authResult.user?.uid.orEmpty()
                    application.userUid = uid

                    // 3) Firestore 가입 여부 확인
                    val exists = userService.isUserExists(uid)

                    // 4) 자동 로그인 UID 저장
                    application.dataStore.edit { prefs ->
                        prefs[FitLogApplication.AUTO_LOGIN_UID_KEY] = uid
                    }

                    // 5) 유저 정보 로드
                    userService.getUserByUid(uid)?.let { application.userModel = it }

                    // 6) 네비게이트
                    if (exists) {
                        onNavigateToHome()
                    } else {
                        onNavigateToGoogleNickname(JoinMethod.GOOGLE.methodName)
                    }

                    // 7) 로딩 종료
                    isLoading = false
                }
            }
            .addOnFailureListener { e ->
                // 8) 실패 시 로딩 종료 및 에러 처리
                isLoading = false
                showLoginErrorDialog = true
            }
    }


    ////////// 카카오 로그인 관련 //////////////

    fun onKakaoSignIn(activity: Activity) {
        isLoading = true
        val auth = FirebaseAuth.getInstance()
        val provider = OAuthProvider.newBuilder("oidc.kakao").apply {
            scopes = listOf("account_email")
            addCustomParameter("prompt", "login")
        }
        auth.pendingAuthResult
            ?.addOnSuccessListener { handleOidcResult(it) }
            ?.addOnFailureListener { isLoading = false }
            ?: auth.startActivityForSignInWithProvider(activity, provider.build())
                .addOnSuccessListener { handleOidcResult(it) }
                .addOnFailureListener { isLoading = false }
    }

    // 공통 처리: FirebaseAuth 결과를 받고, 사용자 존재 여부 판별 → 홈/닉네임 화면으로 분기
    private fun handleOidcResult(authResult: AuthResult) {
        viewModelScope.launch {
            val uid = authResult.user?.uid.orEmpty()
            val exists = userService.isUserExists(uid)
            // 1) in-memory UID 세팅
            application.userUid = uid

            // 2) 항상 DataStore에 UID 저장
            application.dataStore.edit { prefs ->
                prefs[FitLogApplication.AUTO_LOGIN_UID_KEY] = uid
            }

            // 3) 분기 네비게이션
            if (exists) {
                // Uid 기반으로 유저 정보 가져오기
                userService.getUserByUid(application.userUid)?.let { application.userModel = it }
                onNavigateToHome()
            } else {
                onNavigateToGoogleNickname(JoinMethod.KAKAO.methodName)
            }
        }
    }

    //////////////////////////////////////////


    // 구글 닉네임 화면으로 이동
    fun onNavigateToGoogleNickname(joinMethod: String) {
        application.navHostController.navigate(
            "${JoinScreenName.SOCIAL_NICKNAME_SCREEN.name}/$joinMethod"
        )
    }

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