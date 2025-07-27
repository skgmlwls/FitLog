package com.nhj.fitlog.domain.model

import com.nhj.fitlog.domain.vo.FriendVO

// Firebase에 저장할 친구 목록 모델 클래스
data class FriendModel(
    var friendUid: String = "",             // 친구 UID
    var nickname: String = "",              // 친구 닉네임
    var profileImageUrl: String = "",       // 친구 프로필 이미지 URL
    var isVisible: Boolean = true,          // 내 기록을 이 친구에게 공개할지 여부
    var addedAt: Long = System.currentTimeMillis() // 친구 추가 시간
) {
    fun toVO() = FriendVO(friendUid, nickname, profileImageUrl, isVisible, addedAt)
}
