package com.nhj.fitlog.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.nhj.fitlog.domain.model.ExerciseTypeModel
import com.nhj.fitlog.domain.vo.ExerciseTypeVO
import kotlinx.coroutines.tasks.await

class ExerciseRepository {
    private val db = FirebaseFirestore.getInstance()

    // UID 기준 전체 운동 종류 조회
    suspend fun getExerciseTypes(uid: String): List<ExerciseTypeVO> {
        val snapshot = db
            .collection("users")
            .document(uid)
            .collection("exerciseType")
            .orderBy("createdAt")
            .get()
            .await()

        return snapshot.documents
            .mapNotNull { it.toObject(ExerciseTypeModel::class.java)?.toVO() }
            .filter { !it.checkDelete }               // 삭제된 것 제외
            .sortedBy { it.name }  // ㄱ,ㄴ,ㄷ... 순으로 정렬
    }

    // 이름 중복 체크: 같은 이름 중에 checkDelete=false 인 것만 중복으로 간주
    suspend fun isNameDuplicated(uid: String, name: String): Boolean {
        val snap = db.collection("users")
            .document(uid)
            .collection("exerciseType")
            .whereEqualTo("name", name)
            .get()
            .await()

        return snap.documents
            .mapNotNull { it.toObject(ExerciseTypeVO::class.java) }
            .any { !it.checkDelete }                  // ⬅️ 살아있는 문서가 있으면 중복
    }

    // 새로운 운동 종류 추가
    suspend fun addExerciseType(uid: String, vo: ExerciseTypeVO) {
        val ref = db.collection("users")
            .document(uid)
            .collection("exerciseType")
            .document()

        val withDefaults = vo.copy(
            id = ref.id,
            checkDelete = false,                      // 삭제 기본값
            createdAt = if (vo.createdAt == 0L) System.currentTimeMillis() else vo.createdAt
        )
        ref.set(withDefaults).await()
    }

    // 운동 종류 업데이트 (name, category, memo만)
    suspend fun updateExerciseType(uid: String, model: ExerciseTypeVO) {
        db.collection("users")
            .document(uid)
            .collection("exerciseType")
            .document(model.id)
            .update(
                mapOf(
                    "name"     to model.name,
                    "category" to model.category,
                    "memo"     to model.memo
                )
            )
            .await()
    }

    // 운동 종류 삭제
    suspend fun deleteExerciseType(uid: String, id: String) {
        db.collection("users")
            .document(uid)
            .collection("exerciseType")
            .document(id)
            .update(
                mapOf(
                    "checkDelete" to true,            // 삭제 플래그
                    "deletedAt" to System.currentTimeMillis()
                )
            )
            .await()
    }

    // ID로 단일 운동 종류 조회
    suspend fun getExerciseType(uid: String, id: String): ExerciseTypeVO? {
        val doc = db
            .collection("users")
            .document(uid)
            .collection("exerciseType")
            .document(id)
            .get()
            .await()
        return doc.toObject(ExerciseTypeModel::class.java)?.toVO()
    }

}