package com.nhj.fitlog.presentation.coach

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.nhj.fitlog.component.FitLogTopBar
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch

@Composable
fun CoachScreen(vm: CoachViewModel = hiltViewModel()) {
    var input by remember { mutableStateOf("") }
    val msgs: SnapshotStateList<ChatMsg> = vm.messages
    val isLoading by remember { vm.loading }
    val err by remember { vm.error }
    val info by remember { vm.info }

    val snackbarHostState = remember { SnackbarHostState() }
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()

    // ✅ 최초 진입 시 예시 메시지 시드
    LaunchedEffect(Unit) { vm.ensureIntro() }

    // ✅ 스낵바
    LaunchedEffect(err) { if (!err.isNullOrBlank()) snackbarHostState.showSnackbar(err!!) }
    LaunchedEffect(info) { if (!info.isNullOrBlank()) snackbarHostState.showSnackbar(info!!) }

    /**
     * ✅ 자동 스크롤 로직
     * - isNearBottom: 사용자가 거의 바닥(마지막에서 1개 이전까지) 보고 있으면 true
     * - 새 아이템이 추가되면(크기 변화) isNearBottom일 때만 애니메이션으로 맨 아래로
     * - 스트리밍 중엔 마지막 메시지의 길이 변화를 관찰해, isNearBottom일 때만 즉시 스크롤
     */
    val isNearBottom by remember {
        derivedStateOf {
            val lastIndex = msgs.lastIndex
            if (lastIndex < 0) return@derivedStateOf true
            val lastVisible = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            // 마지막 아이템 또는 그 하나 전까지 보이면 "바닥 근처"로 판단
            lastVisible >= lastIndex - 1
        }
    }

    // 1) 첫 렌더링/초기 시드 이후 한 번 아래로
    LaunchedEffect(Unit) {
        if (msgs.isNotEmpty()) {
            listState.scrollToItem(msgs.lastIndex)
        }
    }

    // 2) 메시지 개수가 변할 때(유저 질문 추가, 어시스턴트 버블 추가 등) 바닥 근처면 아래로
    LaunchedEffect(msgs.size) {
        if (msgs.isNotEmpty() && isNearBottom) {
            listState.animateScrollToItem(msgs.lastIndex)
        }
    }

    // 3) 스트리밍 중엔 마지막 메시지의 글자수 변화를 관찰해 바닥 근처면 계속 따라가기
    LaunchedEffect(isLoading) {
        if (!isLoading) return@LaunchedEffect
        snapshotFlow { msgs.lastOrNull()?.content?.length ?: -1 }
            .distinctUntilChanged()
            .collectLatest {
                if (isNearBottom && msgs.isNotEmpty()) {
                    // 즉시 스크롤(미세한 타이핑 변화에 부드럽게 따라감)
                    listState.scrollToItem(msgs.lastIndex)
                }
            }
    }

    Scaffold(
        topBar = {
            FitLogTopBar(
                title = "AI 코치",
                onBackClick = {
                    vm.onBackNavigation()
                },
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            ChatInputBar(
                value = input,
                enabled = !isLoading,
                onValueChange = { input = it },
                onSend = {
                    if (input.isNotBlank() && !isLoading) {
                        val sessionId = "ses-${System.currentTimeMillis()}"
                        vm.askCoachStreaming(sessionId, input.trim())
                        input = ""
                    }
                }
            )
        }
    ) { inner ->
        // 배경 그라데이션
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(Color(0xFF0B1220), Color(0xFF0F1629))
                    )
                )
                .padding(inner)
        ) {
            if (msgs.isEmpty()) {
                EmptyHint()
            }

            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 16.dp)
            ) {
                itemsIndexed(
                    items = msgs,
                    key = { index, _ -> index } // 인덱스 키(메시지에 id 없어서 안전하게 사용)
                ) { idx, m ->
                    val isUser = m.role == "user"
                    val isLast = idx == msgs.lastIndex
                    val isStreaming = !isUser && isLast && isLoading

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp),
                        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
                    ) {
                        if (!isUser) {
                            CircleDot(
                                color = Color(0xFF2E7D32),
                                size = 10.dp,
                                modifier = Modifier.align(Alignment.Bottom)
                            )
                            Spacer(Modifier.width(8.dp))
                        }

                        AssistantOrUserBubble(
                            message = m,
                            isUser = isUser,
                            isStreaming = isStreaming,
                            onActionClick = { act -> vm.runAction(act) }
                        )

                        if (isUser) {
                            Spacer(Modifier.width(8.dp))
                            CircleDot(
                                color = Color(0xFF1E88E5),
                                size = 10.dp,
                                modifier = Modifier.align(Alignment.Bottom)
                            )
                        }
                    }
                }
                item { Spacer(Modifier.height(8.dp)) }
            }
        }
    }
}

