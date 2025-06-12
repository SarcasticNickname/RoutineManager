package com.myprojects.routinemanager.ui.screens.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DragHandle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.myprojects.routinemanager.data.model.Subtask
import org.burnoutcrew.reorderable.ReorderableItem
import org.burnoutcrew.reorderable.detectReorderAfterLongPress
import org.burnoutcrew.reorderable.rememberReorderableLazyListState
import org.burnoutcrew.reorderable.reorderable

// Переименовано в SubtaskEditorContent и убрана обертка ModalBottomSheet
@Composable
fun SubtaskEditorContent( // Изменили название и параметры
    localSubtasks: SnapshotStateList<Subtask>, // Принимаем SnapshotStateList для реактивности
    onNewSubtaskTextChange: (String) -> Unit,
    newSubtaskText: String,
    onAddSubtask: () -> Unit,
    onSave: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier // Добавили Modifier
) {
    val keyboardController = LocalSoftwareKeyboardController.current
    val configuration = LocalConfiguration.current
    val screenHeight = configuration.screenHeightDp.dp

    val reorderState = rememberReorderableLazyListState(onMove = { from, to ->
        localSubtasks.apply {
            add(to.index, removeAt(from.index))
        }
    })

    Column(
        modifier = modifier // Используем переданный Modifier
            .fillMaxWidth()
            .navigationBarsPadding() // Важно для отступов от системных панелей
            .padding(horizontal = 16.dp)
            .heightIn(max = screenHeight * 0.7f)
    ) {
        Text(
            "Подзадачи",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        LazyColumn(
            state = reorderState.listState,
            modifier = Modifier
                .weight(1f)
                .reorderable(reorderState)
                .detectReorderAfterLongPress(reorderState)
        ) {
            itemsIndexed(
                localSubtasks,
                key = { _, subtask -> subtask.hashCode() }) { index, subtask ->
                ReorderableItem(
                    reorderableState = reorderState,
                    key = subtask.hashCode()
                ) { isDragging ->
                    val elevation = if (isDragging) 8.dp else 0.dp
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .shadow(elevation)
                    ) {
                        Icon(
                            imageVector = Icons.Default.DragHandle,
                            contentDescription = "Перетащить",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(end = 8.dp)
                        )
                        Checkbox(
                            checked = subtask.isDone,
                            onCheckedChange = { isChecked ->
                                localSubtasks[index] = subtask.copy(isDone = isChecked)
                            }
                        )
                        Text(
                            text = subtask.title,
                            modifier = Modifier
                                .weight(1f)
                                .padding(horizontal = 8.dp)
                        )
                        IconButton(onClick = { localSubtasks.removeAt(index) }) {
                            Icon(
                                Icons.Default.Close,
                                contentDescription = "Удалить подзадачу",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            }
        }

        Row(
            modifier = Modifier.padding(vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = newSubtaskText,
                onValueChange = onNewSubtaskTextChange,
                label = { Text("Новая подзадача") },
                modifier = Modifier.weight(1f),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = {
                    if (newSubtaskText.isNotBlank()) {
                        onAddSubtask() // Используем колбэк
                        keyboardController?.hide()
                    }
                })
            )
            IconButton(
                onClick = {
                    if (newSubtaskText.isNotBlank()) {
                        onAddSubtask() // Используем колбэк
                    }
                },
                enabled = newSubtaskText.isNotBlank()
            ) {
                Icon(Icons.Default.Add, "Добавить")
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp), // Отступ снизу для кнопок
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextButton(onClick = onDismiss) {
                Text("Отмена")
            }
            Spacer(modifier = Modifier.width(8.dp))
            Button(onClick = onSave) {
                Text("Сохранить")
            }
        }
    }
}