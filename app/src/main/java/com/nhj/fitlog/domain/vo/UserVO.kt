package com.nhj.fitlog.domain.vo

import com.nhj.fitlog.domain.model.UserModel

// 화면에서 사용할 사용자 데이터 VO 클래스
data class UserVO(
    val uid: String,                        // 사용자 고유 UID
    val nickname: String,                   // 사용자 닉네임
    val phone: String,                      // 사용자 휴대폰 번호
    val profileImageUrl: String,            // 프로필 이미지 URL
    val isRecordPublic: Boolean,            // 운동 기록 공개 여부
    var isPicturePublic: Boolean = true,     // 사진 공개 여부
    val createdAt: Long                     // 계정 생성 시간
) {
    fun toModel(): UserModel = UserModel(uid, nickname, phone, profileImageUrl, isRecordPublic, isPicturePublic, createdAt)
}