package com.nhj.fitlog.component

import android.graphics.drawable.Icon
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nhj.fitlog.ui.theme.NanumSquareRound

@Composable
fun FitLogTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    horizontalPadding: Dp = 24.dp,
    isPassword: Boolean = false,
    enabled: Boolean = true,
    isError: Boolean = false,           // ✅ 에러 여부 추가
    errorText: String = ""              // ✅ 에러 메시지 텍스트
) {
    var passwordVisible by remember { mutableStateOf(false) }

    Column(modifier = modifier.padding(horizontal = horizontalPadding)) {
        TextField(
            value = value,
            onValueChange = onValueChange,
            enabled = enabled,
            label = {
                Text(
                    text = label,
                    color = Color.White,
                    fontFamily = NanumSquareRound
                )
            },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            visualTransformation = if (isPassword && !passwordVisible)
                PasswordVisualTransformation() else VisualTransformation.None,
            trailingIcon = {
                if (isPassword) {
                    val icon = if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(imageVector = icon, contentDescription = null, tint = Color.LightGray)
                    }
                }
            },
            isError = isError, // ✅ M3 스타일 에러 상태
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
                disabledContainerColor = Color.Transparent,
                errorContainerColor = Color.Transparent, // 오류 상태에서도 배경 유지
                errorTextColor = Color.White, // 명시적으로 흰색 고정
                unfocusedIndicatorColor = if (isError) Color.Red else Color.Gray,
                focusedIndicatorColor = if (isError) Color.Red else Color.White,
                cursorColor = Color.White,
                focusedLabelColor = if (isError) Color.Red else Color.White,
                unfocusedLabelColor = if (isError) Color.Red else Color.LightGray,
                disabledIndicatorColor = Color.Gray,         // disabled 시 회색으로
                disabledTextColor = Color.LightGray,    // disabled 텍스트 색
                disabledLabelColor = Color.Gray,         // disabled 라벨 색
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White
            ),
            keyboardOptions = if (isPassword) {
                KeyboardOptions(keyboardType = KeyboardType.Password)
            } else {
                KeyboardOptions.Default
            }
        )

        if (isError && errorText.isNotBlank()) {
            Text(
                text = errorText,
                color = Color.Red,
                fontSize = 12.sp,
                modifier = Modifier.padding(start = 4.dp, top = 4.dp)
            )
        }
    }
}
