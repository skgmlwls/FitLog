package com.nhj.fitlog.presentation.record.record_edit

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.nhj.fitlog.component.*
import com.nhj.fitlog.presentation.record.record_exercise.RecordBg
import com.nhj.fitlog.presentation.record.record_exercise.RecordCardBg
import com.nhj.fitlog.presentation.record.record_exercise.component.FitLogTimePickerDialog
import com.nhj.fitlog.presentation.record.record_exercise.component.RoutineExposedDropdownMenuBox
import com.nhj.fitlog.presentation.routine.add.component.ExerciseCard
import com.nhj.fitlog.presentation.routine.add.component.RoutineReorderBottomSheet
import com.nhj.fitlog.utils.RoutineExerciseWithSets
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecordEditScreen(
    recordId: String,
    viewModel: RecordEditViewModel = hiltViewModel()
) {
    LaunchedEffect(recordId) {
        viewModel.init(recordId)
        viewModel.consumeSelectedExerciseIfExists()
    }
    val focus = LocalFocusManager.current

    // 이미지 선택 런처
    val pickImageLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? -> viewModel.addImage(uri) }

    // Date/Time dialog
    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }

    // 이미지 프리뷰
    var previewImageUri by remember { mutableStateOf<Uri?>(null) }
    var showImageDialog by remember { mutableStateOf(false) }

    // Reorder bottom sheet
    var showReorderSheet by rememberSaveable { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    Box(Modifier.fillMaxSize()) {
        Scaffold(
            containerColor = RecordBg,
            topBar = {
                FitLogTopBar(
                    title = "운동 기록 수정",
                    hasActionIcon = true,
                    actionIcon = Icons.Default.Check,
                    onBackClick = { viewModel.showEditCancelConfirm.value = true },
                    onActionClick = { viewModel.showEditConfirm.value = true }
                )
            }
        ) { padding ->
            Box(Modifier.padding(padding).fillMaxSize()) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(RecordBg)
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp)
                        .imePadding()
                        .pointerInput(Unit) { detectTapGestures { focus.clearFocus() } }
                ) {
                    Spacer(Modifier.height(12.dp))

                    // 날짜/시간
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        AssistChip(
                            onClick = { showDatePicker = true },
                            label = { Text(viewModel.displayDate(viewModel.dateText.value)) },
                            colors = AssistChipDefaults.assistChipColors(
                                containerColor = Color(0xFF2C2C2C),
                                labelColor = Color.White
                            )
                        )
                        Spacer(Modifier.width(8.dp))
                        AssistChip(
                            onClick = { showTimePicker = true },
                            label = { Text(viewModel.displayTime12h()) },
                            colors = AssistChipDefaults.assistChipColors(
                                containerColor = Color(0xFF2C2C2C),
                                labelColor = Color.White
                            )
                        )
                        Spacer(Modifier.weight(1f))
                    }

                    Spacer(Modifier.height(16.dp))

                    // 사진
                    FitLogText(text = "사진", fontSize = 20, color = Color.White)
                    Spacer(Modifier.height(16.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // 원격 이미지 썸네일 (삭제 가능)
                        viewModel.remoteImageUrls.forEachIndexed { idx, url ->
                            Card(
                                modifier = Modifier.size(84.dp),
                                colors = CardDefaults.cardColors(containerColor = RecordCardBg),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Box(Modifier.fillMaxSize()) {
                                    AsyncImage(
                                        model = url,
                                        contentDescription = null,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                    IconButton(
                                        onClick = { viewModel.removeRemoteImageAt(idx) },
                                        modifier = Modifier
                                            .align(Alignment.TopEnd)
                                            .padding(2.dp)
                                            .size(24.dp)
                                    ) {
                                        Icon(Icons.Default.Close, contentDescription = null, tint = Color.White)
                                    }
                                }
                            }
                        }

                        // 로컬 이미지 썸네일 (삭제 가능 / 프리뷰)
                        viewModel.imageUris.forEachIndexed { index, uri ->
                            Card(
                                modifier = Modifier.size(84.dp),
                                colors = CardDefaults.cardColors(containerColor = RecordCardBg),
                                shape = RoundedCornerShape(8.dp),
                                onClick = {
                                    previewImageUri = uri
                                    showImageDialog = true
                                }
                            ) {
                                Box(Modifier.fillMaxSize()) {
                                    AsyncImage(
                                        model = uri,
                                        contentDescription = null,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                    IconButton(
                                        onClick = { viewModel.removeLocalImageAt(index) },
                                        modifier = Modifier
                                            .align(Alignment.TopEnd)
                                            .padding(2.dp)
                                            .size(24.dp)
                                    ) {
                                        Icon(Icons.Default.Close, contentDescription = null, tint = Color.White)
                                    }
                                }
                            }
                        }

                        // 추가 버튼 (총합 3장 제한)
                        if (viewModel.canAddMoreImage()) {
                            Card(
                                modifier = Modifier.size(84.dp),
                                colors = CardDefaults.cardColors(containerColor = RecordCardBg),
                                shape = RoundedCornerShape(8.dp),
                                onClick = {
                                    pickImageLauncher.launch(
                                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                    )
                                }
                            ) {
                                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                    Text("+", color = Color.White, style = MaterialTheme.typography.titleLarge)
                                }
                            }
                        }
                    }

                    Spacer(Modifier.height(20.dp))

                    // 메모
                    FitLogOutlinedTextField(
                        value = viewModel.recordMemo.value,
                        onValueChange = { viewModel.recordMemo.value = it },
                        label = "오늘 운동 메모",
                        singleLine = false
                    )

                    Spacer(Modifier.height(20.dp))

                    // 강도
                    FitLogText("강도", 20, color = Color.White)
                    Spacer(Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        viewModel.intensitiesColor.forEach { (inten, dot) ->
                            FilterChip(
                                selected = viewModel.intensity.value == inten,
                                onClick = { viewModel.setIntensity(inten) },
                                label = {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(Modifier.size(8.dp).background(dot, RoundedCornerShape(50)))
                                        Spacer(Modifier.width(6.dp))
                                        Text(inten.name)
                                    }
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    containerColor = Color(0xFF3C3C3C),
                                    selectedContainerColor = Color(0xFF47A6FF),
                                    labelColor = Color.White,
                                    selectedLabelColor = Color.White
                                )
                            )
                        }
                    }

                    Spacer(Modifier.height(25.dp))
                    HorizontalDivider(thickness = 0.5.dp, color = Color(0xFF404040))
                    Spacer(Modifier.height(25.dp))

                    // 헤더 + 루틴 불러오기
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                            FitLogText(
                                text = "운동" + if (viewModel.items.value.isNotEmpty()) " (${viewModel.items.value.size}개)" else "",
                                fontSize = 20, color = Color.White
                            )
                        }
                        RoutineExposedDropdownMenuBox(
                            label = viewModel.importLabel.value,
                            expanded = viewModel.importExpanded.value,
                            onExpandedChange = { viewModel.importExpanded.value = !viewModel.importExpanded.value },
                            routines = viewModel.importRoutines.value,
                            onSelect = { routineId, routineName ->
                                viewModel.applyImportRoutine(routineId)
                                viewModel.importLabel.value = routineName
                            },
                            minWidth = 150.dp, fieldHeight = 36.dp, placeholder = "루틴 불러오기"
                        )
                    }

                    Spacer(Modifier.height(16.dp))

                    // 운동 카드들
                    viewModel.items.value.forEachIndexed { index, ui: RoutineExerciseWithSets ->
                        ExerciseCard(
                            index = index + 1,
                            ui = ui,
                            onAddSet = { viewModel.addSet(ui.exercise.itemId) },
                            onDeleteSet = { setId -> viewModel.removeSet(ui.exercise.itemId, setId) },
                            onWeightChange = { setId, v -> viewModel.updateSetWeight(ui.exercise.itemId, setId, v) },
                            onRepsChange = { setId, v -> viewModel.updateSetReps(ui.exercise.itemId, setId, v) },
                            onDeleteExercise = { viewModel.removeExercise(ui.exercise.itemId) },
                            onReorderClick = { showReorderSheet = true }
                        )
                        Spacer(Modifier.height(12.dp))
                    }

                    // 새 운동 추가 (작성 화면과 동일 플로우를 쓰려면 선택 화면을 열어주는 네비게이션 필요)
                    FitLogButton(
                        text = "+",
                        onClick = {
                            // 선택 화면으로 이동시키는 네비게이션을 사용 중이라면 여기에 연결
                            // viewModel.onAddExerciseClick()
                            // 편집 화면에서 새 운동 추가가 필요 없다면 버튼을 숨겨도 됩니다.
                            viewModel.onAddExerciseClick()
                        },
                        backgroundColor = Color(0xFF3C3C3C),
                        horizontalPadding = 0.dp,
                        fontSize = 26
                    )

                    Spacer(Modifier.height(32.dp))
                }

                // 순서 변경 시트
                if (showReorderSheet) {
                    RoutineReorderBottomSheet(
                        sheetState = sheetState,
                        items = viewModel.items.value.map { it.exercise.itemId to it.exercise.exerciseName },
                        onSave = { newOrderIds ->
                            viewModel.applyReorder(newOrderIds)
                            showReorderSheet = false
                        },
                        onDismiss = { showReorderSheet = false }
                    )
                }

                // 이미지 전체 프리뷰
                if (showImageDialog && previewImageUri != null) {
                    Dialog(
                        onDismissRequest = { showImageDialog = false },
                        properties = DialogProperties(usePlatformDefaultWidth = false)
                    ) {
                        Box(Modifier.fillMaxSize().background(Color.Black)) {
                            IconButton(
                                onClick = { showImageDialog = false },
                                modifier = Modifier.align(Alignment.TopStart).padding(16.dp)
                            ) { Icon(Icons.Default.Close, contentDescription = null, tint = Color.White) }

                            AsyncImage(
                                model = previewImageUri,
                                contentDescription = null,
                                modifier = Modifier.fillMaxWidth().padding(vertical = 60.dp).align(Alignment.Center),
                                contentScale = ContentScale.Fit
                            )
                        }
                    }
                }

                // DatePicker
                if (showDatePicker) {
                    val datePickerState = rememberDatePickerState()
                    DatePickerDialog(
                        onDismissRequest = { showDatePicker = false },
                        confirmButton = {
                            TextButton(onClick = {
                                val millis = datePickerState.selectedDateMillis
                                if (millis != null) {
                                    val kst = java.time.Instant.ofEpochMilli(millis)
                                        .atZone(viewModel.zoneId).toLocalDate()
                                    viewModel.setDate(kst.format(DateTimeFormatter.ISO_DATE))
                                }
                                showDatePicker = false
                            }) { Text("확인") }
                        },
                        dismissButton = { TextButton(onClick = { showDatePicker = false }) { Text("취소") } }
                    ) { DatePicker(state = datePickerState) }
                }

                // TimePicker (12시간제)
                if (showTimePicker) {
                    val parts = viewModel.timeText.value.split(":")
                    val hour = parts.getOrNull(0)?.toIntOrNull() ?: 12
                    val minute = parts.getOrNull(1)?.toIntOrNull() ?: 0

                    FitLogTimePickerDialog(
                        initialHour = hour,
                        initialMinute = minute,
                        onConfirm = { h, m -> viewModel.setTime(h, m); showTimePicker = false },
                        onDismiss = { showTimePicker = false },
                        is24Hour = false
                    )
                }

                // 수정 확인 다이얼로그
                if (viewModel.showEditConfirm.value) {
                    FitLogAlertDialog(
                        title = "수정",
                        message = "이 기록을 수정하시겠습니까?",
                        onConfirm = {
                            viewModel.showEditConfirm.value = false
                            viewModel.onSave()
                        },
                        onDismiss = { viewModel.showEditConfirm.value = false },
                        showCancelButton = true
                    )
                }

                // 수정 취소 확인 다이얼로그
                if (viewModel.showEditCancelConfirm.value) {
                    FitLogAlertDialog(
                        title = "수정 취소",
                        message = "수정을 취소하고 이전 화면으로 돌아가시겠습니까?",
                        onConfirm = {
                            viewModel.showEditCancelConfirm.value = false
                            viewModel.onBack()
                        },
                        onDismiss = { viewModel.showEditCancelConfirm.value = false },
                        showCancelButton = true
                    )
                }
            }
        }

        // 로딩 오버레이
        LottieLoadingOverlay(
            isVisible = viewModel.loading.value,
            modifier = Modifier.fillMaxSize().align(Alignment.Center)
        )
    }
}
