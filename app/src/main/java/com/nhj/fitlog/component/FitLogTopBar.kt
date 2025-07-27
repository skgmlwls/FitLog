package com.nhj.fitlog.component

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nhj.fitlog.ui.theme.NanumSquareRound
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FitLogTopBar(
    title: String,
    onBackClick: () -> Unit,
    fontSize: TextUnit = 24.sp,
    hasActionIcon: Boolean = false, // ✅ 오른쪽 아이콘 사용 여부
    actionIcon: ImageVector = Icons.Default.Add, // 기본값 (원하면 바꿔도 됨)
    onActionClick: () -> Unit = {} // 오른쪽 아이콘 클릭 콜백
) {
    var isClickable by remember { mutableStateOf(true) }
    var trigger by remember { mutableStateOf(false) }

    if (trigger) {
        LaunchedEffect(trigger) {
            delay(500)
            isClickable = true
            trigger = false
        }
    }

    TopAppBar(
        title = {
            Text(
                text = title,
                color = Color.White,
                fontSize = fontSize,
                fontFamily = NanumSquareRound
            )
        },
        navigationIcon = {
            IconButton(
                onClick = {
                    if (isClickable) {
                        isClickable = false
                        trigger = true
                        onBackClick()
                    }
                }
            ) {
                Icon(
                    imageVector = Icons.Filled.ArrowBackIosNew,
                    contentDescription = "뒤로가기",
                    tint = Color.White
                )
            }
        },
        actions = {
            if (hasActionIcon) {
                IconButton(onClick = onActionClick) {
                    Icon(
                        imageVector = actionIcon,
                        contentDescription = "액션 아이콘",
                        tint = Color.White
                    )
                }
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = Color.Transparent,
        )
    )
}
