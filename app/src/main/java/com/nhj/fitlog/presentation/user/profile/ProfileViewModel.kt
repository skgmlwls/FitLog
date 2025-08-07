package com.nhj.fitlog.presentation.user.profile

import android.content.Context
import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nhj.fitlog.FitLogApplication
import com.nhj.fitlog.data.service.UserService
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    @ApplicationContext context: Context,
    private val userService: UserService
) : ViewModel() {
    val application = context as FitLogApplication

    var nickName by mutableStateOf(" ")
    var profileImageUrl = MutableStateFlow<String?>(null)
    
    // 수정 닉네임
    var tempNickname by mutableStateOf(" ")

    // 로딩 상태
    var isLoading by mutableStateOf(false)

    // 오류 메시지 (null이면 에러 없음)
    var nicknameErrorMessage by mutableStateOf<String?>(null)

    // 닉네임 변경 다이얼로그
    var showNicknameDialog by mutableStateOf(false)

    init {
        // 사용자 정보 로드
        nickName = application.userModel.nickname
        profileImageUrl.value = application.userModel.profileImageUrl
    }

    // 사용자가 선택한 이미지 Uri 를 스토리지에 업로드하고 URL 을 Firestore 에 저장
    fun uploadProfileImage(uri: Uri) {
        viewModelScope.launch {
            val uid = application.userUid

            // 1) 스토리지에 업로드 및 URL 취득
            val imageUrl = userService.uploadProfileImage(uid, uri)

            // 2) Firestore 에 profileImageUrl 업데이트
            userService.updateProfileImage(uid, imageUrl)

            profileImageUrl.value = imageUrl
            isLoading = false
        }
    }

    // 닉네임 수정
    fun onNicknameChange() {
        viewModelScope.launch {
            val new = tempNickname.trim()
            isLoading = true
            when {
                new.isBlank() -> {
                    nicknameErrorMessage = "닉네임을 입력해주세요."
                    isLoading = false
                }
                new == nickName -> {
                    nicknameErrorMessage = "현재 닉네임과 같아요."
                    isLoading = false
                }
                !userService.isNicknameAvailable(new) -> {
                    nicknameErrorMessage = "이미 사용 중인 닉네임입니다."
                    isLoading = false
                }
                else -> {
                    // 업데이트
                    userService.updateNickname(application.userUid, new)
                    nickName = new
                    application.userModel = application.userModel.copy(nickname = new)
                    nicknameErrorMessage = null
                    isLoading = false
                    showNicknameDialog = false
                }
            }
        }
    }

    fun onBack() {
        application.navHostController.popBackStack()
    }

}