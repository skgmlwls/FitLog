package com.nhj.fitlog.presentation.login

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.nhj.fitlog.FitLogApplication
import com.nhj.fitlog.data.service.UserService
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

                // 3) 홈으로 이동
                onNavigateToHome()
            } else {
                showLoginErrorDialog = true
            }
        }
    }

    /** Google 로그인 후 Firebase 인증 */
    fun onGoogleSignInSuccess(account: GoogleSignInAccount) {
        val idToken = account.idToken ?: return
        val credential = GoogleAuthProvider.getCredential(idToken, null)

        FirebaseAuth.getInstance()
            .signInWithCredential(credential)
            .addOnSuccessListener { authResult ->
                // 1) UID 세팅
                val uid = authResult.user?.uid.orEmpty()

                // 2) Firestore 가입 여부 확인
                viewModelScope.launch {
                    val exists = userService.isUserExists(uid)
                    if (exists) {
                        application.userUid = uid

                        // 자동 로그인 UID 를 Jetpack DataStore 에 저장
                        application.dataStore.edit { prefs ->
                            // Application 의 companion object 에 정의된 키를 사용
                            prefs[FitLogApplication.AUTO_LOGIN_UID_KEY] = uid
                        }

                        // 이미 가입된 유저 → 홈으로
                        onNavigateToHome()
                    } else {
                        // 신규 유저 → 닉네임 입력 화면으로
                        onNavigateToGoogleNickname()
                    }
                }
            }
            .addOnFailureListener { e ->
                // TODO: 실패 토스트 등 처리
            }
    }

    // 구글 닉네임 화면으로 이동
    fun onNavigateToGoogleNickname() {
        application.navHostController.navigate(
            "${JoinScreenName.GOOGLE_NICKNAME_SCREEN.name}/$userUid/$userEmail"
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