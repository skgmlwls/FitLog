package com.nhj.fitlog.presentation.friends.list.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.nhj.fitlog.component.FitLogAlertDialog
import com.nhj.fitlog.component.FitLogText
import com.nhj.fitlog.data.service.FriendItem

private val OnBg = Color(0xFFE6E6E6)
private val CardBg = Color(0xFF1E1E1E)
private val Bg = Color(0xFF121212)
private val Subtle = Color(0xFFB5B5B5)
private val Primary = Color(0xFF3D7DFF)

@Composable
fun FriendRow(
    friendItem: FriendItem,
    onDelete: () -> Unit,
    onClick: () -> Unit = {}
) {
    var confirmOpen by remember { mutableStateOf(false) }

    Surface(
        color = CardBg,
        shape = RoundedCornerShape(14.dp),
        tonalElevation = 0.dp,
        modifier = Modifier
            .fillMaxWidth()
            .then(
                if (friendItem.hasTodayRecord)
                    Modifier.border(2.dp, Primary, RoundedCornerShape(14.dp))
                else Modifier
            ),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            ProfileAvatar(friendItem.profileImageUrl)
            Spacer(Modifier.width(12.dp))

            Column(Modifier.weight(1f)) {
                FitLogText(
                    text = if (friendItem.nickname.isBlank()) "(알 수 없음)" else friendItem.nickname,
                    color = OnBg,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(Modifier.height(2.dp))
                val scopes = listOfNotNull(
                    if (friendItem.picturePublic) "사진" else null,
                    if (friendItem.recordPublic) "기록" else null
                )
                val scopeText = if (scopes.isEmpty()) "비공개" else scopes.joinToString(", ")
                FitLogText(
                    text = "공개 범위 : $scopeText",
                    fontSize = 12,
                    color = Subtle,
                    fontWeight = FontWeight.Light
                )
            }

            Spacer(Modifier.width(8.dp))

            // ✅ 우측: 삭제 아이콘 + 상태 텍스트(세로)
            Column(horizontalAlignment = Alignment.End) {
                IconButton(onClick = { confirmOpen = true }) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "delete friend",
                        tint = Color(0xFFFFFFFF)
                    )
                }

                val statusText = if (friendItem.hasTodayRecord) {
                    "오늘 운동 업데이트됨"
                } else {
                    val d = friendItem.lastRecordDate
                    if (d.isNullOrBlank()) "기록 없음" else "마지막 기록: $d"
                }
                FitLogText(
                    text = statusText,
                    fontSize = 10,
                    color = if (friendItem.hasTodayRecord) Primary else Subtle,
                    modifier = Modifier.padding(top = 2.dp),
                    textAlign = TextAlign.Center
                )
            }
        }

        if (confirmOpen) {
            FitLogAlertDialog(
                title = "친구 삭제",
                message = "이 친구를 목록에서 삭제할까요?\n삭제 후 되돌릴 수 없습니다.",
                onConfirm = {
                    confirmOpen = false
                    onDelete()
                },
                onDismiss = { confirmOpen = false },
                showCancelButton = true
            )
        }
    }
}