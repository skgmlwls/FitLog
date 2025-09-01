package com.nhj.fitlog.component

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color

@Composable
fun FitLogOutlinedTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier.fillMaxWidth(),
    singleLine: Boolean = true,
    enabled: Boolean = true // 포커스 및 입력 가능 여부 제어
) {
    val isEmpty = value.isBlank()

    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(text = label) },
        modifier = modifier,
        singleLine = singleLine,
        enabled = enabled,
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = Color(0xFF47A6FF),
            unfocusedBorderColor = Color.White,
            cursorColor = Color.White,

            // ✅ 상태별 라벨 색
            focusedLabelColor = Color(0xFF47A6FF),
            unfocusedLabelColor = if (isEmpty) Color.Gray else Color.White,
            disabledLabelColor = Color.White,

            // 텍스트 색
            focusedTextColor = Color.White,
            unfocusedTextColor = Color.White,
            disabledTextColor = Color.White,

            // 비활성 테두리 등
            disabledBorderColor = Color.White,
        )
    )
}