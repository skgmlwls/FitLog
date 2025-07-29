package com.nhj.fitlog.domain.model

import com.nhj.fitlog.domain.vo.UserVO

// Firebase Firestore에 저장할 사용자 모델 클래스
data class UserModel(
    var joinMethod: String = "",             // 가입 방법 (Google, Kakao, Phone)
    var uid: String = "",                    // Firebase Auth 고유 사용자 ID
    var id: String = "",                     // 사용자 입력 아이디 (중복 허용 안됨)
    val password: String = "",                    // 비밀번호
    val email: String = "",                  // 이메일
    var nickname: String = "",               // 닉네임
    var phone: String = "",                  // 휴대폰 번호 (인증된 값, 중복 불가)
    var profileImageUrl: String = "",        // 프로필 이미지 URL
    var recordPublic: Boolean = false,      // 운동 기록 공개 여부
    var picturePublic: Boolean = false,     // 사진 공개 여부
    var createdAt: Long = System.currentTimeMillis() // 가입 시간
) {
    fun toVO(): UserVO = UserVO(
        joinMethod = joinMethod,
        uid = uid,
        id = id,
        password = password,
        email = email,
        nickname = nickname,
        phone = phone,
        profileImageUrl = profileImageUrl,
        recordPublic = recordPublic,
        picturePublic = picturePublic,
        createdAt = createdAt
    )
}