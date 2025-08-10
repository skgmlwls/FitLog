package com.nhj.fitlog.data.repository

import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.WriteBatch
import com.nhj.fitlog.domain.model.RoutineSetModel
import com.nhj.fitlog.domain.vo.*
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.util.UUID

class RoutineRepository {
    private val db = FirebaseFirestore.getInstance()

    // 루틴 이름 중복 여부
    suspend fun isNameDuplicated(uid: String, name: String): Boolean {
        val snap = db.collection("users")
            .document(uid)
            .collection("routines")
            .whereEqualTo("name", name)
            .get()
            .await()
        return !snap.isEmpty
    }

    // 루틴 추가 (루틴 + exercises + sets) - 일괄 배치
    suspend fun addRoutine(
        uid: String,
        routine: RoutineVO,
        exercises: List<RoutineExerciseVO>,
        setsByItemId: Map<String, List<RoutineSetVO>>
    ): String {
        val routinesCol = db.collection("users").document(uid).collection("routines")
        val routineRef = routinesCol.document() // 자동 ID
        val routineWithId = routine.copy(
            routineId = routineRef.id,
            exerciseCount = exercises.size
        )

        val batch = db.batch()
        batch.set(routineRef, routineWithId)

        val exercisesCol = routineRef.collection("exercises")
        exercises.forEach { ex ->
            val exId = if (ex.itemId.isBlank()) UUID.randomUUID().toString() else ex.itemId
            val exRef = exercisesCol.document(exId)
            batch.set(exRef, ex.copy(itemId = exId))

            val sets = setsByItemId[exId].orEmpty()
            val setsCol = exRef.collection("sets")
            sets.forEach { s ->
                val setId = if (s.setId.isBlank()) UUID.randomUUID().toString() else s.setId
                val setRef = setsCol.document(setId)
                batch.set(setRef, s.copy(setId = setId))
            }
        }

        batch.commit().await()
        return routineRef.id
    }

    // 루틴 메타(이름/메모/개수 등) 업데이트
    suspend fun updateRoutineMeta(uid: String, vo: RoutineVO) {
        db.collection("users").document(uid)
            .collection("routines").document(vo.routineId)
            .update(
                mapOf(
                    "name" to vo.name,
                    "memo" to vo.memo,
                    "exerciseCount" to vo.exerciseCount
                )
            ).await()
    }

    // 루틴 목록 조회
    suspend fun getRoutines(uid: String): List<RoutineVO> {
        val snap = db.collection("users").document(uid)
            .collection("routines")
            .orderBy("createdAt")
            .get()
            .await()
        return snap.documents.mapNotNull { it.toObject(RoutineVO::class.java) }
    }

    // 루틴 1개 + exercise/sets 간단 조회 (필요 시)
    suspend fun getRoutine(uid: String, routineId: String): RoutineVO? {
        val doc = db.collection("users").document(uid)
            .collection("routines").document(routineId)
            .get()
            .await()
        return doc.toObject(RoutineVO::class.java)
    }

    // 루틴 삭제 (exercises/sets 포함)
    suspend fun deleteRoutine(uid: String, routineId: String) {
        val routineRef = db.collection("users").document(uid)
            .collection("routines").document(routineId)

        val exSnap = routineRef.collection("exercises").get().await()
        val batch = db.batch()

        // 하위 sets 먼저 삭제
        for (exDoc in exSnap.documents) {
            val setsSnap = exDoc.reference.collection("sets").get().await()
            setsSnap.documents.forEach { batch.delete(it.reference) }
            batch.delete(exDoc.reference)
        }
        batch.delete(routineRef)
        batch.commit().await()
    }


