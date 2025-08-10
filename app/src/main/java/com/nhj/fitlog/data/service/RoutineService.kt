package com.nhj.fitlog.data.service

import com.nhj.fitlog.data.repository.RoutineRepository
import com.nhj.fitlog.domain.model.*
import com.nhj.fitlog.domain.vo.*
import com.nhj.fitlog.presentation.routine.add.RoutineExerciseWithSets
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class RoutineService(
    private val repository: RoutineRepository
) {
    // 이름 사용 가능 여부 (true = 사용 가능)
    suspend fun isNameAvailable(uid: String, name: String): Boolean =
        !repository.isNameDuplicated(uid, name)

    // 루틴 저장 (UI 모델을 VO로 변환해서 저장)
    suspend fun addRoutine(
        uid: String,
        routine: RoutineModel,
        items: List<RoutineExerciseWithSets>
    ): String {
        val routineVO = routine.toVO()

        // exercises -> VO 리스트
        val exercisesVO: List<RoutineExerciseVO> = items.map {
            it.exercise.toVO()
        }

        // itemId -> sets VO 목록 매핑
        val setsByItemId: Map<String, List<RoutineSetVO>> = items.associate { item ->
            val itemId = item.exercise.itemId
            itemId to item.sets.map { it.toVO() }
        }

        return repository.addRoutine(uid, routineVO, exercisesVO, setsByItemId)
    }

    // 메타 업데이트
    suspend fun updateRoutineMeta(uid: String, routine: RoutineModel) {
        repository.updateRoutineMeta(uid, routine.toVO())
    }

    // 목록/단건
    suspend fun fetchRoutines(uid: String): List<RoutineModel> =
        repository.getRoutines(uid).map { it.toModel() }

    suspend fun fetchRoutine(uid: String, routineId: String): RoutineModel? =
        repository.getRoutine(uid, routineId)?.toModel()

    // 삭제
    suspend fun deleteRoutine(uid: String, routineId: String) =
        repository.deleteRoutine(uid, routineId)


    /** 루틴 목록 실시간 흐름 */
    fun routinesFlow(uid: String): Flow<List<RoutineModel>> =
        repository.routinesFlow(uid).map { list -> list.map { it.toModel() } }

    /** 루틴 내 운동들 */
    suspend fun getRoutineExercises(uid: String, routineId: String): List<RoutineExerciseModel> =
        repository.getRoutineExercises(uid, routineId).map { it.toModel() }

    /** 특정 운동의 세트들 */
    suspend fun getRoutineSets(uid: String, routineId: String, itemId: String): List<RoutineSetModel> =
        repository.getRoutineSets(uid, routineId, itemId).map { it.toModel() }

    /** 최신 운동 메모 Map<exerciseTypeId, memo> */
    suspend fun getLatestExerciseMemos(uid: String, typeIds: List<String>): Map<String, String> =
        repository.getLatestExerciseMemos(uid, typeIds)

    // 이름 사용 가능(자기 자신 제외)
    suspend fun isNameAvailableForEdit(uid: String, name: String, routineId: String): Boolean =
        !repository.isNameDuplicatedExceptId(uid, name, routineId)

    // 루틴 전체 치환 저장 (메타 + exercises + sets)
    suspend fun replaceRoutine(
        uid: String,
        routine: RoutineModel,
        items: List<RoutineExerciseWithSets>
    ) {
        // 메타 업데이트
        repository.replaceRoutine(
            uid = uid,
            routine = routine.toVO(),
            exercises = items.map { it.exercise.toVO() },
            setsByItemId = items.associate { it.exercise.itemId to it.sets.map { s -> s.toVO() } }
        )
    }


}