package com.nhj.fitlog.utils

enum class MainScreenName {
    // 스플래시 화면
    MAIN_SCREEN_SPLASH,
    // 메인 화면
    MAIN_SCREEN_HOME,
    // 로그인 화면
    MAIN_SCREEN_LOGIN,
    // 설정 화면
    MAIN_SCREEN_SETTING,
}

// 회원가입 스크린 이름
enum class JoinScreenName {
    // 회원 가입 1 화면, 아이디 입력 받기
    MAIN_SCREEN_STEP1,
    // 회원 가입 2 화면, 비밀번호 입력 받기
    MAIN_SCREEN_STEP2,
    // 회원 가입 3 화면, 문자 인증
    MAIN_SCREEN_STEP3,
    // 회원 가입 4 화면, 닉네임 받기
    MAIN_SCREEN_STEP4,
    // 구글 닉네임 화면
    SOCIAL_NICKNAME_SCREEN,
}

enum class ExerciseScreenName {
    // 운동 타입 스크린
    EXERCISE_TYPE_SCREEN,
    // 운동 추가 스크린
    EXERCISE_ADD_SCREEN,
    // 운동 상세 스크린
    EXERCISE_DETAIL_SCREEN,
    // 운동 수정 스크린
    EXERCISE_DETAIL_EDIT_SCREEN,
    // 운동 이전 기록 스크린
    EXERCISE_HISTORY_SCREEN,
    // 운동 이전 기록 상세 스크린
    EXERCISE_HISTORY_DETAIL_SCREEN,
}