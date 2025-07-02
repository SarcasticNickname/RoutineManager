package com.myprojects.routinemanager.ui.screens.components

import android.app.TimePickerDialog
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.myprojects.routinemanager.data.model.TaskCategory
import com.myprojects.routinemanager.data.model.getCategoryColor
import java.time.LocalTime
import java.time.format.DateTimeFormatter

/**
 * Общий "глупый" компонент, содержащий UI формы для создания/редактирования задачи или шаблона.
 * Он не содержит логики или состояния, а только отображает переданные данные.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskFormContent(
    modifier: Modifier = Modifier,
    headerText: String,
    title: String,
    onTitleChange: (String) -> Unit,
    description: String,
    onDescriptionChange: (String) -> Unit,
    selectedCategory: TaskCategory,
    onCategorySelected: (TaskCategory) -> Unit,
    startTime: LocalTime?,
    onStartTimeSelected: (LocalTime) -> Unit,
    endTime: LocalTime?,
    onEndTimeSelected: (LocalTime) -> Unit,
    subtasks: SnapshotStateList<String>,
    newSubtaskText: String,
    onNewSubtaskTextChange: (String) -> Unit,
    onAddSubtask: () -> Unit,
    onDeleteSubtask: (Int) -> Unit
) {
    var categoryExpanded by remember { mutableStateOf(false) }
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .verticalScroll(scrollState),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Screen Header
        Text(
            text = headerText,
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
        )

        // Task Title Input
        OutlinedTextField(
            value = title,
            onValueChange = { if (it.length <= 40) onTitleChange(it) },
            label = { Text("Название *") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            isError = title.isBlank()
        )

        // Task Description Input
        OutlinedTextField(
            value = description,
            onValueChange = { if (it.length <= 100) onDescriptionChange(it) },
            label = { Text("Описание") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 2,
            maxLines = 4
        )

        // Category Selector
        ExposedDropdownMenuBox(
            expanded = categoryExpanded,
            onExpandedChange = { categoryExpanded = !categoryExpanded }
        ) {
            OutlinedTextField(
                value = selectedCategory.name,
                onValueChange = {},
                readOnly = true,
                label = { Text("Категория") },
                leadingIcon = {
                    Box(modifier = Modifier.size(18.dp).clip(CircleShape).background(getCategoryColor(selectedCategory)))
                },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryExpanded) },
                modifier = Modifier.menuAnchor().fillMaxWidth().clickable { categoryExpanded = true }
            )
            ExposedDropdownMenu(expanded = categoryExpanded, onDismissRequest = { categoryExpanded = false }) {
                TaskCategory.values().forEach { category ->
                    DropdownMenuItem(
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(modifier = Modifier.size(12.dp).background(getCategoryColor(category), CircleShape))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(category.name)
                            }
                        },
                        onClick = {
                            onCategorySelected(category)
                            categoryExpanded = false
                        }
                    )
                }
            }
        }

        // Start and End Time Pickers
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            Column(Modifier.weight(1f)) { TimePickerText("Время начала", startTime, onTimeSelected = onStartTimeSelected) }
            Column(Modifier.weight(1f)) { TimePickerText("Время конца", endTime, onTimeSelected = onEndTimeSelected) }
        }
        val invalidTime = startTime != null && endTime != null && startTime.isAfter(endTime)
        if (invalidTime) {
            Text("Время начала не может быть позже конца", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
        }

        // Subtasks Section
        Text("Подзадачи", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(top = 8.dp))
        SubtaskInputRow(
            newSubtaskText = newSubtaskText,
            onTextChange = onNewSubtaskTextChange,
            onAddClick = onAddSubtask
        )

        // Display added subtasks
        Column(modifier = Modifier.padding(top = 8.dp)) {
            subtasks.forEachIndexed { index, subtask ->
                SubtaskDisplayRow(
                    subtaskText = subtask,
                    onDeleteClick = { onDeleteSubtask(index) }
                )
                if (index < subtasks.lastIndex) {
                    HorizontalDivider(
                        thickness = 0.5.dp,
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                    )
                }
            }
        }
    }
}

// Composable for displaying the time picker text/button
@Composable
private fun TimePickerText(label: String, time: LocalTime?, onTimeSelected: (LocalTime) -> Unit) {
    val context = LocalContext.current
    val formatter = remember { DateTimeFormatter.ofPattern("HH:mm") }

    Column {
        Text(label, style = MaterialTheme.typography.labelMedium)
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = time?.format(formatter) ?: "Выбрать",
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    val now = LocalTime.now()
                    TimePickerDialog( context, { _, hour, minute -> onTimeSelected(LocalTime.of(hour, minute)) },
                        time?.hour ?: now.hour, time?.minute ?: now.minute, true ).show()
                }
                .padding(vertical = 8.dp),
            style = MaterialTheme.typography.bodyLarge.copy(
                color = if (time != null) LocalContentColor.current else MaterialTheme.colorScheme.outline,
                textDecoration = TextDecoration.Underline
            )
        )
        HorizontalDivider(thickness = 1.dp, color = MaterialTheme.colorScheme.outlineVariant)
    }
}

// Composable for the subtask input row
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SubtaskInputRow(
    newSubtaskText: String,
    onTextChange: (String) -> Unit,
    onAddClick: () -> Unit
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        OutlinedTextField(
            value = newSubtaskText,
            onValueChange = onTextChange,
            label = { Text("Новая подзадача") },
            modifier = Modifier.weight(1f),
            singleLine = true
        )
        Button(
            onClick = onAddClick,
            enabled = newSubtaskText.isNotBlank(),
            contentPadding = PaddingValues(horizontal = 12.dp)
        ) {
            Text("+")
        }
    }
}

// Composable for displaying an existing subtask row
@Composable
private fun SubtaskDisplayRow( subtaskText: String, onDeleteClick: () -> Unit ) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
    ) {
        Text(text = "• $subtaskText", modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium)
        IconButton(onClick = onDeleteClick, modifier = Modifier.size(36.dp)) {
            Icon(Icons.Filled.DeleteOutline, contentDescription = "Удалить", tint = MaterialTheme.colorScheme.error)
        }
    }
}