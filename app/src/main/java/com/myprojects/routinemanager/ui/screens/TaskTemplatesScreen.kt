package com.myprojects.routinemanager.ui.screens

import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.myprojects.routinemanager.data.model.TaskTemplate
import com.myprojects.routinemanager.ui.viewmodel.DayTemplateViewModel
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskTemplatesScreen(
    dayTemplateViewModel: DayTemplateViewModel, // Depends only on this ViewModel now
    navController: NavController
) {
    // Collect the standalone task templates directly from the StateFlow
    val templates by dayTemplateViewModel.standaloneTaskTemplates.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Шаблоны задач") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = {
                // Navigate to AddTaskScreen with the "new_template" marker
                navController.navigate("add_task/new_template")
            }) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Создать шаблон задачи")
            }
        }
    ) { innerPadding ->
        if (templates.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize().padding(innerPadding).padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Text("Нет шаблонов задач. Нажмите +, чтобы создать новый.", textAlign = TextAlign.Center)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(innerPadding),
                contentPadding = PaddingValues(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(templates, key = { it.id }) { template ->
                    TaskTemplateCard(
                        taskTemplate = template,
                        onClick = {
                            Log.w("TaskTemplatesScreen", "Edit template ${template.id} not implemented yet.")
                            // TODO: Implement navigation to edit TaskTemplate
                            // navController.navigate("edit_task_template/${template.id}")
                        },
                        onDelete = {
                            dayTemplateViewModel.deleteTaskTemplate(template)
                        }
                    )
                }
            }
        }
    }
}

// TaskTemplateCard Composable
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskTemplateCard(
    taskTemplate: TaskTemplate,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    val timeFormatter = remember { DateTimeFormatter.ofPattern("HH:mm") }

    OutlinedCard(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text( text = taskTemplate.defaultTitle, style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.weight(1f, fill = false) )
                IconButton(onClick = onDelete, modifier = Modifier.size(36.dp)) {
                    Icon(Icons.Default.DeleteOutline, contentDescription = "Удалить шаблон", tint = MaterialTheme.colorScheme.error)
                }
            }

            if (!taskTemplate.defaultDescription.isNullOrEmpty()) {
                Text( text = taskTemplate.defaultDescription, style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant )
            }

            // Display time range if available
            val timeString = formatTimeRangeInternal(taskTemplate.defaultStartTime, taskTemplate.defaultEndTime) // Use internal helper
            if (timeString != "Время не указано") {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Schedule, contentDescription = null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.secondary)
                    Spacer(Modifier.width(4.dp))
                    Text(text = timeString, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.secondary)
                }
            }
            // Display subtask count if any
            if (taskTemplate.subtasks.isNotEmpty()) {
                Text("Подзадач: ${taskTemplate.subtasks.size}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.outline)
            }
        }
    }
}

// Internal helper function to format time range
@Composable
private fun formatTimeRangeInternal(start: java.time.LocalTime?, end: java.time.LocalTime?): String {
    val formatter = remember { DateTimeFormatter.ofPattern("HH:mm") }
    return when {
        start != null && end != null -> "${start.format(formatter)} - ${end.format(formatter)}"
        start != null -> "с ${start.format(formatter)}"
        end != null -> "до ${end.format(formatter)}"
        else -> "Время не указано"
    }
}