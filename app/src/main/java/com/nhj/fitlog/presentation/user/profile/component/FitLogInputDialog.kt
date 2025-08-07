package com.nhj.fitlog.presentation.user.profile.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.nhj.fitlog.component.FitLogButton
import com.nhj.fitlog.component.FitLogOutlinedTextField

// component/FitLogInputDialog.kt
@Composable
fun FitLogInputDialog(
    title: String,
    label: String,
    text: String,
    errorMessage: String? = null,
    onTextChange: (String) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(dismissOnClickOutside = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(Color(0xFF1E1E1E))
                .padding(24.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    title,
                    fontSize = 20.sp,
                    color = Color.White
                )
                Spacer(Modifier.height(12.dp))
                FitLogOutlinedTextField(
                    value = text,
                    onValueChange = onTextChange,
                    label = label,
                    modifier = Modifier.fillMaxWidth()
                )
                // 오류 메시지 표시
                if (!errorMessage.isNullOrEmpty()) {
                    Text(
                        text = errorMessage,
                        color = Color.Red,
                        fontSize = 12.sp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp)
                    )
                }
                Spacer(Modifier.height(24.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(Modifier.weight(1f)) {
                        FitLogButton(
                            text = "취소",
                            onClick = onDismiss,
                            textColor = Color.White,
                            backgroundColor = Color(0xFF444444),
                            horizontalPadding = 0.dp
                        )
                    }
                    Box(Modifier.weight(1f)) {
                        FitLogButton(
                            text = "확인",
                            onClick = onConfirm,
                            textColor = Color.White,
                            backgroundColor = Color(0xFF47A6FF),
                            horizontalPadding = 0.dp
                        )
                    }
                }
            }
        }
    }
}