@Composable
private fun EmptyHint() {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("최근 운동을 요약해 드릴까요?", color = Color(0xFF9FB2D8))
        Spacer(Modifier.height(6.dp))
        Text("예) “최근 운동 요약해줘”, “가슴/등 편향 분석해줘”", color = Color(0xFF6E7FA3))
    }
}

@Composable
private fun AssistantOrUserBubble(
    message: ChatMsg,
    isUser: Boolean,
    isStreaming: Boolean = false,
    onActionClick: (CoachAction) -> Unit
) {
    val bubbleColor = if (isUser) Color(0xFF1E88E5) else Color(0xFF2E7D32)
    val bg = bubbleColor.copy(alpha = 0.15f)
    val shape = if (isUser)
        RoundedCornerShape(topStart = 14.dp, topEnd = 4.dp, bottomEnd = 14.dp, bottomStart = 14.dp)
    else
        RoundedCornerShape(topStart = 4.dp, topEnd = 14.dp, bottomEnd = 14.dp, bottomStart = 14.dp)

    Surface(color = bg, shape = shape, tonalElevation = 0.dp, shadowElevation = 0.dp) {
        Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp)) {
            // 본문 텍스트
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = message.content, color = Color.White)
                if (isStreaming) {
                    Spacer(Modifier.width(6.dp))
                    TypingDots()
                }
            }
        }
    }
}

/** 액션 라벨링(간단 버전) */
private fun actionLabel(a: CoachAction): String = when (a.type) {
    "plan_next_week" -> "다음 주 계획 적용"
    else -> a.type
}

/** 간단 Wrap 레이아웃(Compose 1.6+: FlowRow 대체용) */
@Composable
private fun FlowRowMainAxisWrap(
    mainAxisSpacing: Dp,
    crossAxisSpacing: Dp,
    content: @Composable RowScope.() -> Unit
) {
    // 안정적 구현을 위해 Row + Flow-like spacing 제공
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(mainAxisSpacing)
    ) {
        content()
    }
}

@Composable
private fun TypingDots() {
    val transition = rememberInfiniteTransition(label = "dots")
    val a by transition.animateFloat(
        initialValue = 0.3f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(700, easing = FastOutSlowInEasing)),
        label = "a"
    )
    val b by transition.animateFloat(
        initialValue = 0.3f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(700, delayMillis = 150, easing = FastOutSlowInEasing)),
        label = "b"
    )
    val c by transition.animateFloat(
        initialValue = 0.3f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(700, delayMillis = 300, easing = FastOutSlowInEasing)),
        label = "c"
    )
    Row(verticalAlignment = Alignment.CenterVertically) {
        Dot(modifier = Modifier.alpha(a))
        Spacer(Modifier.width(4.dp))
        Dot(modifier = Modifier.alpha(b))
        Spacer(Modifier.width(4.dp))
        Dot(modifier = Modifier.alpha(c))
    }
}

@Composable
private fun Dot(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .size(6.dp)
            .background(Color(0xFFB6D2B6), CircleShape)
    )
}

@Composable
private fun CircleDot(color: Color, size: Dp, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .size(size)
            .background(color.copy(alpha = 0.35f), CircleShape)
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ChatInputBar(
    value: String,
    enabled: Boolean,
    onValueChange: (String) -> Unit,
    onSend: () -> Unit
) {
    Surface(tonalElevation = 2.dp) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            verticalAlignment = Alignment.Bottom
        ) {
            OutlinedTextField(
                value = value,
                onValueChange = onValueChange,
                modifier = Modifier.weight(1f),
                placeholder = { Text("메시지를 입력하세요") },
                enabled = enabled,
                maxLines = 5
            )
            Spacer(Modifier.width(8.dp))
            FilledIconButton(
                onClick = onSend,
                enabled = enabled && value.isNotBlank()
            ) {
                Icon(Icons.Rounded.Send, contentDescription = "send")
            }
        }
    }
}
