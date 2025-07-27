package com.nhj.fitlog.domain.vo

import com.nhj.fitlog.domain.model.RoutineModel

// UI에서 사용할 루틴 데이터 VO 클래스
data class RoutineVO(
    val routineId: String,                  // 루틴 고유 ID
    val name: String,                       // 루틴 이름
    val exercises: List<String>,            // 운동 ID 목록
    val createdAt: Long                     // 생성 시간
) {
    fun toModel() = RoutineModel(routineId, name, exercises, createdAt)
}
