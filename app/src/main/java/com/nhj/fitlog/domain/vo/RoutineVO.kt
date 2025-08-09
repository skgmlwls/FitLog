package com.nhj.fitlog.domain.vo

import com.nhj.fitlog.domain.model.RoutineExerciseModel
import com.nhj.fitlog.domain.model.RoutineModel
import com.nhj.fitlog.domain.model.RoutineSetModel

/** 루틴 (Firebase) */
data class RoutineVO(
    var routineId: String = "",
    var name: String = "",
    var memo: String = "",
    var exerciseCount: Int = 0,
    var createdAt: Long = System.currentTimeMillis()
) {
    fun toModel() = RoutineModel(routineId, name, memo, exerciseCount, createdAt)
}

/** 루틴의 운동 항목 (Firebase) */
data class RoutineExerciseVO(
    var itemId: String = "",
    var exerciseTypeId: String = "",
    var exerciseName: String = "",
    var order: Int = 0,
    var memo: String = "",
    var setCount: Int = 0,
    var createdAt: Long = System.currentTimeMillis()
) {
    fun toModel() = RoutineExerciseModel(itemId, exerciseTypeId, exerciseName, order, memo, setCount, createdAt)
}

/** 세트 (Firebase) */
data class RoutineSetVO(
    var setId: String = "",
    var setNumber: Int = 1,
    var weight: Double = 0.0,
    var reps: Int = 0,
    var createdAt: Long = System.currentTimeMillis()
) {
    fun toModel() = RoutineSetModel(setId, setNumber, weight, reps, createdAt)
}