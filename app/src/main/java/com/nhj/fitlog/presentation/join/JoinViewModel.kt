package com.nhj.fitlog.presentation.join

import android.app.Activity
import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.nhj.fitlog.FitLogApplication
import com.nhj.fitlog.data.service.UserService
import com.nhj.fitlog.domain.model.UserModel
import com.nhj.fitlog.utils.JoinMethod
import com.nhj.fitlog.utils.JoinScreenName
import com.nhj.fitlog.utils.MainScreenName
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@HiltViewModel
class JoinViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val userService: UserService
) : ViewModel() {
    val application = context as FitLogApplication

    // 아이디
    var joinId by mutableStateOf("")                       // 입력된 아이디
    var showIdDuplicateDialog by mutableStateOf(false)     // 중복 알림

    // 비밀번호
    var joinPassword by mutableStateOf("")

    // 비밀번호 확인
    var joinConfirmPassword by mutableStateOf("")

    // 전화번호 입력 / 콜백 상태
    var joinPhoneNumber by mutableStateOf("")          // 입력된 번호
    var joinVerificationCode by mutableStateOf("")      // 입력된 코드
    var verificationId by mutableStateOf("")           // onCodeSent 콜백으로 받은 ID
    // 다이얼로그 토글 플래그
    var showPhoneDuplicateDialog by mutableStateOf(false)      // 번호 중복
    var showPhoneSendErrorDialog by mutableStateOf(false)      // 전송 실패
    var showPhoneSendSuccessDialog by mutableStateOf(false)    // 전송 성공
    var showCodeVerifyErrorDialog by mutableStateOf(false)     // 인증 실패
    var showCodeVerifySuccessDialog by mutableStateOf(false)   // 인증 성공
    // 인증 완료 여부 (코드 검증 성공 시 true)
    var phoneVerified by mutableStateOf(false)

    // 인증된 사용자의 Firebase UID
    var userUid by mutableStateOf("")

    // 닉네임
    var joinNickname by mutableStateOf("")
    // 회원가입 완료 다이얼로그
    var showCompleteDialog by mutableStateOf(false)             // 기존 showDialog
    // 닉네임 중복 알림
    var showNicknameDuplicateDialog by mutableStateOf(false)

    // 로딩 상태
    var isLoading by mutableStateOf(false)       // 로딩 표시용 플래그

    // 중복 검사 후 이동 또는 다이얼로그
    fun checkIdAndNavigate() {
        viewModelScope.launch {
            // isCheckingId = true
            val ok = userService.isUserIdAvailable(joinId)
            // isCheckingId = false

            if (ok) onNavigateToJoinScreen2()
            else showIdDuplicateDialog = true
        }
    }


    /** 1. 중복 검사 후 SMS 전송 */
    fun sendPhoneCode(activity: Activity) {
        isLoading = true    // 로딩 시작
        viewModelScope.launch {
            // 1) Firestore 중복 체크
            if (!userService.isPhoneAvailable(joinPhoneNumber)) {
                showPhoneDuplicateDialog = true
                isLoading = false   // 로딩 종료
                return@launch
            }
            // 2) Firebase SMS 요청
            val options = PhoneAuthOptions.newBuilder(FirebaseAuth.getInstance())
                .setPhoneNumber("+82" + joinPhoneNumber.removePrefix("0"))
                .setTimeout(60, TimeUnit.SECONDS)
                .setActivity(activity)
                .setCallbacks(object: PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                    override fun onCodeSent(id: String, token: PhoneAuthProvider.ForceResendingToken) {
                        verificationId = id
                        showPhoneSendSuccessDialog = true
                        isLoading = false   // 로딩 종료
                    }
                    override fun onVerificationCompleted(cred: PhoneAuthCredential) {
                        phoneVerified = true
                        showCodeVerifySuccessDialog = true
                        isLoading = false   // 로딩 종료
                    }
                    override fun onVerificationFailed(e: FirebaseException) {
                        showPhoneSendErrorDialog = true
                        isLoading = false   // 로딩 종료
                    }
                })
                .build()
            PhoneAuthProvider.verifyPhoneNumber(options)
        }
    }

    /** 2. 사용자 입력 코드 검증 */
    fun verifyCode() {
        isLoading = true    // 로딩 시작
        val cred = PhoneAuthProvider.getCredential(verificationId, joinVerificationCode)
        FirebaseAuth.getInstance().signInWithCredential(cred)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // 1) 인증 완료 플래그
                    phoneVerified = true
                    showCodeVerifySuccessDialog = true

                    // 2) 성공적으로 로그인된 사용자의 UID 가져오기
                    userUid = FirebaseAuth.getInstance().currentUser?.uid.orEmpty()
                    isLoading = false   // 로딩 종료
                } else {
                    showCodeVerifyErrorDialog = true
                    isLoading = false   // 로딩 종료
                }
            }
    }

    /** 닉네임 검사 후 다이얼로그 또는 완료 */
    fun checkNicknameAndComplete() {
        viewModelScope.launch {
            // 서버에 빈 값 검사해도 좋지만, 우선 중복만
            val ok = userService.isNicknameAvailable(joinNickname)
            if (ok) {
                showCompleteDialog = true
            } else {
                showNicknameDuplicateDialog = true
            }
        }
    }

    // 회원가입 완료 시 사용자 정보를 Firestore에 저장하는 함수
    fun addUserToFirestore() {
        viewModelScope.launch {
            val user = UserModel(
                joinMethod = JoinMethod.PHONE.methodName,       // 가입 방법 (휴대폰)
                uid = userUid,                                  // Firebase 인증 UID
                id = joinId,                                    // 사용자 입력 아이디
                password = joinPassword,                        // 비밀번호
                nickname = joinNickname,                        // 닉네임
                phone = joinPhoneNumber,                        // 전화번호
                profileImageUrl = "",                           // 기본 프로필 이미지 (아직 없음)
                isRecordPublic = true,                          // 운동 기록 공개 여부
                isPicturePublic = true,                         // 사진 공개 여부
                createdAt = System.currentTimeMillis()          // 가입 시각
            )
            userService.addUser(user) // UserService를 통해 저장
        }
    }

    // 회원가입 화면2로 이동
    fun onNavigateToJoinScreen2() {
        application.navHostController.navigate(JoinScreenName.MAIN_SCREEN_STEP2.name)
    }

    // 회원가입 화면3로 이동
    fun onNavigateToJoinScreen3() {
        application.navHostController.navigate(JoinScreenName.MAIN_SCREEN_STEP3.name)
    }

    // 회원가입 화면4로 이동
    fun onNavigateToJoinScreen4() {
        application.navHostController.navigate(JoinScreenName.MAIN_SCREEN_STEP4.name)
    }

    // 로그인 화면으로 이동
    fun onNavigateToLoginScreen() {
        application.navHostController.navigate(MainScreenName.MAIN_SCREEN_LOGIN.name) {
            popUpTo(0) { inclusive = true } // 🔥 백스택 모두 제거
            launchSingleTop = true
        }
    }

    // 뒤로가기
    fun onNavigateBack() {
        application.navHostController.popBackStack()
    }

}