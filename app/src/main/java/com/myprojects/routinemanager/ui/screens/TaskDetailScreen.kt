package com.myprojects.routinemanager.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.myprojects.routinemanager.ui.viewmodel.TaskViewModel
import com.myprojects.routinemanager.data.model.Task

/**
 * Экран деталей задачи.
 * Вызывается при нажатии на задачу в списке.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskDetailScreen(
    viewModel: TaskViewModel,
    taskId: String,
    onBack: () -> Unit
) {
    // Получаем список всех задач
    val taskList by viewModel.tasks.collectAsState()
    // Ищем нужную задачу по ID (в реальном проекте ViewModel может иметь метод getTaskById)
    val currentTask = taskList.find { it.id == taskId }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Детали задачи") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        }
    ) { padding ->
        if (currentTask == null) {
            // Если задача не найдена
            Box(modifier = Modifier.padding(padding).fillMaxSize()) {
                Text("Задача не найдена")
            }
        } else {
            DetailContent(
                task = currentTask,
                onSave = { updated ->
                    viewModel.updateTask(updated) // Сохраняем изменения
                    onBack()
                },
                onBack = onBack,
                modifier = Modifier.padding(padding).fillMaxSize()
            )
        }
    }
}

@Composable
private fun DetailContent(
    task: Task,
    onSave: (Task) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var title by remember { mutableStateOf(task.title) }
    var description by remember { mutableStateOf(task.description ?: "") }

    Column(
        modifier = modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        TextField(
            value = title,
            onValueChange = { title = it },
            label = { Text("Название") },
            modifier = Modifier.fillMaxWidth()
        )
        TextField(
            value = description,
            onValueChange = { description = it },
            label = { Text("Описание") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = {
                    val updatedTask = task.copy(title = title, description = description)
                    onSave(updatedTask)
                }
            ) {
                Text("Сохранить")
            }
            OutlinedButton(onClick = onBack) {
                Text("Отмена")
            }
        }
    }
}
