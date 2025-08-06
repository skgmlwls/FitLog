package com.nhj.fitlog.data.service

import com.nhj.fitlog.data.repository.ExerciseRepository
import com.nhj.fitlog.domain.model.ExerciseTypeModel
import com.nhj.fitlog.domain.vo.ExerciseTypeVO

class ExerciseService(
    private val repository: ExerciseRepository
) {
    // Repository 에서 불러온 운동 종류 반환
    suspend fun fetchExerciseTypes(uid: String): List<ExerciseTypeVO> =
        repository.getExerciseTypes(uid)

    // 이름 중복 여부 확인 (true: 사용 가능, false: 중복)
    suspend fun isNameAvailable(uid: String, name: String): Boolean =
        !repository.isNameDuplicated(uid, name)

    // 운동 종류 저장
    suspend fun addExerciseType(uid: String, model: ExerciseTypeModel) {
        val vo: ExerciseTypeVO = model.toVO()
        repository.addExerciseType(uid, vo)
    }

    // 운동 종류 정보 업데이트
    suspend fun updateExerciseType(uid: String, model: ExerciseTypeModel) {
        repository.updateExerciseType(uid, model.toVO())
    }

    // 운동 종류 삭제
    suspend fun deleteExerciseType(uid: String, id: String) {
        repository.deleteExerciseType(uid, id)
    }

    // 단일 운동 종류 조회
    suspend fun fetchExerciseType(uid: String, id: String): ExerciseTypeModel? =
        repository.getExerciseType(uid, id)?.toModel()

}