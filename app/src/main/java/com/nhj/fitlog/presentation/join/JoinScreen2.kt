package com.nhj.fitlog.presentation.join

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.nhj.fitlog.component.FitLogTextField
import com.nhj.fitlog.component.FitLogButton
import com.nhj.fitlog.component.FitLogTopBar

@Composable
fun JoinScreen2(
    viewModel: JoinViewModel = hiltViewModel()
) {
    val focusManager = LocalFocusManager.current

    Scaffold(
        topBar = {
            FitLogTopBar(
                title = "비밀번호",
                onBackClick = { viewModel.onNavigateBack() }
            )
        },
        containerColor = Color(0xFF121212)
    ) { innerPadding ->

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(Color(0xFF121212))
                .pointerInput(Unit) {
                    detectTapGestures(onTap = { focusManager.clearFocus() })
                }
        ) {

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                FitLogTextField(
                    value = viewModel.joinPassword,
                    onValueChange = { viewModel.joinPassword = it },
                    label = "비밀번호",
                    isPassword = true
                )

                FitLogTextField(
                    value = viewModel.joinConfirmPassword,
                    onValueChange = { viewModel.joinConfirmPassword = it },
                    label = "비밀번호 확인",
                    isPassword = true
                )
            }

            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(bottom = 20.dp)
            ) {
                FitLogButton(
                    text = "다음",
                    onClick = { viewModel.onNavigateToJoinScreen3() },
                    horizontalPadding = 20.dp,
                )
            }
        }
    }
}