package com.nhj.fitlog.presentation.home.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nhj.fitlog.domain.vo.ExerciseRecordVO
import java.text.SimpleDateFormat
import java.util.*

// 친구의 운동 기록 로그 항목을 UI로 표시하는 컴포저블
@Composable
fun FriendLogItem(
    log: ExerciseRecordVO
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .background(Color.DarkGray, shape = RoundedCornerShape(8.dp))
            .padding(12.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth()) {
            // 운동 정보 요약 텍스트
            Text(
                text = "${log.date} • ${log.exerciseTypeId} • ${log.sets}세트",
                color = Color.White,
                modifier = Modifier.weight(1f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            // 시간 표시
            Text(
                text = formatTime(log.createdAt),
                color = Color.Gray,
                fontSize = 12.sp
            )
        }
    }
}

// 타임스탬프(Long)를 HH:mm 형식의 문자열로 변환하는 함수
fun formatTime(timestamp: Long): String {
    val date = Date(timestamp)
    val format = SimpleDateFormat("HH:mm", Locale.getDefault())
    return format.format(date)
}
