package com.nhj.fitlog.presentation.setting

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.nhj.fitlog.component.FitLogButton
import com.nhj.fitlog.component.FitLogTopBar
import com.nhj.fitlog.component.FitLogText

@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = hiltViewModel()
) {
    Scaffold(
        topBar = {
            FitLogTopBar(
                title = "설정",
                onBackClick = { viewModel.onBack() }
            )
        },
        containerColor = Color(0xFF121212)
    ) { inner ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(inner)
                .padding(16.dp)
        ) {
            // 공개 범위 섹션
            FitLogText(
                text = "공개 범위",
                fontSize = 20,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Spacer(Modifier.height(15.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                FitLogText(text = "기록", fontSize = 16, color = Color.White)
                Spacer(Modifier.weight(1f))
                Switch(
                    checked = viewModel.recordPublic,
                    onCheckedChange = { viewModel.toggleRecordPublic() },
                    colors = SwitchDefaults.colors(checkedThumbColor = Color(0xFF47A6FF))
                )
            }
            Spacer(Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                FitLogText(text = "사진", fontSize = 16, color = Color.White)
                Spacer(Modifier.weight(1f))
                Switch(
                    checked = viewModel.photoPublic,
                    onCheckedChange = { viewModel.togglePhotoPublic() },
                    colors = SwitchDefaults.colors(checkedThumbColor = Color(0xFF47A6FF))
                )
            }

            Spacer(Modifier.height(24.dp))

            // 계정 섹션
            FitLogText(
                text = "계정",
                fontSize = 20,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Spacer(Modifier.height(15.dp))

            // 프로필 버튼
            FitLogButton(
                text = "프로필",
                onClick = { viewModel.onNavigateToProfile() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                horizontalPadding = 0.dp,
                backgroundColor = Color(0xFF3C3C3C)
            )
            Spacer(Modifier.height(8.dp))

            // 로그아웃 버튼
            FitLogButton(
                text = "로그아웃",
                onClick = { viewModel.onLogout() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                horizontalPadding = 0.dp,
                backgroundColor = Color(0xFF3C3C3C)
            )
        }
    }
}