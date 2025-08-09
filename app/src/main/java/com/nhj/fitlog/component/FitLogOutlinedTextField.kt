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
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = {
            Text(text = label, color = Color.LightGray)
        },
        modifier = modifier,
        singleLine = singleLine,
        enabled = enabled, // 포커스 및 입력 허용 여부
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = Color(0xFF47A6FF),
            unfocusedBorderColor = Color.White,
            focusedLabelColor = Color(0xFF47A6FF),
            unfocusedLabelColor = Color.White,
            cursorColor = Color.White,
            focusedTextColor = Color.White,
            unfocusedTextColor = Color.White,
            disabledBorderColor = Color.White,            // 비활성화 시 테두리 색상
            disabledLabelColor = Color.White,             // 비활성화 시 라벨 색상
            disabledTextColor = Color.White,              // 비활성화 시 텍스트 색상
        )
    )
}