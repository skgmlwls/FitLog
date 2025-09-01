package com.nhj.fitlog.presentation.record.record_exercise.component

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.material3.LocalTextStyle
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.text.style.TextOverflow
import com.nhj.fitlog.utils.ImportRoutine

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoutineExposedDropdownMenuBox(
    label: String,
    expanded: Boolean,
    onExpandedChange: () -> Unit,
    routines: List<ImportRoutine>,
    onSelect: (routineId: String, routineName: String) -> Unit,
    modifier: Modifier = Modifier,
    minWidth: Dp = 150.dp,
    fieldHeight: Dp = 36.dp,
    placeholder: String = "루틴 불러오기",
) {
    val interaction = remember { MutableInteractionSource() }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { onExpandedChange() },
        modifier = modifier
    ) {
        // 앵커: 빈 세로 패딩 제거를 위해 BasicTextField + DecorationBox 사용
        BasicTextField(
            value = label,
            onValueChange = {},
            readOnly = true,
            singleLine = true,
            textStyle = LocalTextStyle.current.copy(color = Color.White),
            modifier = Modifier
                .menuAnchor()
                .defaultMinSize(minHeight = 1.dp)
                .height(fieldHeight)
                .widthIn(min = minWidth),
            decorationBox = { innerTextField ->
                TextFieldDefaults.DecorationBox(
                    value = label,
                    innerTextField = innerTextField,
                    enabled = true,
                    singleLine = true,
                    visualTransformation = VisualTransformation.None,
                    interactionSource = interaction,
                    // 내부 패딩 최소화
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
                    placeholder = {
                        Text(
                            text = placeholder,
                            color = Color(0xFFBDBDBD),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    },
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                    },
                    shape = RoundedCornerShape(12.dp),
                    colors = TextFieldDefaults.colors(
                        cursorColor = Color.White,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        disabledIndicatorColor = Color.Transparent,
                        focusedContainerColor = Color(0xFF2A2A2A),
                        unfocusedContainerColor = Color(0xFF2A2A2A),
                        disabledContainerColor = Color(0xFF2A2A2A),
                        focusedTrailingIconColor = Color.White,
                        unfocusedTrailingIconColor = Color.White,
                        disabledTrailingIconColor = Color.White,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        disabledTextColor = Color.White
                    )
                )
            }
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { onExpandedChange() },
            modifier = Modifier.exposedDropdownSize(),
            containerColor = Color(0xFF2F2F2F),
            tonalElevation = 0.dp,
            shadowElevation = 0.dp
        ) {
            if (routines.isEmpty()) {
                DropdownMenuItem(
                    text = { Text("루틴 없음", color = Color(0xFFBDBDBD)) },
                    enabled = false,
                    onClick = {}
                )
            } else {
                routines.forEachIndexed { i, pack ->
                    DropdownMenuItem(
                        text = { Text(pack.name, color = Color.White) },
                        onClick = {
                            // 선택 후 콜백
                            onSelect(pack.routineId, pack.name)
                            // 닫기는 상위에서 expanded=false로 처리
                            onExpandedChange()
                        }
                    )
                    if (i != routines.lastIndex) {
                        HorizontalDivider(thickness = 0.5.dp, color = Color(0xFF404040))
                    }
                }
            }
        }
    }
}
