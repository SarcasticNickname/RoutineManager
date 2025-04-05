package com.myprojects.routinemanager.ui.screens

import android.app.TimePickerDialog
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.myprojects.routinemanager.data.model.TaskCategory
import com.myprojects.routinemanager.data.model.TaskTemplate
import com.myprojects.routinemanager.data.model.getCategoryColor
import com.myprojects.routinemanager.ui.viewmodel.TaskViewModel
import java.time.LocalDate
import java.time.LocalTime

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTaskScreen(
    viewModel: TaskViewModel,
    onTaskAdded: () -> Unit
) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var newSubtaskText by remember { mutableStateOf("") }
    val subtasks = remember { mutableStateListOf<String>() }

    // Категория
    var expanded by remember { mutableStateOf(false) }
    var selectedCategory by remember { mutableStateOf(TaskCategory.OTHER) }

    // Время
    val context = LocalContext.current
    var startTime by remember { mutableStateOf<LocalTime?>(null) }
    var endTime by remember { mutableStateOf<LocalTime?>(null) }

    val invalidTime = remember(startTime, endTime) {
        startTime != null && endTime != null && startTime!!.isAfter(endTime)
    }

    val selectedDate = remember { LocalDate.now() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Добавить задачу", style = MaterialTheme.typography.titleLarge)

        TextField(
            value = title,
            onValueChange = { if (it.length <= 40) title = it },
            label = { Text("Название задачи") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        TextField(
            value = description,
            onValueChange = { if (it.length <= 100) description = it },
            label = { Text("Описание задачи") },
            modifier = Modifier.fillMaxWidth(),
            maxLines = 3
        )

        // Категория
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded }
        ) {
            TextField(
                value = selectedCategory.name,
                onValueChange = {},
                readOnly = true,
                label = { Text("Категория") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
                modifier = Modifier.menuAnchor().fillMaxWidth()
            )
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                TaskCategory.values().forEach { category ->
                    DropdownMenuItem(
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(12.dp)
                                        .background(getCategoryColor(category), CircleShape)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(category.name)
                            }
                        },
                        onClick = {
                            selectedCategory = category
                            expanded = false
                        }
                    )
                }
            }
        }

        // Время начала
        Text("Время начала", style = MaterialTheme.typography.labelMedium)
        Text(
            text = startTime?.toString() ?: "Выбрать время начала",
            modifier = Modifier
                .clickable {
                    val now = LocalTime.now()
                    TimePickerDialog(
                        context,
                        { _, hour, minute ->
                            startTime = LocalTime.of(hour, minute)
                        },
                        now.hour, now.minute, true
                    ).show()
                }
                .padding(bottom = 8.dp),
            style = MaterialTheme.typography.bodyLarge.copy(
                color = if (startTime != null) MaterialTheme.colorScheme.onSurface else Color.Gray,
                textDecoration = TextDecoration.Underline
            )
        )

        // Время конца
        Text("Время конца", style = MaterialTheme.typography.labelMedium)
        Text(
            text = endTime?.toString() ?: "Выбрать время конца",
            modifier = Modifier
                .clickable {
                    val now = LocalTime.now()
                    TimePickerDialog(
                        context,
                        { _, hour, minute ->
                            endTime = LocalTime.of(hour, minute)
                        },
                        now.hour, now.minute, true
                    ).show()
                }
                .padding(bottom = 8.dp),
            style = MaterialTheme.typography.bodyLarge.copy(
                color = if (endTime != null) MaterialTheme.colorScheme.onSurface else Color.Gray,
                textDecoration = TextDecoration.Underline
            )
        )

        if (invalidTime) {
            Text(
                "Время начала не может быть позже конца",
                color = Color.Red,
                style = MaterialTheme.typography.bodySmall
            )
        }

        // Подзадачи
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            TextField(
                value = newSubtaskText,
                onValueChange = { newSubtaskText = it },
                label = { Text("Подзадача") },
                modifier = Modifier.weight(1f),
                singleLine = true
            )
            Button(onClick = {
                if (newSubtaskText.isNotBlank()) {
                    subtasks.add(newSubtaskText.trim())
                    newSubtaskText = ""
                }
            }) {
                Text("+")
            }
        }

        subtasks.forEachIndexed { index, subtask ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("• $subtask", modifier = Modifier.weight(1f))
                IconButton(onClick = { subtasks.removeAt(index) }) {
                    Icon(Icons.Default.Delete, contentDescription = "Удалить")
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                viewModel.addTask(
                    title = title,
                    description = description,
                    category = selectedCategory,
                    startTime = startTime,
                    endTime = endTime,
                    date = selectedDate,
                    subtasks = subtasks.toList()
                )
                onTaskAdded()
            },
            enabled = title.isNotBlank() && !invalidTime
        ) {
            Text("Добавить задачу вручную")
        }
    }
}
