package com.nhj.fitlog.utils

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