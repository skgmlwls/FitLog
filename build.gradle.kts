// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false

    // Hilt 의존성 주입 플러그인
    id("com.google.dagger.hilt.android") version "2.51.1" apply false

    // Firebase
    id("com.google.gms.google-services") version "4.4.3" apply false
}