package com.myprojects.routinemanager.ui.screens

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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.myprojects.routinemanager.data.model.Subtask
import com.myprojects.routinemanager.data.model.Task
import com.myprojects.routinemanager.data.model.TaskCategory
import com.myprojects.routinemanager.data.model.getCategoryColor
import com.myprojects.routinemanager.ui.viewmodel.TaskViewModel
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTaskScreen(
    viewModel: TaskViewModel,
    onTaskAdded: () -> Unit,
    taskId: String? = null,
    templateId: String? = null,
    isCreatingTemplate: Boolean = false // Flag for creating a standalone TaskTemplate
) {
    val editingTask: Task? = taskId?.let { viewModel.getTaskById(it) }

    // State variables using remember
    var title by remember { mutableStateOf(editingTask?.title ?: "") }
    var description by remember { mutableStateOf(editingTask?.description ?: "") }
    var newSubtaskText by remember { mutableStateOf("") }
    val subtasks = remember { // List to hold subtask titles (strings)
        mutableStateListOf<String>().apply {
            if (editingTask != null) {
                addAll(editingTask.subtasks.map { it.title })
            }
        }
    }
    var categoryExpanded by remember { mutableStateOf(false) }
    var selectedCategory by remember { mutableStateOf(editingTask?.category ?: TaskCategory.OTHER) }
    var startTime by remember { mutableStateOf<LocalTime?>(editingTask?.startTime) }
    var endTime by remember { mutableStateOf<LocalTime?>(editingTask?.endTime) }

    val invalidTime = remember(startTime, endTime) {
        startTime != null && endTime != null && startTime!!.isAfter(endTime)
    }
    // Use editing task's date if available, otherwise today
    val selectedDate = remember { editingTask?.date ?: LocalDate.now() }

    // Determine header and button text based on the screen's mode
    val headerText = when {
        isCreatingTemplate -> "Создать шаблон задачи"
        taskId != null -> "Редактировать задачу"
        templateId != null -> "Добавить в шаблон дня"
        else -> "Добавить задачу"
    }
    val buttonText = when {
        isCreatingTemplate -> "Сохранить шаблон"
        templateId != null -> "Добавить в шаблон дня"
        taskId != null -> "Сохранить изменения"
        else -> "Добавить задачу"
    }

    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .padding(bottom = 16.dp) // Padding at the bottom
            .verticalScroll(scrollState), // Make the column scrollable
        verticalArrangement = Arrangement.spacedBy(16.dp) // Spacing between elements
    ) {
        // Screen Header
        Text(
            text = headerText,
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(top = 16.dp)
        )

        // Task Title Input
        OutlinedTextField(
            value = title,
            onValueChange = { if (it.length <= 40) title = it },
            label = { Text("Название *") }, // Indicate required field
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            isError = title.isBlank() // Show error if title is empty
        )

        // Task Description Input
        OutlinedTextField(
            value = description,
            onValueChange = { if (it.length <= 100) description = it },
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
                leadingIcon = { // Show category color indicator
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
                            selectedCategory = category
                            categoryExpanded = false
                        }
                    )
                }
            }
        }

        // Start and End Time Pickers
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            Column(Modifier.weight(1f)) { TimePickerText("Время начала", startTime) { startTime = it } }
            Column(Modifier.weight(1f)) { TimePickerText("Время конца", endTime) { endTime = it } }
        }
        if (invalidTime) {
            Text("Время начала не может быть позже конца", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
        }

        // Subtasks Section
        Text("Подзадачи", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(top = 8.dp))
        SubtaskInputRow( // Use the original SubtaskSection name if preferred
            newSubtaskText = newSubtaskText,
            onTextChange = { newSubtaskText = it },
            onAddClick = {
                if (newSubtaskText.isNotBlank()) {
                    subtasks.add(newSubtaskText.trim())
                    newSubtaskText = ""
                }
            }
        )

        // Display added subtasks
        Column(modifier = Modifier.padding(top = 8.dp)) {
            subtasks.forEachIndexed { index, subtask ->
                SubtaskDisplayRow(
                    subtaskText = subtask,
                    onDeleteClick = { subtasks.removeAt(index) }
                )
                if (index < subtasks.lastIndex) {
                    Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f), thickness = 0.5.dp)
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp)) // Space before the main button

        // Save/Add Button
        Button(
            onClick = {
                val finalTitle = title.trim()
                val finalDescription = description.trim().ifEmpty { null }
                // Convert UI list of strings to List<Subtask> only when saving template
                val finalSubtasksObjects = if (isCreatingTemplate || editingTask != null) subtasks.map { Subtask(it) } else emptyList()

                when {
                    isCreatingTemplate -> {
                        viewModel.createStandaloneTaskTemplate( // Call VM to create standalone template
                            finalTitle, finalDescription, selectedCategory, startTime, endTime, finalSubtasksObjects
                        )
                    }
                    templateId != null -> {
                        viewModel.addTaskTemplateToTemplate( // Call VM to add to existing DayTemplate
                            templateId, finalTitle, finalDescription, selectedCategory, startTime, endTime, subtasks.toList() // Pass List<String> here
                        )
                    }
                    taskId != null && editingTask != null -> { // Editing existing Task
                        val updatedSubtasks = subtasks.map { subtaskTitle ->
                            val existing = editingTask.subtasks.find { it.title == subtaskTitle }
                            Subtask(title = subtaskTitle, isDone = existing?.isDone ?: false)
                        }
                        val updatedTask = editingTask.copy(
                            title = finalTitle, description = finalDescription, category = selectedCategory,
                            startTime = startTime, endTime = endTime, subtasks = updatedSubtasks
                        )
                        viewModel.updateTask(updatedTask)
                    }
                    else -> { // Creating a new regular Task
                        viewModel.addTask( // Pass List<String> here
                            finalTitle, finalDescription, selectedCategory, startTime, endTime, selectedDate, subtasks.toList()
                        )
                    }
                }
                onTaskAdded() // Navigate back after action
            },
            enabled = title.isNotBlank() && !invalidTime,
            modifier = Modifier.fillMaxWidth() // Button takes full width
        ) {
            Text(buttonText)
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
        Divider(color = MaterialTheme.colorScheme.outlineVariant, thickness = 1.dp)
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