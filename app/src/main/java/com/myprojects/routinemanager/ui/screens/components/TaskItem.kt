package com.myprojects.routinemanager.ui.screens.components

import android.app.TimePickerDialog
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material.ContentAlpha
import com.myprojects.routinemanager.data.model.Task
import com.myprojects.routinemanager.data.model.getCategoryColor
import java.time.LocalTime
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TaskItem(
    modifier: Modifier = Modifier,
    task: Task,
    onCheck: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onUpdateTask: (Task) -> Unit,
    onEditSubtasks: () -> Unit,
    onToggleSubtask: (task: Task, subtaskIndex: Int) -> Unit
) {
    val categoryColor = getCategoryColor(task.category)
    var expanded by remember { mutableStateOf(false) }

    var showEditTimeDialog by remember { mutableStateOf(false) }
    var showEditTitleDialog by remember { mutableStateOf(false) }
    var showEditDescriptionDialog by remember { mutableStateOf(false) }

    val cardContainerColor by animateColorAsState(
        targetValue = if (task.isDone) MaterialTheme.colorScheme.surfaceColorAtElevation(1.dp)
        else MaterialTheme.colorScheme.surfaceVariant,
        label = "CardContainerColorAnimation"
    )
    val baseCardElevation = if (task.isDone) 1.dp else 2.dp // Базовая тень

    val primaryTextColor =
        if (task.isDone) MaterialTheme.colorScheme.onSurface.copy(alpha = ContentAlpha.medium) else MaterialTheme.colorScheme.onSurface
    val secondaryTextColor =
        if (task.isDone) MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = ContentAlpha.medium) else MaterialTheme.colorScheme.onSurfaceVariant
    val outlineColor =
        if (task.isDone) MaterialTheme.colorScheme.outline.copy(alpha = ContentAlpha.medium) else MaterialTheme.colorScheme.outline


    Card(
        modifier = modifier // Применяем внешний модификатор (для тени и других эффектов от ReorderableItem)
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp)
            .combinedClickable(onClick = {
                expanded = !expanded
            }), // Долгое нажатие теперь обрабатывается reorderable
        colors = CardDefaults.cardColors(containerColor = cardContainerColor),
        elevation = CardDefaults.cardElevation(defaultElevation = baseCardElevation)
    ) {
        Column {
            Row(modifier = Modifier.height(IntrinsicSize.Min)) {
                Box(
                    modifier = Modifier
                        .width(6.dp)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(topStart = 12.dp, bottomStart = 12.dp))
                        .background(if (task.isDone) categoryColor.copy(alpha = 0.5f) else categoryColor)
                )
                Column(
                    modifier = Modifier
                        .padding(horizontal = 12.dp, vertical = 10.dp)
                        .weight(1f)
                ) {
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .padding(end = 8.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.AutoMirrored.Filled.Assignment,
                                    contentDescription = null,
                                    tint = secondaryTextColor,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = task.title,
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        textDecoration = if (task.isDone) TextDecoration.LineThrough else null
                                    ),
                                    color = primaryTextColor
                                )
                            }
                            if (!task.description.isNullOrEmpty()) {
                                Text(
                                    text = task.description,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = secondaryTextColor,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.padding(top = 4.dp)
                                )
                            }
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(top = 6.dp)
                            ) {
                                Icon(
                                    Icons.Default.Schedule,
                                    contentDescription = null,
                                    tint = outlineColor,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    formatTimeRange(task.startTime, task.endTime),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = outlineColor
                                )
                            }
                        }
                        Row(verticalAlignment = Alignment.Top) {
                            var menuExpanded by remember { mutableStateOf(false) }
                            Box {
                                IconButton(
                                    onClick = { menuExpanded = true },
                                    modifier = Modifier.size(36.dp),
                                    enabled = !task.isDone
                                ) {
                                    Icon(
                                        Icons.Default.MoreVert,
                                        contentDescription = "Меню",
                                        tint = if (task.isDone) secondaryTextColor else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                DropdownMenu(
                                    expanded = menuExpanded,
                                    onDismissRequest = { menuExpanded = false }) {
                                    DropdownMenuItem(
                                        text = { Text("Редактировать") },
                                        onClick = { menuExpanded = false; onEdit() })
                                    DropdownMenuItem(
                                        text = { Text("Удалить") },
                                        onClick = { menuExpanded = false; onDelete() })
                                }
                            }
                            Checkbox(
                                checked = task.isDone,
                                onCheckedChange = { onCheck() },
                                modifier = Modifier.size(36.dp),
                                colors = CheckboxDefaults.colors(
                                    checkedColor = MaterialTheme.colorScheme.primary,
                                    uncheckedColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                    checkmarkColor = MaterialTheme.colorScheme.onPrimary
                                )
                            )
                        }
                    }
                }
            }

            AnimatedVisibility(visible = expanded && !task.isDone) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 12.dp, end = 12.dp, bottom = 10.dp, top = 0.dp)
                ) {
                    Divider(
                        modifier = Modifier
                            .padding(bottom = 0.dp)
                            .fillMaxWidth(),
                        thickness = 1.dp,
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                    )
                    QuickEditActionsBar(
                        onEditTimeClick = { showEditTimeDialog = true },
                        onAddSubtaskClick = onEditSubtasks,
                        onEditTitleClick = { showEditTitleDialog = true },
                        onEditDescriptionClick = { showEditDescriptionDialog = true }
                    )
                    if (task.subtasks.isNotEmpty()) {
                        Divider(
                            modifier = Modifier
                                .padding(horizontal = 4.dp, vertical = 4.dp)
                                .fillMaxWidth(),
                            thickness = 1.dp,
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.List,
                                contentDescription = null,
                                tint = secondaryTextColor,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                "Подзадачи:",
                                style = MaterialTheme.typography.labelMedium,
                                color = secondaryTextColor
                            )
                        }
                        task.subtasks.forEachIndexed { index, subtask ->
                            val subtaskTextColor =
                                if (subtask.isDone) MaterialTheme.colorScheme.onSurface.copy(alpha = ContentAlpha.disabled) else MaterialTheme.colorScheme.onSurface
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(start = 8.dp)
                            ) {
                                Text(
                                    text = subtask.title,
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        textDecoration = if (subtask.isDone) TextDecoration.LineThrough else null
                                    ),
                                    color = subtaskTextColor,
                                    modifier = Modifier.weight(1f)
                                )
                                Checkbox(
                                    checked = subtask.isDone,
                                    onCheckedChange = { onToggleSubtask(task, index) },
                                    modifier = Modifier.size(40.dp),
                                    colors = CheckboxDefaults.colors(
                                        checkedColor = MaterialTheme.colorScheme.primary,
                                        uncheckedColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                        checkmarkColor = MaterialTheme.colorScheme.onPrimary
                                    )
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showEditTimeDialog && !task.isDone) {
        EditTimeDialog(
            task = task,
            onDismiss = { showEditTimeDialog = false },
            onConfirm = { newStartTime, newEndTime ->
                onUpdateTask(
                    task.copy(
                        startTime = newStartTime,
                        endTime = newEndTime
                    )
                ); showEditTimeDialog = false
            })
    }
    if (showEditTitleDialog && !task.isDone) {
        EditTextFieldDialog(
            title = "Изменить название",
            label = "Новое название",
            initialValue = task.title,
            onDismiss = { showEditTitleDialog = false },
            onConfirm = { newTitle ->
                onUpdateTask(task.copy(title = newTitle)); showEditTitleDialog = false
            })
    }
    if (showEditDescriptionDialog && !task.isDone) {
        EditTextFieldDialog(
            title = "Изменить описание",
            label = "Новое описание",
            initialValue = task.description ?: "",
            singleLine = false,
            onDismiss = { showEditDescriptionDialog = false },
            onConfirm = { newDescription ->
                onUpdateTask(task.copy(description = newDescription.ifBlank { null })); showEditDescriptionDialog =
                false
            })
    }
}

