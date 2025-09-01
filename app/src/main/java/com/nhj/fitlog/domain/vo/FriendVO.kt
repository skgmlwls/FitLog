package com.nhj.fitlog.domain.vo

import com.nhj.fitlog.domain.model.FriendModel

// UI에서 사용할 친구 목록 데이터 VO 클래스
data class FriendVO(
    var friendUid: String = "",
    var isVisible: Boolean = true,
    var addedAt: Long = System.currentTimeMillis()
) {
    fun toModel() = FriendModel(friendUid, isVisible, addedAt)
}
