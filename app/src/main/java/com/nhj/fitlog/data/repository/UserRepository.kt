package com.nhj.fitlog.data.repository

import android.net.Uri
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.nhj.fitlog.domain.vo.UserVO
import kotlinx.coroutines.tasks.await

class UserRepository {
    private val db = FirebaseFirestore.getInstance()

    // 아이디 중복 검사
    suspend fun isUserIdAvailable(id: String): Boolean {
        val snapshot = db.collection("users")
            .whereEqualTo("id", id)
            .get()
            .await()
        return !snapshot.isEmpty
    }

    // phone 중복 검사
    suspend fun isPhoneAvailable(phone: String): Boolean {
        val snap = db.collection("users")
            .whereEqualTo("phone", phone)
            .get()
            .await()
        return !snap.isEmpty
    }

    // 닉네임 중복 검사
    suspend fun isNicknameDuplicated(nickname: String): Boolean {
        val snap = db.collection("users")
            .whereEqualTo("nickname", nickname)
            .get()
            .await()
        return !snap.isEmpty
    }

    // UID 기반 사용자 존재 여부 체크
    suspend fun isUserExists(uid: String): Boolean {
        val doc = db.collection("users")
            .document(uid)
            .get()
            .await()
        return doc.exists()
    }

    // Firestore에 사용자 정보를 추가
    suspend fun addUser(userVO: UserVO) {
        val db = FirebaseFirestore.getInstance()
        db.collection("users")
            .document(userVO.uid) // uid를 문서 ID로 사용
            .set(userVO)          // userVO 객체 전체 저장
            .await()
    }

    // id/password 조회
    suspend fun getUserByIdAndPassword(id: String, password: String): UserVO? {
        val snap = db.collection("users")
            .whereEqualTo("id", id)
            .whereEqualTo("password", password)
            .get()
            .await()
        return snap.documents.firstOrNull()?.toObject(UserVO::class.java)
    }

    // UID 기반 사용자 정보 조회
    suspend fun getUserByUid(uid: String): UserVO? {
        val snap = db.collection("users")
            .document(uid)
            .get()
            .await()
        return snap.toObject(UserVO::class.java)
    }

    // 기록 공개 여부 업데이트
    suspend fun updateRecordVisibility(uid: String, isRecordPublic: Boolean) {
        db.collection("users")
            .document(uid)
            .update("recordPublic", isRecordPublic)
            .await()
    }

    // 사진 공개 여부 업데이트
    suspend fun updatePictureVisibility(uid: String, isPicturePublic: Boolean) {
        db.collection("users")
            .document(uid)
            .update("picturePublic", isPicturePublic)
            .await()
    }

    // 이미지 저장 후 URL 가져오기
    suspend fun uploadProfileImage(uid: String, imageUri: Uri) : String {
        val storageRef = FirebaseStorage
            .getInstance()
            .getReference("ProfileImage/$uid.jpg")

        // 1) 파일 업로드
        storageRef.putFile(imageUri).await()

        // 2) 다운로드 URL 취득
        val imageURL = storageRef.downloadUrl.await().toString()

        return imageURL
    }

    // 프로필 이미지 URL 업데이트
    suspend fun updateProfileImage(uid: String, imageUrl: String) {
        db.collection("users")
            .document(uid)
            .update("profileImageUrl", imageUrl)
            .await()
    }

    // 닉네임 업데이트
    suspend fun updateNickname(uid: String, nickname: String) {
        db.collection("users")
            .document(uid)
            .update("nickname", nickname)
            .await()
    }

}