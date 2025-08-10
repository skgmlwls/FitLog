package com.nhj.fitlog.presentation.routine.add.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DragHandle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import org.burnoutcrew.reorderable.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoutineReorderBottomSheet(
    sheetState: SheetState,
    items: List<Pair<String, String>>, // (itemId, name)
    onSave: (List<String>) -> Unit,
    onDismiss: () -> Unit,
) {
    // 시트 내부에서 편집할 로컬 리스트 (id, name)
    val local = remember(items) { items.toMutableStateList() }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color(0xFF1E1E1E)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp)
        ) {
            Text(
                "운동 순서 변경",
                color = Color.White,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(Modifier.height(8.dp))
            Text("길게 눌러 드래그해서 순서를 바꾸세요.", color = Color(0xFFBDBDBD))
            Spacer(Modifier.height(12.dp))

            // 🔧 reorderable 상태
            val reorderState = rememberReorderableLazyListState(
                onMove = { from, to ->
                    local.add(to.index, local.removeAt(from.index))
                }
            )

            LazyColumn(
                state = reorderState.listState,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 420.dp) // 시트 높이 제한
                    .reorderable(reorderState)
                    .detectReorderAfterLongPress(reorderState)
                    .background(Color(0xFF262626), shape = MaterialTheme.shapes.medium)
                    .padding(vertical = 6.dp)
            ) {
                itemsIndexed(local, key = { _, it -> it.first }) { _, pair ->
                    ReorderableItem(reorderState, key = pair.first) { dragging ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(if (dragging) Color(0xFF2E2E2E) else Color.Transparent)
                                .padding(horizontal = 12.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // 드래그 핸들
                            Icon(
                                imageVector = Icons.Default.DragHandle,
                                contentDescription = "drag",
                                tint = Color.White.copy(alpha = 0.8f),
                                modifier = Modifier
                                    .size(24.dp)
                                    .detectReorder(reorderState)
                            )
                            Spacer(Modifier.width(12.dp))
                            Text(
                                text = pair.second,
                                color = Color.White,
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
                ) { Text("취소") }

                Button(
                    onClick = { onSave(local.map { it.first }) },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF47A6FF))
                ) { Text("저장", color = Color.White) }
            }

            Spacer(Modifier.height(18.dp))
        }
    }
}