package com.nhj.fitlog.domain.vo

import com.nhj.fitlog.domain.model.ExerciseTypeModel

// UI에서 사용할 운동 종류 데이터 VO 클래스
data class ExerciseTypeVO(
    val id: String,                         // 운동 종류 고유 ID
    val name: String,                       // 운동 이름
    val category: String,                   // 부위 카테고리
    val createdAt: Long                     // 등록 시간
) {
    fun toModel() = ExerciseTypeModel(id, name, category, createdAt)
}
