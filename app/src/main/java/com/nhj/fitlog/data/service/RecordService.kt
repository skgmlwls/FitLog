package com.nhj.fitlog.data.service

import android.net.Uri
import com.nhj.fitlog.data.repository.RecordRepository
import com.nhj.fitlog.domain.model.ExerciseDayExerciseModel
import com.nhj.fitlog.domain.model.ExerciseDayRecordModel
import com.nhj.fitlog.domain.model.ExerciseDaySetModel
import com.nhj.fitlog.domain.vo.ExerciseDayExerciseVO
import com.nhj.fitlog.domain.vo.ExerciseDaySetVO
import com.nhj.fitlog.presentation.record.record_detail.UserPublicFlags
import com.nhj.fitlog.utils.DayRecordDetail
import com.nhj.fitlog.utils.RoutineExerciseWithSets

class RecordService(
    private val recordRepository: RecordRepository
) {
    /**
     * 하루 기록 생성:
     * 1) 이미지 업로드 → URL
     * 2) 상위 문서(meta) + 하위(exercises/sets) 저장
     *   - recordId가 비어있으면 자동 생성
     */
    suspend fun addDayRecord(
        uid: String,
        record: ExerciseDayRecordModel,
        items: List<RoutineExerciseWithSets>,
        localImageUris: List<Uri>
    ) {
        val recordId = if (record.recordId.isNotBlank()) record.recordId else java.util.UUID.randomUUID().toString()

        // 1) 이미지 업로드
        val imageUrls = recordRepository.uploadRecordImages(uid, recordId, localImageUris)

        // 2) 모델 업데이트 (이미지URL/운동 개수/recordId)
        val fixedRecord = record.copy(
            recordId = recordId,
            imageUrlList = imageUrls,
            exerciseCount = items.size
        )
        val recordVO = fixedRecord.toVO()

        // 3) exercises/sets VO 변환
        val exercisesVO: List<ExerciseDayExerciseVO> = items.map { it.exercise.run {
            ExerciseDayExerciseModel(
                itemId = itemId,
                exerciseTypeId = exerciseTypeId,
                exerciseName = exerciseName,
                exerciseCategory = exerciseCategory,
                order = order,
                exerciseMemo = exerciseMemo,
                setCount = it.sets.size,
                createdAt = createdAt,
            ).toVO()
        } }

        val setsByItemId: Map<String, List<ExerciseDaySetVO>> = items.associate { item ->
            val itemId = item.exercise.itemId
            itemId to item.sets.map { s ->
                ExerciseDaySetModel(
                    setId = s.setId,
                    setNumber = s.setNumber,
                    weight = s.weight,
                    reps = s.reps,
                    createdAt = s.createdAt
                ).toVO()
            }
        }

        // 4) 배치 생성
        recordRepository.addRecordAll(uid, recordVO, exercisesVO, setsByItemId)
    }

    suspend fun getAllExerciseRecords(uid: String) = recordRepository.getAllExerciseRecords(uid)
    // ✅ 상위 문서(메타) 전체 가져오기
    suspend fun getAllDayRecords(uid: String): List<ExerciseDayRecordModel> {
        return recordRepository.getAllDayRecords(uid)
            .map { it.toModel() }
            .sortedByDescending { it.recordedAt } // 최신 먼저
    }

    // 기록 상세 내용 가져 오기
    suspend fun getDayRecordDetail(uid: String, recordId: String): DayRecordDetail =
        recordRepository.getRecordDetail(uid, recordId)

    /**
     * 하루 기록 수정:
     * 1) 새 로컬 이미지 업로드 → URL
     * 2) keptRemoteUrls + 신규 URL을 합쳐 imageUrlList 재구성(최대 3장 가드)
     * 3) 상위 문서(meta) 갱신 + 하위(exercises/sets) upsert/삭제 동기화
     */
    suspend fun updateDayRecord(
        uid: String,
        recordId: String,
        record: ExerciseDayRecordModel,
        items: List<RoutineExerciseWithSets>,
        localNewImageUris: List<Uri>,
        keptRemoteUrls: List<String>
    ) {
        // 1) 새 이미지 업로드
        val newImageUrls = recordRepository.uploadRecordImages(uid, recordId, localNewImageUris)

        // 2) 최종 이미지 목록 (보존 + 신규, 최대 3장 안전 가드)
        val finalImageUrls = (keptRemoteUrls + newImageUrls).take(3)

        // 3) 상위 모델 보정
        val fixedRecord = record.copy(
            recordId = recordId,
            imageUrlList = finalImageUrls,
            exerciseCount = items.size
        )

        // 4) exercises/sets VO 변환
        val exercisesVO: List<ExerciseDayExerciseVO> = items.map { pack ->
            with(pack.exercise) {
                ExerciseDayExerciseModel(
                    itemId = itemId,
                    exerciseTypeId = exerciseTypeId,
                    exerciseName = exerciseName,
                    exerciseCategory = exerciseCategory,
                    order = order,
                    exerciseMemo = exerciseMemo,
                    setCount = pack.sets.size,
                    createdAt = createdAt
                ).toVO()
            }
        }

        val setsByItemId: Map<String, List<ExerciseDaySetVO>> = items.associate { pack ->
            val itemId = pack.exercise.itemId
            itemId to pack.sets.map { s ->
                ExerciseDaySetModel(
                    setId = s.setId,
                    setNumber = s.setNumber,
                    weight = s.weight,
                    reps = s.reps,
                    createdAt = s.createdAt
                ).toVO()
            }
        }

        // 5) 레포지토리로 일괄 동기화 (upsert + 삭제)
        recordRepository.updateRecordAll(
            uid = uid,
            record = fixedRecord.toVO(),
            exercises = exercisesVO,
            setsByItemId = setsByItemId
        )
    }

    // 기록 삭제
    suspend fun deleteDayRecord(uid: String, recordId: String) {
        recordRepository.deleteDayRecord(uid, recordId)
    }

    /** 공개 플래그 조회 (레포지토리 위임) */
    suspend fun getUserPublicFlags(uid: String): UserPublicFlags {
        return recordRepository.getUserPublicFlags(uid)
    }

}