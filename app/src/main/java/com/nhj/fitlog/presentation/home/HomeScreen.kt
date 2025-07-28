package com.nhj.fitlog.presentation.home

import android.util.Log
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircleOutline
import androidx.compose.material.icons.filled.Cached
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.nhj.fitlog.R
import com.nhj.fitlog.component.FitLogIconButton
import com.nhj.fitlog.presentation.home.component.FitLogHomeButton
import com.nhj.fitlog.presentation.home.component.FriendLogItem

@Composable
fun HomeScreen(
    viewModel: HomeViewModel = hiltViewModel()
) {
    val focusManager = LocalFocusManager.current

    LaunchedEffect(Unit) {
        Log.d("HomeScreen", viewModel.application.userUid)
        viewModel.firstRun()
    }

    Scaffold(
        containerColor = Color(0xFF121212)
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
                .pointerInput(Unit) {
                    detectTapGestures(onTap = { focusManager.clearFocus() })
                }
        ) {

            // Top bar: 앱 이름 + 설정 아이콘 (아이콘은 상단 고정, 텍스트는 아래로)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
            ) {
                // 아이콘 버튼: 오른쪽 상단 고정
                IconButton(
                    onClick = { },
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(top = 16.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "설정",
                        tint = Color.White
                    )
                }

                // 앱 이름 텍스트: 가운데 정렬 + 아래 여백
                Text(
                    text = "FITLOG",
                    color = Color(0xFF47A6FF),
                    fontSize = 60.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(top = 40.dp)
                )
            }

            // Spacer(modifier = Modifier.height(8.dp))

            // 인사 텍스트
            Text(
                text = "홍길동님 안녕하세요~!",
                color = Color.White,
                fontSize = 16.sp
            )

            Spacer(modifier = Modifier.height(30.dp))

            // 2x2 버튼 그리드
            Column {
                Row(modifier = Modifier.fillMaxWidth()) {
                    FitLogHomeButton(
                        iconVector = Icons.Default.Edit,
                        label = "운동 기록",
                        modifier = Modifier.weight(1f),
                        onClick = {  }
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    FitLogHomeButton(
                        iconVector = Icons.Default.Group,
                        label = "친구 목록",
                        modifier = Modifier.weight(1f),
                        onClick = {  }
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(modifier = Modifier.fillMaxWidth()) {
                    FitLogHomeButton(
                        iconVector = Icons.Default.Cached,
                        label = "루틴 목록",
                        modifier = Modifier.weight(1f),
                        onClick = {  }
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    FitLogHomeButton(
                        iconPainter = painterResource(id = R.drawable.ic_dumbbell),
                        label = "운동 종류",
                        modifier = Modifier.weight(1f),
                        onClick = { viewModel.onNavigateToExerciseTypeScreen() }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 오늘 운동 기록하기 버튼
            FitLogIconButton(
                text = "오늘 운동 기록하기",
                icon = Icons.Default.AddCircleOutline,
                onClick = {  }
            )

            // 오늘 운동 기록하기 버튼
            FitLogIconButton(
                text = "로그아웃",
                icon = Icons.Default.Logout,
                onClick = { viewModel.onLogout() }
            )

            Spacer(modifier = Modifier.height(24.dp))

            // 친구의 기록 리스트 제목
            Text(
                text = "친구의 기록",
                color = Color.White,
                fontSize = 16.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            // 친구 기록 리스트
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                items(viewModel.friendLogs) { log ->
                    FriendLogItem(log = log)
                }
            }

        }
    }
}
