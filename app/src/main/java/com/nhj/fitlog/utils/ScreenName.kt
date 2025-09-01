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

enum class RoutineScreenName {
    // 루틴 리스트
    ROUTINE_LIST_SCREEN,
    // 루틴 추가
    ROUTINE_ADD_SCREEN,
    // 루틴 추가 운동 리스트
    ROUTINE_ADD_LIST_SCREEN,
    // 루틴 상세
    ROUTINE_DETAIL_SCREEN,
    // 루틴 수정
    ROUTINE_DETAIL_EDIT_SCREEN,
}

enum class RecordScreenName {
    // 운동 기록 캘린더 화면
    RECORD_CALENDAR_SCREEN,
    // 운동 기록 화면
    RECORD_EXERCISE_SCREEN,
    // 운동 기록 상세 화면
    RECORD_DETAIL_SCREEN,
    // 운동 기록 수정 화면
    RECORD_EDIT_SCREEN,

}

enum class FriendScreenName {
    // 친구 목록 리스트 화면
    FRIEND_LIST_SCREEN,
    // 친구 추가 화면
    FRIEND_REQUESTS_SCREEN,
    // 친구 상세 화면
    FRIEND_DETAIL_SCREEN,
}

enum class UserProfileScreenName {
    // 프로필 화면
    PROFILE_SCREEN,
}