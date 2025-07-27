package com.nhj.fitlog.domain.model

import androidx.annotation.Keep
import com.nhj.fitlog.domain.vo.UserVO

// 사용자 정보를 Firebase에 저장할 모델 클래스
data class UserModel(
    var uid: String = "",                   // 사용자 고유 UID
    var nickname: String = "",              // 사용자 닉네임
    var phone: String = "",                 // 사용자 휴대폰 번호
    var profileImageUrl: String = "",       // 프로필 이미지 URL
    var isRecordPublic: Boolean = true,     // 운동 기록 공개 여부
    var isPicturePublic: Boolean = true,     // 사진 공개 여부
    var createdAt: Long = System.currentTimeMillis() // 계정 생성 시간
) {
    fun toVO(): UserVO = UserVO(uid, nickname, phone, profileImageUrl, isRecordPublic, isPicturePublic, createdAt)
}
