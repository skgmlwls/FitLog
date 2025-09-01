package com.nhj.fitlog.domain.vo

import com.nhj.fitlog.domain.model.ExerciseDayExerciseModel
import com.nhj.fitlog.domain.model.ExerciseDayRecordModel
import com.nhj.fitlog.domain.model.ExerciseDaySetModel
import com.nhj.fitlog.utils.RecordIntensity

/** 하루 기록 (Firebase 문서) */
data class ExerciseDayRecordVO(
    var recordId: String = "",
    var date: String = "",
    var memo: String = "",
    var intensity: String = RecordIntensity.NORMAL.name, // 문자열로 저장
    var imageUrlList: List<String> = emptyList(),
    var exerciseCount: Int = 0,
    var deleteState: Boolean = false,
    var recordedAt: Long = System.currentTimeMillis(), // 사용자가 지정한 '작성 시각'
    var createdAt: Long = System.currentTimeMillis(),
    var volumeByCategory: Map<String, Double> = emptyMap(),     // 카테고리별 볼륨
) {
    fun toModel() = ExerciseDayRecordModel(
        recordId, date, memo, RecordIntensity.valueOf(intensity), imageUrlList, exerciseCount, deleteState, recordedAt, createdAt, volumeByCategory
    )
}

/** 하루 기록의 운동 항목 (Firebase) */
data class ExerciseDayExerciseVO(
    var itemId: String = "",
    var exerciseTypeId: String = "",
    var exerciseName: String = "",
    var exerciseCategory: String = "",
    var order: Int = 0,
    var exerciseMemo: String = "",
    var setCount: Int = 0,
    var createdAt: Long = System.currentTimeMillis()
) {
    fun toModel() = ExerciseDayExerciseModel(itemId, exerciseTypeId, exerciseName, exerciseCategory, order, exerciseMemo, setCount, createdAt)
}

/** 하루 기록의 세트 (Firebase) */
data class ExerciseDaySetVO(
    var setId: String = "",
    var setNumber: Int = 1,
    var weight: Double = 0.0,
    var reps: Int = 0,
    var createdAt: Long = System.currentTimeMillis()
) {
    fun toModel() = ExerciseDaySetModel(setId, setNumber, weight, reps, createdAt)
}