@Composable
private fun EditTimeDialog(
    task: Task,
    onDismiss: () -> Unit,
    onConfirm: (LocalTime?, LocalTime?) -> Unit
) {
    var startTime by remember { mutableStateOf(task.startTime) }
    var endTime by remember { mutableStateOf(task.endTime) }
    val context = LocalContext.current

    val timeFormatter = remember { DateTimeFormatter.ofPattern("HH:mm") }

    // <<<--- НОВОЕ: Состояние для проверки валидности времени ---<<<
    val isTimeInvalid by remember(startTime, endTime) {
        derivedStateOf {
            startTime != null && endTime != null && startTime!!.isAfter(endTime!!)
        }
    }

    val startTimePicker = TimePickerDialog(
        context, { _, hour, minute -> startTime = LocalTime.of(hour, minute) },
        startTime?.hour ?: LocalTime.now().hour, startTime?.minute ?: LocalTime.now().minute, true
    )

    val endTimePicker = TimePickerDialog(
        context,
        { _, hour, minute -> endTime = LocalTime.of(hour, minute) },
        endTime?.hour ?: startTime?.hour ?: LocalTime.now().hour,
        endTime?.minute ?: startTime?.minute ?: LocalTime.now().minute,
        true
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Изменить время") },
        text = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    OutlinedButton(
                        onClick = { startTimePicker.show() },
                        modifier = Modifier.weight(1f),
                        shape = MaterialTheme.shapes.medium
                    ) {
                        Text(startTime?.format(timeFormatter) ?: "Начало")
                    }
                    OutlinedButton(
                        onClick = { endTimePicker.show() },
                        modifier = Modifier.weight(1f),
                        shape = MaterialTheme.shapes.medium
                    ) {
                        Text(endTime?.format(timeFormatter) ?: "Конец")
                    }
                }
                // <<<--- НОВОЕ: Показ предупреждения, если время некорректно ---<<<
                if (isTimeInvalid) {
                    Text(
                        text = "Время начала не может быть позже времени окончания.",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            }
        },
        confirmButton = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(
                    onClick = {
                        startTime = null
                        endTime = null
                    }
                ) {
                    Text("Очистить")
                }
                Spacer(Modifier.weight(1f))
                TextButton(onClick = onDismiss) {
                    Text("Отмена")
                }
                Spacer(Modifier.width(8.dp))
                // <<<--- НОВОЕ: Кнопка "Сохранить" деактивируется, если время некорректно ---<<<
                Button(
                    onClick = { onConfirm(startTime, endTime) },
                    enabled = !isTimeInvalid // Кнопка активна, только если время валидно
                ) {
                    Text("Сохранить")
                }
            }
        },
        dismissButton = { }
    )
}

@Composable
private fun EditTextFieldDialog(
    title: String,
    label: String,
    initialValue: String = "",
    singleLine: Boolean = true,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var text by remember { mutableStateOf(initialValue) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            OutlinedTextField(
                value = text,
                onValueChange = { text = it },
                label = { Text(label) },
                singleLine = singleLine,
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            Button(onClick = { onConfirm(text.trim()) }, enabled = text.isNotBlank()) {
                Text("Сохранить")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Отмена")
            }
        }
    )
}


fun formatTimeRange(start: LocalTime?, end: LocalTime?): String {
    val formatter = DateTimeFormatter.ofPattern("HH:mm")
    return when {
        start != null && end != null -> "${start.format(formatter)} - ${end.format(formatter)}"
        start != null -> "с ${start.format(formatter)}"
        end != null -> "до ${end.format(formatter)}"
        else -> "Время не указано"
    }
}