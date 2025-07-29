package com.nhj.fitlog.presentation.login.social_nickname

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.datastore.preferences.core.edit
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.nhj.fitlog.FitLogApplication
import com.nhj.fitlog.data.service.UserService
import com.nhj.fitlog.domain.model.UserModel
import com.nhj.fitlog.utils.MainScreenName
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SocialNickNameViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val userService: UserService
) : ViewModel() {
    val application = context as FitLogApplication

//    // UID
//    var joinUid by mutableStateOf("")
//    // Email
//    var joinEmail by mutableStateOf("")

    // 인증된 사용자의 Firebase UID
    var joinMethod by mutableStateOf("")

    // 닉네임
    var joinNickname by mutableStateOf("")

    // 회원가입 완료 다이얼로그
    var showCompleteDialog by mutableStateOf(false)             // 기존 showDialog
    // 닉네임 중복 알림
    var showNicknameDuplicateDialog by mutableStateOf(false)

//    fun settingUserData(userUid: String, userEmail: String) {
//        joinUid = userUid
//        joinEmail = userEmail
//        Log.d("GoogleNickNameViewModel", "joinUid: $joinUid, joinEmail: $joinEmail")
//    }

    /** 닉네임 검사 후 다이얼로그 또는 완료 */
    fun checkNicknameAndComplete() {
        viewModelScope.launch {
            // 서버에 빈 값 검사해도 좋지만, 우선 중복만
            val ok = userService.isNicknameAvailable(joinNickname)
            if (ok) {
                // 유저 정보 저장
                addUserToFirestore()
                showCompleteDialog = true
            } else {
                showNicknameDuplicateDialog = true
            }
        }
    }

    // 유저 정보 추가
    fun addUserToFirestore() {
        // 1) Authentication 확인
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser == null) {
            // (정상 흐름에서는 여기 안 옴)
            return
        }

        // 2) Firestore 저장
        viewModelScope.launch {
            val user = UserModel(
                joinMethod = joinMethod,
                uid             = currentUser.uid,             // Auth UID
                id              = "",                           // Auth Email
                password        = "",                           // Auth Email
                email           = currentUser.email.orEmpty(), // Auth Email
                nickname        = joinNickname,                // 입력한 닉네임
                phone           = "",                          // 없으므로 빈값
                profileImageUrl = "",                // Google 프로필 사진
                recordPublic  = true,
                picturePublic = true,
                createdAt       = System.currentTimeMillis()
            )
            userService.addUser(user)

            // 유저 UID 를 앱에 저장
            application.userUid = currentUser.uid

            // 자동 로그인 UID 를 Jetpack DataStore 에 저장
            application.dataStore.edit { prefs ->
                // Application 의 companion object 에 정의된 키를 사용
                prefs[FitLogApplication.AUTO_LOGIN_UID_KEY] = user.uid
            }
        }
    }

    // 홈 화면으로 이동
    fun onNavigateToHomeScreen() {
        application.navHostController.navigate(MainScreenName.MAIN_SCREEN_HOME.name) {
            popUpTo(0) { inclusive = true }
            launchSingleTop = true
        }
    }

    // 로그인 화면으로 이동
    fun onNavigateToLoginScreen() {
        application.navHostController.navigate(MainScreenName.MAIN_SCREEN_LOGIN.name) {
            popUpTo(0) { inclusive = true } // 🔥 백스택 모두 제거
            launchSingleTop = true
        }
    }

    fun onNavigateBack() {
        application.navHostController.popBackStack()
    }

}