    /** 루틴 목록 실시간 Flow */
    fun routinesFlow(uid: String): Flow<List<RoutineVO>> = callbackFlow {
        val listener = db.collection("users").document(uid)
            .collection("routines")
            .orderBy("createdAt")
            .addSnapshotListener { snap, err ->
                if (err != null) {
                    // 에러 시 빈 리스트라도 흘려보냄(필요시 close(err))
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                val list = snap?.documents
                    ?.mapNotNull { it.toObject(RoutineVO::class.java) }
                    .orEmpty()
                trySend(list)
            }
        awaitClose { listener.remove() }
    }

    /** 루틴 내 운동 목록 가져오기 (order 순) */
    suspend fun getRoutineExercises(uid: String, routineId: String): List<RoutineExerciseVO> {
        val snap = db.collection("users").document(uid)
            .collection("routines").document(routineId)
            .collection("exercises")
            .orderBy("order")
            .get()
            .await()
        return snap.documents.mapNotNull { it.toObject(RoutineExerciseVO::class.java) }
    }

    /** 특정 운동의 세트 목록 가져오기 (setNumber 순) */
    suspend fun getRoutineSets(uid: String, routineId: String, itemId: String): List<RoutineSetVO> {
        val snap = db.collection("users").document(uid)
            .collection("routines").document(routineId)
            .collection("exercises").document(itemId)
            .collection("sets")
            .orderBy("setNumber")
            .get()
            .await()
        return snap.documents.mapNotNull { it.toObject(RoutineSetVO::class.java) }
    }

    /** 최신 운동 메모를 exerciseType에서 가져오기 (10개씩 whereIn) */
    suspend fun getLatestExerciseMemos(uid: String, typeIds: List<String>): Map<String, String> {
        if (typeIds.isEmpty()) return emptyMap()
        val result = mutableMapOf<String, String>()
        val distinct = typeIds.distinct()

        // 문서 ID와 "id" 필드가 동일하게 저장된 구조라면 FieldPath.documentId() 사용이 직관적
        for (chunk in distinct.chunked(10)) {
            val snap = db.collection("users").document(uid)
                .collection("exerciseType")
                .whereIn(FieldPath.documentId(), chunk)
                .get()
                .await()

            snap.documents.forEach { doc ->
                val id = doc.id
                val memo = doc.getString("memo") ?: ""
                result[id] = memo
            }
        }
        return result
    }

    // 이름 중복 (자기 자신 제외)
    suspend fun isNameDuplicatedExceptId(uid: String, name: String, routineId: String): Boolean {
        val snap = db.collection("users").document(uid)
            .collection("routines")
            .whereEqualTo("name", name)
            .get()
            .await()
        return snap.documents.any { it.id != routineId }
    }

    // 루틴 치환 저장: 메타 업데이트 + exercises/sets 전체 갈아끼움(배치)
    suspend fun replaceRoutine(
        uid: String,
        routine: RoutineVO,
        exercises: List<RoutineExerciseVO>,
        setsByItemId: Map<String, List<RoutineSetVO>>
    ) {
        val routineRef = db.collection("users").document(uid)
            .collection("routines").document(routine.routineId)

        // 1) 기존 exercises/sets 삭제
        val exSnap = routineRef.collection("exercises").get().await()
        val deleteBatch = db.batch()
        for (exDoc in exSnap.documents) {
            val setsSnap = exDoc.reference.collection("sets").get().await()
            setsSnap.documents.forEach { deleteBatch.delete(it.reference) }
            deleteBatch.delete(exDoc.reference)
        }
        deleteBatch.commit().await()

        // 2) 메타 업데이트
        val meta = routine.copy(exerciseCount = exercises.size)
        routineRef.update(
            mapOf(
                "name" to meta.name,
                "memo" to meta.memo,
                "exerciseCount" to meta.exerciseCount
            )
        ).await()

        // 3) 새 exercises/sets 생성
        val createBatch = db.batch()
        val exCol = routineRef.collection("exercises")
        exercises.forEach { ex ->
            val exId = if (ex.itemId.isBlank()) UUID.randomUUID().toString() else ex.itemId
            val exRef = exCol.document(exId)
            createBatch.set(exRef, ex.copy(itemId = exId))

            val sets = setsByItemId[exId].orEmpty()
            val setsCol = exRef.collection("sets")
            sets.forEach { s ->
                val setId = if (s.setId.isBlank()) UUID.randomUUID().toString() else s.setId
                createBatch.set(setsCol.document(setId), s.copy(setId = setId))
            }
        }
        createBatch.commit().await()
    }

}
