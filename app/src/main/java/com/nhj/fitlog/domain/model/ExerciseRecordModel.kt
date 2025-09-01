package com.nhj.fitlog.domain.model

import com.nhj.fitlog.domain.vo.ExerciseDayExerciseVO
import com.nhj.fitlog.domain.vo.ExerciseDayRecordVO
import com.nhj.fitlog.domain.vo.ExerciseDaySetVO
import com.nhj.fitlog.utils.RecordIntensity

/** 하루 기록 상위 문서 */
data class ExerciseDayRecordModel(
    var recordId: String = "",
    var date: String = "",                       // "yyyy-MM-dd"
    var memo: String = "",
    var intensity: RecordIntensity = RecordIntensity.NORMAL,
    var imageUrlList: List<String> = emptyList(),// Storage 업로드 URL
    var exerciseCount: Int = 0,
    var deleteState: Boolean = false,
    var recordedAt: Long = System.currentTimeMillis(), // 사용자가 지정한 '작성 시각'
    var createdAt: Long = System.currentTimeMillis(),
    var volumeByCategory: Map<String, Double> = emptyMap(),     // 카테고리별 볼륨
) {
    fun toVO() = ExerciseDayRecordVO(
        recordId = recordId,
        date = date,
        memo = memo,
        intensity = intensity.name,
        imageUrlList = imageUrlList,
        exerciseCount = exerciseCount,
        deleteState = deleteState,
        recordedAt = recordedAt,
        createdAt = createdAt,
        volumeByCategory = volumeByCategory
    )
}

/** 하루 기록의 운동 항목 (하위 문서) */
data class ExerciseDayExerciseModel(
    var itemId: String = "",
    var exerciseTypeId: String = "",
    var exerciseName: String = "",
    var exerciseCategory: String = "",
    var order: Int = 0,
    var exerciseMemo: String = "",
    var setCount: Int = 0,
    var createdAt: Long = System.currentTimeMillis()
) {
    fun toVO() = ExerciseDayExerciseVO(
        itemId, exerciseTypeId, exerciseName, exerciseCategory, order, exerciseMemo, setCount, createdAt
    )
}

/** 하루 기록의 세트 (하위-하위 문서) */
data class ExerciseDaySetModel(
    var setId: String = "",
    var setNumber: Int = 1,
    var weight: Double = 0.0,
    var reps: Int = 0,
    var createdAt: Long = System.currentTimeMillis()
) {
    fun toVO() = ExerciseDaySetVO(setId, setNumber, weight, reps, createdAt)
}