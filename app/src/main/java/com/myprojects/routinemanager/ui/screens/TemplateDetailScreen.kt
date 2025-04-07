package com.myprojects.routinemanager.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.myprojects.routinemanager.data.model.DayTemplate
import com.myprojects.routinemanager.data.model.TaskTemplate
import com.myprojects.routinemanager.data.model.getCategoryColor
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TemplateDetailScreen(
    navController: NavController,
    template: DayTemplate,
    onAddTaskClick: () -> Unit,
    onEditTaskTemplate: (TaskTemplate) -> Unit,
    onDeleteTaskTemplate: (TaskTemplate) -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Шаблон: ${template.name}") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddTaskClick) {
                Text("+")
            }
        }
    ) { padding ->
        LazyColumn(
            contentPadding = padding,
            modifier = Modifier.fillMaxSize()
        ) {
            items(template.taskTemplates) { taskTemplate ->
                TaskTemplateItem(
                    task = taskTemplate,
                    onEdit = { onEditTaskTemplate(taskTemplate) },
                    onDelete = { onDeleteTaskTemplate(taskTemplate) }
                )
            }
        }
    }
}


@Composable
fun TaskTemplateItem(
    task: TaskTemplate,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val categoryColor = getCategoryColor(task.category)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column {
            Row(modifier = Modifier.height(IntrinsicSize.Min)) {
                Box(
                    modifier = Modifier
                        .width(6.dp)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(topStart = 12.dp, bottomStart = 12.dp))
                        .background(categoryColor)
                )

                Column(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(task.defaultTitle, style = MaterialTheme.typography.titleMedium)

                            if (!task.defaultDescription.isNullOrEmpty()) {
                                Text(
                                    task.defaultDescription,
                                    style = MaterialTheme.typography.bodyMedium,
                                    modifier = Modifier.padding(top = 4.dp)
                                )
                            }

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(top = 4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Schedule,
                                    contentDescription = "Время",
                                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = formatTimeRange(task.defaultStartTime, task.defaultEndTime),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                )
                            }
                        }

                        IconButton(onClick = { onDelete() }) {
                            Icon(Icons.Default.Delete, contentDescription = "Удалить задачу")
                        }
                    }

                    if (task.subtasks.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Column {
                            task.subtasks.forEach { subtask ->
                                Text("• ${subtask.title}", style = MaterialTheme.typography.bodyMedium)
                            }
                        }
                    }
                }
            }
        }
    }
}
