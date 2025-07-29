package com.nhj.fitlog.presentation.setting

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.datastore.preferences.core.edit
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nhj.fitlog.FitLogApplication
import com.nhj.fitlog.data.service.UserService
import com.nhj.fitlog.domain.model.UserModel
import com.nhj.fitlog.utils.MainScreenName
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val userService: UserService
) : ViewModel() {
    private val application = context as FitLogApplication

    // 공개 범위 상태
    var recordPublic by mutableStateOf(application.userModel.recordPublic)    // 기록
    var photoPublic by mutableStateOf(application.userModel.picturePublic)    // 사진

    // 토글 메소드
    // 기록 공개 여부 토글
    fun toggleRecordPublic() {
        recordPublic = !recordPublic
        application.userModel = application.userModel.copy(recordPublic = recordPublic)

        // Firestore에 업데이트
        viewModelScope.launch {
            userService.updateRecordVisibility(application.userUid, recordPublic)
        }
    }
    // 사진 공개 여부 토글
    fun togglePhotoPublic() {
        photoPublic = !photoPublic
        application.userModel = application.userModel.copy(picturePublic = photoPublic)

        // Firestore에 업데이트
        viewModelScope.launch {
            userService.updatePictureVisibility(application.userUid, photoPublic)
        }
    }

    // 프로필 화면으로 이동
    fun onNavigateToProfile() {
        // application.navHostController.navigate("PROFILE_SCREEN")  // 실제 경로로 교체
    }

    // 로그아웃
    fun onLogout() {
        viewModelScope.launch {
            // 1) DataStore 에서 UID 삭제
            application.dataStore.edit { prefs ->
                prefs.remove(FitLogApplication.AUTO_LOGIN_UID_KEY)
            }
            // 2) 메모리 초기화
            application.userUid = ""
            application.userModel = UserModel()
            // 3) 백스택 클리어 후 로그인 화면으로
            application.navHostController.navigate(MainScreenName.MAIN_SCREEN_LOGIN.name) {
                popUpTo(application.navHostController.graph.id) { inclusive = true }
                launchSingleTop = true
            }
        }
    }

    // 뒤로가기
    fun onBack() {
        application.navHostController.popBackStack()
    }
}