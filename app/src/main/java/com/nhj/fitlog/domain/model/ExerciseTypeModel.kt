package com.nhj.fitlog.domain.model

import com.nhj.fitlog.domain.vo.ExerciseTypeVO

// 운동 종류(부위/이름 등)를 Firebase에 저장할 모델 클래스
data class ExerciseTypeModel(
    var id: String = "",                    // 운동 종류 고유 ID
    var name: String = "",                  // 운동 이름
    var category: String = "",              // 부위 카테고리 (예: 가슴, 등 등)
    var createdAt: Long = System.currentTimeMillis() // 등록 시간
) {
    fun toVO() = ExerciseTypeVO(id, name, category, createdAt)
}
