package com.nhj.fitlog.data.service

import com.nhj.fitlog.data.repository.ExerciseRepository
import com.nhj.fitlog.domain.vo.ExerciseTypeVO

class ExerciseService(
    private val repository: ExerciseRepository
) {
    // Repository 에서 불러온 운동 종류 반환
    suspend fun fetchExerciseTypes(uid: String): List<ExerciseTypeVO> =
        repository.getExerciseTypes(uid)
}