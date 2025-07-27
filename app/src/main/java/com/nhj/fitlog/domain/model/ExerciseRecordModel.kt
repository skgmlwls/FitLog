package com.nhj.fitlog.domain.model

import com.nhj.fitlog.domain.vo.ExerciseRecordVO
import com.nhj.fitlog.domain.vo.ExerciseSetVO

// Firebase에 저장할 운동 기록 모델 클래스
data class ExerciseRecordModel(
    var recordId: String = "",
    var date: String = "",
    var exerciseTypeId: String = "",
    var sets: List<ExerciseSetModel> = emptyList(), // 여러 세트 정보 저장
    var memo: String = "",
    var imageUrl: String = "",
    var createdAt: Long = System.currentTimeMillis()
) {
    fun toVO() = ExerciseRecordVO(recordId, date, exerciseTypeId, sets.map { it.toVO() }, memo, imageUrl, createdAt
    )
}

// Firebase에 저장할 세트 정보 모델 클래스
data class ExerciseSetModel(
    var setNumber: Int = 0,
    var reps: Int = 0,
    var weight: Double = 0.0
) {
    fun toVO() = ExerciseSetVO(setNumber, reps, weight)
}
