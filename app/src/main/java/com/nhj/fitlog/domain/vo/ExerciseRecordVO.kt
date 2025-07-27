package com.nhj.fitlog.domain.vo

import com.nhj.fitlog.domain.model.ExerciseRecordModel
import com.nhj.fitlog.domain.model.ExerciseSetModel

// UI에서 사용할 운동 기록 데이터 VO 클래스
data class ExerciseRecordVO(
    val recordId: String,
    val date: String,
    val exerciseTypeId: String,
    val sets: List<ExerciseSetVO>,
    val memo: String,
    val imageUrl: String,
    val createdAt: Long
) {
    fun toModel() = ExerciseRecordModel(recordId, date, exerciseTypeId, sets.map { it.toModel() }, memo, imageUrl, createdAt)
}

data class ExerciseSetVO(
    val setNumber: Int,
    val reps: Int,
    val weight: Double
) {
    fun toModel() = ExerciseSetModel(setNumber, reps, weight)
}