package com.nhj.fitlog

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.navigation.NavHostController
import com.nhj.fitlog.domain.model.UserModel
import dagger.hilt.android.HiltAndroidApp

// Hilt를 사용하려면 이 어노테이션을 애플리케이션의 Application 클래스에 선언해야 함
@HiltAndroidApp
class FitLogApplication : Application() {

    // 네비게이션
    lateinit var navHostController: NavHostController

    // 유저 UID
    lateinit var userUid: String

    // 유저 정보
    // 실시간 변화를 위해 mutableStateOf
    var userModel by mutableStateOf(UserModel())

    // 1) DataStore 싱글톤 선언
    val dataStore by preferencesDataStore(name = "login_prefs")

    companion object {
        // 2) Preferences Key 정의도 여기로 이동
        val AUTO_LOGIN_UID_KEY = stringPreferencesKey("auto_login_uid")
    }

}