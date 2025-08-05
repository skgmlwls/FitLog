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
}