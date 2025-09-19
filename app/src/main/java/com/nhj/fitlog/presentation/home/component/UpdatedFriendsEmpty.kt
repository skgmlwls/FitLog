package com.nhj.fitlog.presentation.home.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PersonAddAlt1
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.nhj.fitlog.component.FitLogText

@Composable
fun UpdatedFriendsEmpty(
    onRefresh: () -> Unit,
    onGoFriends: () -> Unit
) {
    Column(Modifier.fillMaxWidth()) {
        FitLogText(text = "오늘 업데이트", color = Color(0xFFEAF2FF), fontSize = 16)
        Spacer(Modifier.height(12.dp))

        Card(
            colors = CardDefaults.cardColors(containerColor = Color.Transparent),
            shape = RoundedCornerShape(18.dp),
            modifier = Modifier.fillMaxWidth()
                .background(Color(0xFF2C2C2C), RoundedCornerShape(18.dp))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                FitLogText(
                    text = "오늘 업데이트한 친구가 없어요",
                    color = Color(0xFF9FB2D8),
                    fontSize = 14
                )
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    FilledTonalButton(
                        onClick = onRefresh,
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = Color(0xFF1E2A3F),
                            contentColor = Color(0xFFEAF2FF)
                        )
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = null)
                        Spacer(Modifier.width(6.dp))
                        Text("새로고침")
                    }
                    Button(
                        onClick = onGoFriends,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF47A6FF),
                            contentColor = Color.White
                        )
                    ) {
                        Icon(Icons.Default.PersonAddAlt1, contentDescription = null)
                        Spacer(Modifier.width(6.dp))
                        Text("친구 찾기")
                    }
                }
            }
        }
    }
}
