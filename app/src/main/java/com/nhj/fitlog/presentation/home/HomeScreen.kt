package com.nhj.fitlog.presentation.home

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.nhj.fitlog.component.FitLogButton
import com.nhj.fitlog.component.FitLogTextField

@Composable
fun HomeScreen(
    viewModel: HomeViewModel = hiltViewModel()
) {
    val focusManager = LocalFocusManager.current

    Scaffold(
        containerColor = Color(0xFF121212)
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .pointerInput(Unit) {
                    detectTapGestures(onTap = { focusManager.clearFocus() })
                }
        ) {
            Text(
                text = "홈화면",
                color = Color.White,
                modifier = Modifier.align(Alignment.Center),
                fontSize = 30.sp
            )

        }
    }

}