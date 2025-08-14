package com.nhj.fitlog.utils

import androidx.compose.runtime.mutableStateListOf
import com.nhj.fitlog.domain.model.RoutineExerciseModel
import com.nhj.fitlog.domain.model.RoutineSetModel

enum class JoinMethod(val methodName: String) {
    PHONE("Phone"),
    GOOGLE("Google"),
    KAKAO("Kakao")
}

enum class ExerciseCategories(val num: Int, val str: String) {
    TOTAL(0, "전체"),
    CHEST(1, "가슴"),
    BACK(2, "등"),
    SHOULDER(3, "어깨"),
    LEG(4, "하체"),
    ARM(5, "팔"),
    ABDOMEN(6, "복부"),
    ETC(7, "기타")
}

// 운동 강도
enum class RecordIntensity { HARD, NORMAL, EASY }

/** UI에서 다루기 편하도록 '운동 + 세트들'을 묶은 모델 */
data class RoutineExerciseWithSets(
    val exercise: RoutineExerciseModel,
    val sets: MutableList<RoutineSetModel> = mutableStateListOf()
)
