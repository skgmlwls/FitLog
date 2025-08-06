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
            .sortedBy { it.name }  // ㄱ,ㄴ,ㄷ... 순으로 정렬
    }

    // 특정 이름의 운동 종류가 이미 존재하는지 체크
    suspend fun isNameDuplicated(uid: String, name: String): Boolean {
        val snap = db
            .collection("users")
            .document(uid)
            .collection("exerciseType")
            .whereEqualTo("name", name)
            .get()
            .await()

        return !snap.isEmpty
    }

    // 새로운 운동 종류 추가
    suspend fun addExerciseType(uid: String, vo: ExerciseTypeVO) {
        val ref = db
            .collection("users")
            .document(uid)
            .collection("exerciseType")
            .document()             // 자동 ID 생성
        val withId = vo.copy(id = ref.id)
        ref.set(withId).await()
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
            .delete()
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