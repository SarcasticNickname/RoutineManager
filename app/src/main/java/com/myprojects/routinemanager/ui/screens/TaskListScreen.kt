package com.myprojects.routinemanager.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.myprojects.routinemanager.ui.viewmodel.TaskViewModel
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import com.myprojects.routinemanager.data.model.Task
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme


/**
 * Экран со списком задач.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskListScreen(
    viewModel: TaskViewModel,
    onAddTaskClick: () -> Unit,
    onTaskClick: (String) -> Unit  // Добавили колбэк при клике на задачу
) {
    val taskList by viewModel.tasks.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Менеджер рутины") })
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddTaskClick) {
                Text("+")
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            items(taskList) { task ->
                TaskItem(
                    task = task,
                    onCheck = { viewModel.toggleTaskDone(task) },
                    onDelete = { viewModel.deleteTask(task) },
                    onClick = { onTaskClick(task.id) }
                )
            }
        }
    }
}

@Composable
fun TaskItem(
    task: Task,
    onCheck: () -> Unit,
    onDelete: () -> Unit,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            // Добавили возможность кликнуть по карточке
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp)
        ) {
            Checkbox(
                checked = task.isDone,
                onCheckedChange = { onCheck() }
            )
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 8.dp)
            ) {
                Text(text = task.title, style = MaterialTheme.typography.titleMedium)
                if (!task.description.isNullOrEmpty()) {
                    Text(text = task.description, style = MaterialTheme.typography.bodyMedium)
                }
            }
            IconButton(onClick = onDelete) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete Task"
                )
            }
        }
    }
}
