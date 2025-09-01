package com.nhj.fitlog.data.repository

import android.content.ContentResolver
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.storage.FirebaseStorage
import com.nhj.fitlog.domain.model.RoutineExerciseModel
import com.nhj.fitlog.domain.model.RoutineSetModel
import com.nhj.fitlog.domain.vo.ExerciseDayExerciseVO
import com.nhj.fitlog.domain.vo.ExerciseDayRecordVO
import com.nhj.fitlog.domain.vo.ExerciseDaySetVO
import com.nhj.fitlog.presentation.record.record_detail.UserPublicFlags
import com.nhj.fitlog.utils.DayRecordDetail
import com.nhj.fitlog.utils.ImportRoutine
import com.nhj.fitlog.utils.RoutineExerciseWithSets
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.util.UUID
import kotlin.math.max

class RecordRepository(
    private val contentResolver: ContentResolver
) {
    private val db = FirebaseFirestore.getInstance()
    private val storage = FirebaseStorage.getInstance()

    private fun recordDoc(uid: String, recordId: String) =
        db.collection("users").document(uid)
            .collection("exerciseRecords").document(recordId)

    /**
     * 이미지 업로드(최적화 버전)
     * - Uri → 다운샘플링 decode → JPEG 압축(품질 80) → putBytes
     * - 긴 변 1280px 제한 (원본이 더 작으면 그대로)
     * - 동시 업로드 최대 3개 (느린 네트워크에서 과부하 방지)
     */
    suspend fun uploadRecordImages(uid: String, recordId: String, uris: List<Uri>): List<String> =
        withContext(Dispatchers.IO) {
            if (uris.isEmpty()) return@withContext emptyList()

            val root = storage.reference.child("records/$uid/$recordId/images")
            val semaphore = Semaphore(3) // 동시 3개 제한

            uris.mapIndexed { idx, uri ->
                async {
                    semaphore.withPermit {
                        val bytes = compressImageToJpegBytes(uri, maxSizePx = 1280, quality = 80)
                        val ref = root.child("${System.currentTimeMillis()}_$idx.jpg")
                        ref.putBytes(bytes).await()
                        ref.downloadUrl.await().toString()
                    }
                }
            }.awaitAll()
        }

    /** Uri 이미지를 다운샘플+압축해서 ByteArray 로 반환 */
    private fun compressImageToJpegBytes(
        uri: Uri,
        maxSizePx: Int,
        quality: Int
    ): ByteArray {
        // 1) 크기만 먼저 읽기
        val opt = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        contentResolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, opt) }

        val (w, h) = opt.outWidth to opt.outHeight
        if (w <= 0 || h <= 0) {
            // 디코딩 실패하면 그냥 원본 스트림 업로드(포기)
            return contentResolver.openInputStream(uri)?.readBytes() ?: ByteArray(0)
        }

        // 2) 샘플링 비율 계산(가까운 2의 제곱으로)
        val longest = max(w, h)
        var inSampleSize = 1
        while ((longest / inSampleSize) > maxSizePx) inSampleSize *= 2

        val decodeOpt = BitmapFactory.Options().apply { this.inSampleSize = inSampleSize }
        val bmp = contentResolver.openInputStream(uri)?.use {
            BitmapFactory.decodeStream(it, null, decodeOpt)
        } ?: return ByteArray(0)

        // 3) 최대 변 1280 에 맞춰 추가 스케일 (샘플링 후에도 큰 경우)
        val scale = maxOf(bmp.width, bmp.height).toFloat() / maxSizePx
        val scaled = if (scale > 1f) {
            val tw = (bmp.width / scale).toInt()
            val th = (bmp.height / scale).toInt()
            Bitmap.createScaledBitmap(bmp, tw, th, true)
        } else bmp

        // 4) JPEG 압축
        val baos = java.io.ByteArrayOutputStream()
        scaled.compress(Bitmap.CompressFormat.JPEG, quality, baos)

        if (scaled !== bmp) bmp.recycle()
        scaled.recycle()

        return baos.toByteArray()
    }

    /** 한 번에 생성 (메타 + exercises + sets) */
    suspend fun addRecordAll(
        uid: String,
        record: ExerciseDayRecordVO,
        exercises: List<ExerciseDayExerciseVO>,
        setsByItemId: Map<String, List<ExerciseDaySetVO>>
    ) {
        val doc = if (record.recordId.isNotBlank()) {
            recordDoc(uid, record.recordId)
        } else {
            db.collection("users").document(uid)
                .collection("exerciseRecords").document()
        }
        val fixedRecord = record.copy(recordId = doc.id)
        val batch = db.batch()
        batch.set(doc, fixedRecord)

        val exCol = doc.collection("exercises")
        exercises.forEach { ex ->
            val exId = if (ex.itemId.isBlank()) UUID.randomUUID().toString() else ex.itemId
            val exRef = exCol.document(exId)
            batch.set(exRef, ex.copy(itemId = exId))

            val sets = setsByItemId[exId].orEmpty()
            val setsCol = exRef.collection("sets")
            sets.forEach { s ->
                val setId = if (s.setId.isBlank()) UUID.randomUUID().toString() else s.setId
                batch.set(setsCol.document(setId), s.copy(setId = setId))
            }
        }
        batch.commit().await()
    }

    // 전체 exerciseRecords를 ImportRoutine 리스트로 변환 (deleteState=false 필터 추가)
    suspend fun getAllExerciseRecords(uid: String): List<ImportRoutine> = coroutineScope {
        val recordsCol = db.collection("users").document(uid)
            .collection("exerciseRecords")

        // 🔽 여기! 삭제되지 않은 문서만
        val recordDocs = recordsCol
            .whereEqualTo("deleteState", false)
            .get()
            .await()
            .documents

        recordDocs.map { doc ->
            async {
                val recordId = doc.id
                val dateLabel = (doc.getString("date") ?: recordId)

                val exSnap = doc.reference.collection("exercises")
                    .orderBy("order")
                    .get()
                    .await()

                val items = exSnap.documents.map { exDoc ->
                    async {
                        val exVO = exDoc.toObject(ExerciseDayExerciseVO::class.java) ?: return@async null
                        val setsSnap = exDoc.reference.collection("sets")
                            .orderBy("setNumber")
                            .get()
                            .await()

                        val sets = setsSnap.documents.mapNotNull { sDoc ->
                            val sVO = sDoc.toObject(ExerciseDaySetVO::class.java) ?: return@mapNotNull null
                            RoutineSetModel(
                                setId = sVO.setId,
                                setNumber = sVO.setNumber,
                                weight = sVO.weight,
                                reps = sVO.reps,
                                createdAt = sVO.createdAt
                            )
                        }.toMutableList()

                        val exModel = RoutineExerciseModel(
                            itemId = exVO.itemId,
                            exerciseTypeId = exVO.exerciseTypeId,
                            exerciseName = exVO.exerciseName,
                            exerciseCategory = exVO.exerciseCategory,
                            order = exVO.order,
                            exerciseMemo = exVO.exerciseMemo,
                            setCount = sets.size,
                            createdAt = exVO.createdAt
                        )
                        RoutineExerciseWithSets(exModel, sets)
                    }
                }.awaitAll().filterNotNull()

                ImportRoutine(
                    routineId = recordId,
                    name = dateLabel,
                    items = items
                )
            }
        }.awaitAll()
    }

    // 하루 기록 메타 리스트 (deleteState=false 필터 + 정렬)
    // 주의: where + orderBy 조합은 Firestore에서 복합 인덱스가 필요할 수 있음
    suspend fun getAllDayRecords(uid: String): List<ExerciseDayRecordVO> = withContext(Dispatchers.IO) {
        val col = db.collection("users").document(uid).collection("exerciseRecords")

        val snap = col
            .whereEqualTo("deleteState", false)           // 🔽 삭제되지 않은 문서만
            .orderBy("recordedAt", Query.Direction.ASCENDING) // 기존 정렬 유지(서비스에서 역정렬한다면 ASC로 유지)
            .get()
            .await()

        snap.documents.mapNotNull { doc ->
            val vo = doc.toObject(ExerciseDayRecordVO::class.java) ?: return@mapNotNull null
            vo.copy(recordId = if (vo.recordId.isBlank()) doc.id else vo.recordId)
        }
    }

    // 기록 상세 내용 가져 오기
    suspend fun getRecordDetail(uid: String, recordId: String): DayRecordDetail = withContext(Dispatchers.IO) {
        val doc = recordDoc(uid, recordId).get().await()
        val vo = doc.toObject(ExerciseDayRecordVO::class.java)
            ?: throw IllegalStateException("Record not found: $recordId")
        val record = vo.toModel()

        val exSnap = doc.reference.collection("exercises")
            .orderBy("order")
            .get()
            .await()

        val items = exSnap.documents.map { exDoc ->
            async {
                val exVO = exDoc.toObject(ExerciseDayExerciseVO::class.java) ?: return@async null
                val setsSnap = exDoc.reference.collection("sets")
                    .orderBy("setNumber")
                    .get()
                    .await()
                val sets = setsSnap.documents.mapNotNull { sDoc ->
                    val sVO = sDoc.toObject(ExerciseDaySetVO::class.java) ?: return@mapNotNull null
                    com.nhj.fitlog.domain.model.RoutineSetModel(
                        setId = sVO.setId,
                        setNumber = sVO.setNumber,
                        weight = sVO.weight,
                        reps = sVO.reps,
                        createdAt = sVO.createdAt
                    )
                }.toMutableList()

                val exModel = com.nhj.fitlog.domain.model.RoutineExerciseModel(
                    itemId = exVO.itemId,
                    exerciseTypeId = exVO.exerciseTypeId,
                    exerciseName = exVO.exerciseName,
                    exerciseCategory = exVO.exerciseCategory,
                    order = exVO.order,
                    exerciseMemo = exVO.exerciseMemo,
                    setCount = sets.size,
                    createdAt = exVO.createdAt
                )
                com.nhj.fitlog.utils.RoutineExerciseWithSets(exModel, sets)
            }
        }.awaitAll().filterNotNull()

        DayRecordDetail(record = record, items = items)
    }

    // ... (기존 코드 유지)

    /**
     * 메타 + exercises/sets 동기화(업데이트):
     * - 전달된 exercises/sets 로 문서 상태를 일치시킴
     * - 누락된 exercise/set 은 삭제, 전달된 항목은 upsert
     */
    suspend fun updateRecordAll(
        uid: String,
        record: ExerciseDayRecordVO,
        exercises: List<ExerciseDayExerciseVO>,
        setsByItemId: Map<String, List<ExerciseDaySetVO>>
    ) = withContext(Dispatchers.IO) {
        require(record.recordId.isNotBlank()) { "recordId is blank for update" }

        val doc = recordDoc(uid, record.recordId)

        // 0) 현재 상태 로드
        val exSnap = doc.collection("exercises").get().await()
        val existingExIds = exSnap.documents.map { it.id }.toSet()

        // 새 목록 (id 보정)
        val normalizedExercises = exercises.map { ex ->
            if (ex.itemId.isBlank()) ex.copy(itemId = UUID.randomUUID().toString()) else ex
        }
        val newExIds = normalizedExercises.map { it.itemId }.toSet()

        // 삭제 대상 exercise
        val exIdsToDelete = existingExIds - newExIds

        // 각 keep exercise 의 set 삭제 대상 계산
        // (신규 set 은 setId 가 비어있을 수 있으므로, 삭제 판단은 "제공된 setId(비어있지 않은 것들)"만 비교)
        val setsToDeleteByExId = mutableMapOf<String, List<String>>()
        for (exId in (existingExIds intersect newExIds)) {
            val existingSetIds = doc.collection("exercises").document(exId)
                .collection("sets").get().await().documents.map { it.id }.toSet()
            val providedSetIds = setsByItemId[exId].orEmpty()
                .mapNotNull { it.setId.takeIf { id -> id.isNotBlank() } }
                .toSet()
            val toDelete = (existingSetIds - providedSetIds).toList()
            if (toDelete.isNotEmpty()) setsToDeleteByExId[exId] = toDelete
        }

        // 1) 배치 실행
        val batch = db.batch()

        // 메타 갱신
        batch.set(doc, record.copy(recordId = record.recordId))

        // exercises upsert
        val exCol = doc.collection("exercises")
        normalizedExercises.forEach { ex ->
            val exRef = exCol.document(ex.itemId)
            // setCount 는 호출부에서 계산되므로 그대로 사용
            batch.set(exRef, ex)

            // sets upsert
            val sets = setsByItemId[ex.itemId].orEmpty()
            val setsCol = exRef.collection("sets")
            sets.forEach { s ->
                val realId = if (s.setId.isBlank()) UUID.randomUUID().toString() else s.setId
                batch.set(setsCol.document(realId), s.copy(setId = realId))
            }
        }

        // sets 삭제
        setsToDeleteByExId.forEach { (exId, setIds) ->
            val setsCol = exCol.document(exId).collection("sets")
            setIds.forEach { sid ->
                batch.delete(setsCol.document(sid))
            }
        }

        // exercises 삭제(하위 sets 먼저 모두 삭제)
        exIdsToDelete.forEach { exId ->
            val exRef = exCol.document(exId)
            val setsSnap = exRef.collection("sets").get().await()
            setsSnap.documents.forEach { sDoc ->
                batch.delete(sDoc.reference)
            }
            batch.delete(exRef)
        }

        batch.commit().await()
    }

    // 삭제: deleteState=true
    suspend fun deleteDayRecord(uid: String, recordId: String) {
        recordDoc(uid, recordId)
            .update(
                mapOf(
                    "deleteState" to true,
                )
            )
            .await()
    }

    /** /users/{uid} 문서에서 공개 플래그만 읽어오기 */
    suspend fun getUserPublicFlags(uid: String): UserPublicFlags = withContext(Dispatchers.IO) {
        val doc = db.collection("users").document(uid).get().await()
        UserPublicFlags(
            nickName = doc.getString("nickname") ?: "",
            picturePublic = doc.getBoolean("picturePublic") ?: false,
            recordPublic  = doc.getBoolean("recordPublic")  ?: false
        )
    }

}