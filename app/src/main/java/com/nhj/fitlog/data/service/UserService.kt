package com.nhj.fitlog.data.service

import com.nhj.fitlog.data.repository.UserRepository
import com.nhj.fitlog.domain.model.UserModel

class UserService(
    private val userRepository: UserRepository
) {
    // 아이디 사용 가능 여부
    suspend fun isUserIdAvailable(id: String): Boolean =
        !userRepository.isUserIdAvailable(id)

    // phone 사용 가능 여부
    suspend fun isPhoneAvailable(phone: String) =
        !userRepository.isPhoneAvailable(phone)

    // 닉네임 사용 가능 여부
    suspend fun isNicknameAvailable(nickname: String): Boolean {
        return !userRepository.isNicknameDuplicated(nickname)
    }

    // 사용자 존재 여부 확인 (uid 기준)
    suspend fun isUserExists(uid: String): Boolean =
        userRepository.isUserExists(uid)

    // 사용자 정보 저장
    suspend fun addUser(userModel: UserModel) {
        val userVO = userModel.toVO()
        userRepository.addUser(userVO)
    }

    // id/password 조회
    suspend fun login(id: String, password: String): UserModel? {
        val vo = userRepository.getUserByIdAndPassword(id, password)
        return vo?.toModel()
    }

    // UID 기반 사용자 정보 조회
    suspend fun getUserByUid(uid: String): UserModel? {
        val vo = userRepository.getUserByUid(uid)
        return vo?.toModel()
    }

    // 기록 공개 여부 업데이트
    suspend fun updateRecordVisibility(uid: String, isRecordPublic: Boolean) {
        userRepository.updateRecordVisibility(uid, isRecordPublic)
    }

    // 사진 공개 여부 업데이트
    suspend fun updatePictureVisibility(uid: String, isPicturePublic: Boolean) {
        userRepository.updatePictureVisibility(uid, isPicturePublic)
    }

}