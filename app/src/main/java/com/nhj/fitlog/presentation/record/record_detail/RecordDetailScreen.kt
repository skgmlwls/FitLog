package com.nhj.fitlog.presentation.record.record_detail

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.outlined.AccessTime
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.nhj.fitlog.component.FitLogAlertDialog
import com.nhj.fitlog.component.FitLogOutlinedTextField
import com.nhj.fitlog.component.FitLogText
import com.nhj.fitlog.component.FitLogTopBar
import com.nhj.fitlog.component.LottieLoadingOverlay
import com.nhj.fitlog.presentation.record.record_detail.component.ReadonlyExerciseCard
import com.nhj.fitlog.presentation.record.record_exercise.RecordBg
import com.nhj.fitlog.presentation.record.record_exercise.RecordCardBg
import com.nhj.fitlog.utils.MainScreenName
import com.nhj.fitlog.utils.RoutineExerciseWithSets
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Composable
fun RecordDetailScreen(
    recordId: String,
    uid: String,
    previousScreen: String,
    viewModel: RecordDetailViewModel = hiltViewModel()
) {
    val focus = LocalFocusManager.current
    LaunchedEffect(recordId) {
        viewModel.uid.value = uid
        viewModel.previousScreen.value = previousScreen
        viewModel.load(recordId)
        Log.d("RecordDetailScreen", "recordId: ${viewModel.previousScreen.value}")
    }

    val rec = viewModel.record.value
    val items = viewModel.items.value

    // 탑바 메뉴 상태
    // var showMenu by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = RecordBg,
        topBar = {
            FitLogTopBar(
                title = "기록 상세",
                onBackClick = { viewModel.onBack() },
                hasActionIcon = previousScreen == MainScreenName.MAIN_SCREEN_HOME.name,
                actionIcon = Icons.Default.MoreVert,         // ← 여기 변경
                onActionClick = { viewModel.showTopBarMenu.value = true }          // ← 메뉴 오픈
            )
        }
    ) { inner ->
        Box(
            Modifier
                .padding(inner)
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectTapGestures { focus.clearFocus() }
                }
        ) {

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(RecordBg)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp)
            ) {
                Spacer(Modifier.height(12.dp))

                // 날짜/시간 (RecordExerciseScreen 스타일의 AssistChip, 읽기 전용)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    AssistChip(
                        onClick = { /* no-op */ },
                        enabled = false,
                        label = { FitLogText(viewModel.formatDateDisplay(rec?.date.orEmpty()), color = Color.White, fontSize = 18) },
                        colors = AssistChipDefaults.assistChipColors(
                            containerColor = Color(0xFF2C2C2C),
                            labelColor = Color.White,
                            disabledContainerColor = Color(0xFF2C2C2C),
                            disabledLabelColor = Color.White
                        )
                    )
                    Spacer(Modifier.width(8.dp))
                    AssistChip(
                        onClick = { /* no-op */ },
                        enabled = false,
                        label = { FitLogText(rec?.let { viewModel.formatTime12hKST(it.recordedAt) } ?: "--:--", color = Color.White, fontSize = 18) },
                        colors = AssistChipDefaults.assistChipColors(
                            containerColor = Color(0xFF2C2C2C),
                            labelColor = Color.White,
                            disabledContainerColor = Color(0xFF2C2C2C),
                            disabledLabelColor = Color.White
                        )
                    )

                    Spacer(Modifier.weight(1f))
                }

                Spacer(Modifier.height(16.dp))

                // 사진 (RecordExerciseScreen과 동일한 카드 썸네일 룩, 클릭/삭제 없음)
                if (previousScreen == MainScreenName.MAIN_SCREEN_HOME.name) {
                    if (!rec?.imageUrlList.isNullOrEmpty()) {
                        FitLogText(text = "사진", fontSize = 20, color = Color.White)
                        Spacer(Modifier.height(16.dp))

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            rec!!.imageUrlList.forEach { url ->
                                Card(
                                    modifier = Modifier.size(84.dp),
                                    colors = CardDefaults.cardColors(containerColor = RecordCardBg),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    AsyncImage(
                                        model = url,
                                        contentDescription = null,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                }
                            }
                        }

                        Spacer(Modifier.height(20.dp))
                    }
                } else {
                    if (viewModel.picturePublic.value) {
                        if (!rec?.imageUrlList.isNullOrEmpty()) {
                            FitLogText(text = "사진", fontSize = 20, color = Color.White)
                            Spacer(Modifier.height(16.dp))

                            Row(
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                rec!!.imageUrlList.forEach { url ->
                                    Card(
                                        modifier = Modifier.size(84.dp),
                                        colors = CardDefaults.cardColors(containerColor = RecordCardBg),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        AsyncImage(
                                            model = url,
                                            contentDescription = null,
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier.fillMaxSize()
                                        )
                                    }
                                }
                            }

                            Spacer(Modifier.height(20.dp))
                        }
                    }
                }

                Spacer(Modifier.height(10.dp))
                // 강도
                if (previousScreen == MainScreenName.MAIN_SCREEN_HOME.name) {
                    FitLogText(text = "강도", fontSize = 20, color = Color.White)
                    Spacer(Modifier.height(8.dp))
                    AssistChip(
                        onClick = { /* no-op */ },
                        enabled = false,
                        label = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    Modifier
                                        .size(8.dp)
                                        .background(
                                            rec?.let { intensityColor(it.intensity) } ?: Color.Gray,
                                            RoundedCornerShape(50)
                                        )
                                )
                                Spacer(Modifier.width(6.dp))
                                Text(rec?.intensity?.name.orEmpty(), color = Color.White)
                            }
                        },
                        colors = AssistChipDefaults.assistChipColors(
                            containerColor = Color(0xFF2C2C2A),
                            labelColor = Color.White,
                            disabledContainerColor = Color(0xFF2C2C2A),
                            disabledLabelColor = Color.White
                        )
                    )
                    Spacer(Modifier.height(10.dp))
                    // 메모 (텍스트필드 룩 그대로, 읽기전용/비활성)
                    FitLogText(text = "오늘 운동 메모", fontSize = 20, color = Color.White)
                    Spacer(Modifier.height(8.dp))
                    FitLogOutlinedTextField(
                        value = rec?.memo.orEmpty(),
                        onValueChange = {  },
                        label = "오늘 운동 메모",
                        singleLine = false,
                        enabled = false
                    )

                    Spacer(Modifier.height(25.dp))
                    HorizontalDivider(thickness = 0.5.dp, color = Color(0xFF404040))
                    Spacer(Modifier.height(25.dp))

                    // 운동/세트 목록 (ExerciseCard 느낌의 카드, 읽기 전용)
                    if (items.isNotEmpty()) {
                        FitLogText("운동 ${items.size}개", 20, color = Color.White, fontWeight = FontWeight.SemiBold)
                        Spacer(Modifier.height(15.dp))
                        items.forEachIndexed { index, ui ->
                            ReadonlyExerciseCard(
                                index = index + 1,
                                ui = ui
                            )
                            Spacer(Modifier.height(12.dp))
                        }
                        Spacer(Modifier.height(24.dp))
                    }

                    Spacer(Modifier.height(32.dp))
                } else {
                    if (viewModel.recordPublic.value) {
                        FitLogText(text = "강도", fontSize = 20, color = Color.White)
                        Spacer(Modifier.height(8.dp))
                        AssistChip(
                            onClick = { /* no-op */ },
                            enabled = false,
                            label = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        Modifier
                                            .size(8.dp)
                                            .background(
                                                rec?.let { intensityColor(it.intensity) }
                                                    ?: Color.Gray,
                                                RoundedCornerShape(50)
                                            )
                                    )
                                    Spacer(Modifier.width(6.dp))
                                    Text(rec?.intensity?.name.orEmpty(), color = Color.White)
                                }
                            },
                            colors = AssistChipDefaults.assistChipColors(
                                containerColor = Color(0xFF2C2C2A),
                                labelColor = Color.White,
                                disabledContainerColor = Color(0xFF2C2C2A),
                                disabledLabelColor = Color.White
                            )
                        )
                        Spacer(Modifier.height(10.dp))
                        // 메모 (텍스트필드 룩 그대로, 읽기전용/비활성)
                        FitLogText(text = "오늘 운동 메모", fontSize = 20, color = Color.White)
                        Spacer(Modifier.height(8.dp))
                        FitLogOutlinedTextField(
                            value = rec?.memo.orEmpty(),
                            onValueChange = {  },
                            label = "오늘 운동 메모",
                            singleLine = false,
                            enabled = false
                        )

                        Spacer(Modifier.height(25.dp))
                        HorizontalDivider(thickness = 0.5.dp, color = Color(0xFF404040))
                        Spacer(Modifier.height(25.dp))

                        // 운동/세트 목록 (ExerciseCard 느낌의 카드, 읽기 전용)
                        if (items.isNotEmpty()) {
                            FitLogText("운동 ${items.size}개", 20, color = Color.White, fontWeight = FontWeight.SemiBold)
                            Spacer(Modifier.height(15.dp))
                            items.forEachIndexed { index, ui ->
                                ReadonlyExerciseCard(
                                    index = index + 1,
                                    ui = ui
                                )
                                Spacer(Modifier.height(12.dp))
                            }
                            Spacer(Modifier.height(24.dp))
                        }

                        Spacer(Modifier.height(32.dp))
                    }
                }

                // 삭제 확인 다이얼로그
                if (viewModel.showDeleteConfirm.value) {
                    FitLogAlertDialog(
                        title = "삭제",
                        message = "이 기록을 삭제하시겠습니까?",
                        onConfirm = {
                            viewModel.onDeleteClick()
                            viewModel.showDeleteConfirm.value = false
                        },
                        onDismiss = { viewModel.showDeleteConfirm.value = false },
                        showCancelButton = true
                    )
                }
            }

            // ▼ 우측 상단에 드롭다운 메뉴를 오버레이로 띄움
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 8.dp, end = 8.dp)
            ) {
                DropdownMenu(
                    modifier = Modifier.background(Color(0xFF3C3C3C)),
                    shape = RoundedCornerShape(12.dp),
                    expanded = viewModel.showTopBarMenu.value,
                    onDismissRequest = { viewModel.showTopBarMenu.value = false },
                ) {
                    DropdownMenuItem(
                        text = { FitLogText("수정", color = Color.White) },
                        leadingIcon = {
                            Icon(Icons.Default.Edit, contentDescription = null, tint = Color.White)
                        },
                        onClick = {
                            viewModel.showTopBarMenu.value = false
                            viewModel.onNavigateToEditScreen()
                        }
                    )
                    HorizontalDivider(color = Color(0xFF7F7F7F))
                    DropdownMenuItem(
                        text = { FitLogText("삭제", color = Color.White) },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = null,
                                tint = Color.White
                            )
                        },
                        onClick = {
                            viewModel.showTopBarMenu.value = false
                            viewModel.showDeleteConfirm.value = true
                        }
                    )
                }
            }

            LottieLoadingOverlay(
                isVisible = viewModel.loading.value,
                modifier = Modifier
                    .fillMaxSize()
                    .align(Alignment.Center)
            )
        }
    }
}

// ── helpers ─────────────────────────────────────────────────────
private fun intensityColor(intensity: com.nhj.fitlog.utils.RecordIntensity) = when (intensity) {
    com.nhj.fitlog.utils.RecordIntensity.HARD   -> Color(0xFFFF4D4D)
    com.nhj.fitlog.utils.RecordIntensity.NORMAL -> Color(0xFF3D7DFF)
    com.nhj.fitlog.utils.RecordIntensity.EASY   -> Color(0xFFF8FF32)
}

private fun formatTimeKST(millis: Long): String =
    Instant.ofEpochMilli(millis)
        .atZone(ZoneId.of("Asia/Seoul"))
        .toLocalTime()
        .format(DateTimeFormatter.ofPattern("HH:mm"))

private fun trimZero(v: Double): String =
    if (kotlin.math.abs(v - v.toLong()) < 1e-9) v.toLong().toString() else v.toString()
