package com.nhj.fitlog.domain.vo

import com.nhj.fitlog.domain.model.UserModel

// UI에서 사용할 사용자 정보 VO 클래스
data class UserVO(
    val joinMethod: String = "",                        // 가입 방법 (휴대폰, 구글, 카카오)
    val uid: String = "",                               // Firebase Auth 사용자 ID
    val id: String = "",                                // 사용자 ID
    val password: String = "",                          // 비밀번호
    val email: String = "",                             // 이메일
    val nickname: String = "",                          // 닉네임
    val phone: String = "",                             // 휴대폰 번호
    val profileImageUrl: String = "",                   // 프로필 이미지 URL
    val isRecordPublic: Boolean = true,                 // 운동 기록 공개 여부
    val isPicturePublic: Boolean = true,                // 사진 공개 여부
    val createdAt: Long = 0L                            // 가입 시간
) {
    fun toModel(): UserModel = UserModel(
        joinMethod = joinMethod,
        uid = uid,
        id = id,
        password = password,
        email = email,
        nickname = nickname,
        phone = phone,
        profileImageUrl = profileImageUrl,
        isRecordPublic = isRecordPublic,
        isPicturePublic = isPicturePublic,
        createdAt = createdAt
    )
}