package com.nhj.fitlog.presentation.join.component


import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.input.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.nhj.fitlog.ui.theme.NanumSquareRound

@Composable
fun FitLogPhoneTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String = "휴대폰 번호",
    horizontalPadding: Dp = 24.dp,
    modifier: Modifier = Modifier
) {
    val visualTransformation = PhoneNumberVisualTransformation()

    TextField(
        value = value,
        onValueChange = {
            // 숫자만 필터링
            val digitsOnly = it.filter { char -> char.isDigit() }
            // 최대 11자리까지만 입력 허용
            val limited = digitsOnly.take(11)
            onValueChange(limited)
        },
        label = {
            Text(
                text = label,
                color = Color.White,
                fontFamily = NanumSquareRound
            )
        },
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = horizontalPadding),
        singleLine = true,
        visualTransformation = visualTransformation,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        colors = TextFieldDefaults.colors(
            focusedContainerColor = Color.Transparent,
            unfocusedContainerColor = Color.Transparent,
            disabledContainerColor = Color.Transparent,

            unfocusedIndicatorColor = Color.Gray,
            focusedIndicatorColor = Color.White,
            cursorColor = Color.White,
            focusedLabelColor = Color.White,
            unfocusedLabelColor = Color.LightGray,
            disabledIndicatorColor = Color.Transparent,
            disabledTextColor = Color.LightGray,
            disabledLabelColor = Color.Gray,
            focusedTextColor = Color.White,
            unfocusedTextColor = Color.White
        )
    )
}

// OffsetMapping을 적용하여 커서 위치까지 자연스럽게 보정
class PhoneNumberVisualTransformation : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val raw = text.text.take(11)
        val builder = StringBuilder()

        for (i in raw.indices) {
            builder.append(raw[i])

            if (i == 2 && raw.length > 3) {
                builder.append('-') // 010- (4자리 이상일 때만)
            }
            if (i == 6 && raw.length > 7) {
                builder.append('-') // 010-1234- (8자리 이상일 때만)
            }
        }

        val offsetMapping = object : OffsetMapping {
            override fun originalToTransformed(offset: Int): Int {
                return when {
                    offset <= 3 -> offset
                    offset <= 7 -> offset + 1
                    offset <= 11 -> offset + 2
                    else -> 13
                }
            }

            override fun transformedToOriginal(offset: Int): Int {
                return when {
                    offset <= 3 -> offset
                    offset <= 8 -> offset - 1
                    offset <= 13 -> offset - 2
                    else -> 11
                }
            }
        }

        return TransformedText(AnnotatedString(builder.toString()), offsetMapping)
    }
}
