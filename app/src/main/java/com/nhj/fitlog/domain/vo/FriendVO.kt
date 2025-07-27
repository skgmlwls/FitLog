package com.nhj.fitlog.domain.vo

import com.nhj.fitlog.domain.model.FriendModel

// UI에서 사용할 친구 목록 데이터 VO 클래스
data class FriendVO(
    val friendUid: String,                  // 친구 UID
    val nickname: String,                   // 친구 닉네임
    val profileImageUrl: String,            // 친구 프로필 이미지 URL
    val isVisible: Boolean,                 // 공개 여부
    val addedAt: Long                       // 친구 추가 시간
) {
    fun toModel() = FriendModel(friendUid, nickname, profileImageUrl, isVisible, addedAt)
}
