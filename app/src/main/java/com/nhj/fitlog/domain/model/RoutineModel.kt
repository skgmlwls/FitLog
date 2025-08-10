package com.nhj.fitlog.domain.model

import com.nhj.fitlog.domain.vo.RoutineExerciseVO
import com.nhj.fitlog.domain.vo.RoutineSetVO
import com.nhj.fitlog.domain.vo.RoutineVO

data class RoutineModel(
    var routineId: String = "",
    var name: String = "",
    var memo: String = "",
    var exerciseCount: Int = 0,
    var createdAt: Long = System.currentTimeMillis()
) {
    fun toVO() = RoutineVO(routineId, name, memo, exerciseCount, createdAt)
}

/** 루틴의 운동 항목 (UI) */
data class RoutineExerciseModel(
    var itemId: String = "",
    var exerciseTypeId: String = "",
    var exerciseName: String = "",
    var exerciseCategory: String = "",
    var order: Int = 0,
    var exerciseMemo: String = "",
    var setCount: Int = 0,
    var createdAt: Long = System.currentTimeMillis()
) {
    fun toVO() = RoutineExerciseVO(itemId, exerciseTypeId, exerciseName, exerciseCategory, order, exerciseMemo, setCount, createdAt)
}

/** 세트 (UI) */
data class RoutineSetModel(
    var setId: String = "",
    var setNumber: Int = 1,
    var weight: Double = 0.0,
    var reps: Int = 0,
    var createdAt: Long = System.currentTimeMillis()
) {
    fun toVO() = RoutineSetVO(setId, setNumber, weight, reps, createdAt)
}