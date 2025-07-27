package com.nhj.fitlog.domain.model

import com.nhj.fitlog.domain.vo.RoutineVO

// 사용자 루틴 정보를 Firebase에 저장할 모델 클래스
data class RoutineModel(
    var routineId: String = "",             // 루틴 고유 ID
    var name: String = "",                  // 루틴 이름
    var exercises: List<String> = emptyList(), // 루틴에 포함된 운동 ID 목록
    var createdAt: Long = System.currentTimeMillis() // 루틴 생성 시간
) {
    fun toVO() = RoutineVO(routineId, name, exercises, createdAt)
}
