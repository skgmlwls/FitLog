package com.nhj.fitlog.domain.model

import com.nhj.fitlog.domain.vo.FriendVO

// Firebase에 저장할 친구 목록 모델 클래스
data class FriendModel(
    var friendUid: String = "",
    var isVisible: Boolean = true,
    var addedAt: Long = System.currentTimeMillis()
) {
    fun toVO() = FriendVO(friendUid, isVisible, addedAt)
}
