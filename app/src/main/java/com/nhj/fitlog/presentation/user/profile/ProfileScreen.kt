package com.nhj.fitlog.presentation.user.profile

import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import coil.compose.SubcomposeAsyncImage
import coil.compose.SubcomposeAsyncImageContent
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
import com.nhj.fitlog.R
import com.nhj.fitlog.component.FitLogButton
import com.nhj.fitlog.component.FitLogOutlinedTextField
import com.nhj.fitlog.component.FitLogText
import com.nhj.fitlog.component.FitLogTopBar
import com.nhj.fitlog.component.LottieLoadingOverlay
import com.nhj.fitlog.presentation.user.profile.component.FitLogInputDialog

@Composable
fun ProfileScreen(
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val focusManager = LocalFocusManager.current

    // collect the image URL state
    val imageUrl by viewModel.profileImageUrl.collectAsState()
    // 갤러리 호출 런처
    val pickImageLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            viewModel.isLoading = true
            viewModel.uploadProfileImage(it)
        }
    }

    // 프로필 이미지 클릭 가능 여부 상태
    var imageClickable by remember { mutableStateOf(false) }

    // Lottie shimmer animation 준비
    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.shimmer))
    val progress by animateLottieCompositionAsState(
        composition,
        iterations = LottieConstants.IterateForever
    )

    // 이미지를 볼 수 있는 다이얼로그
    var showImageDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            FitLogTopBar(
                title = "운동 상세",
                onBackClick = {
                    viewModel.onBack()
                },
            )
        },
        containerColor = Color(0xFF121212)
    ) { inner ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(inner)
                .padding(16.dp)
                .pointerInput(Unit) {
                    detectTapGestures(onTap = { focusManager.clearFocus() })
                },
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {

            FitLogText(
                text = "프로필 사진",
                fontSize = 25,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Box(
                modifier = Modifier
                    .size(180.dp)
                    .clip(CircleShape)
                    .background(Color.DarkGray)
                    .align(Alignment.CenterHorizontally)
                    .clickable(enabled = imageClickable) {
                        // 해당 사진을 볼 수 있는 기능
                        showImageDialog = true
                    },
                contentAlignment = Alignment.Center
            ) {
                SubcomposeAsyncImage(
                    model = imageUrl,
                    contentDescription = "프로필 사진",
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop,
                    loading = {
                        // 로딩 중에는 shimmer
                        LottieAnimation(
                            composition = composition,
                            progress = { progress },
                            modifier = Modifier.fillMaxSize()
                        )
                    },
                    success = { state ->
                        imageClickable = true
                        // 이미지가 완전히 로드된 순간 로그 찍기
                        Log.d("ProfileScreen", "Image loaded successfully: $imageUrl")
                        SubcomposeAsyncImageContent()
                    },
                    error = {
                        // 에러 시 기본 아이콘
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "기본 프로필 아이콘",
                            tint = Color.LightGray,
                            modifier = Modifier.size(64.dp)
                        )
                    }
                )
            }

            FitLogButton(
                text = "프로필 사진 변경",
                onClick = { pickImageLauncher.launch("image/*") },
                horizontalPadding = 0.dp,
                backgroundColor = Color(0xFF3C3C3C),
            )

            FitLogText(
                text = "닉네임",
                fontSize = 25,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            FitLogOutlinedTextField(
                value = viewModel.nickName,
                onValueChange = { viewModel.nickName = it },
                label = "닉네임",
                enabled = false
            )
            // 닉네임 변경 버튼
            FitLogButton(
                text = "닉네임 변경",
                onClick = {
                    viewModel.tempNickname = viewModel.nickName
                    viewModel.nicknameErrorMessage = null
                    viewModel.showNicknameDialog = true
                },
                horizontalPadding = 0.dp,
                backgroundColor = Color(0xFF3C3C3C),
            )

        }
        // 로딩 오버레이
        LottieLoadingOverlay(
            isVisible = viewModel.isLoading,
            modifier = Modifier.fillMaxSize()
        )

        // 닉네임 변경 다이얼로그
        if (viewModel.showNicknameDialog) {
            FitLogInputDialog(
                title = "닉네임 변경",
                label = "새 닉네임",
                text = viewModel.tempNickname,
                errorMessage = viewModel.nicknameErrorMessage,
                onTextChange = { viewModel.tempNickname = it },
                onConfirm = {
                    viewModel.onNicknameChange()
                },
                onDismiss = {
                    viewModel.showNicknameDialog = false
                }
            )
        }

        // 전체화면 이미지 다이얼로그
        if (showImageDialog && !imageUrl.isNullOrBlank()) {
            Dialog(
                onDismissRequest = { showImageDialog = false },
                properties = DialogProperties(usePlatformDefaultWidth = false)  // 전체 폭 사용
            ) {
                Box(
                    Modifier
                        .fillMaxSize()
                        .background(Color.Black)
                ) {
                    // 닫기 버튼 (왼쪽 상단)
                    IconButton(
                        onClick = { showImageDialog = false },
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(16.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "닫기",
                            tint = Color.White
                        )
                    }

                    // 전체화면 이미지
                    AsyncImage(
                        model = imageUrl,
                        contentDescription = "프로필 전체화면",
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(1f)
                            .align(Alignment.Center),
                        contentScale = ContentScale.Fit
                    )
                }
            }
        }
    }
}