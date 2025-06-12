package com.myprojects.routinemanager.ui.screens

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.myprojects.routinemanager.data.model.TaskTemplate
import com.myprojects.routinemanager.data.model.getCategoryColor
import com.myprojects.routinemanager.ui.screens.components.formatTimeRange
import com.myprojects.routinemanager.ui.viewmodel.DayTemplateViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TemplateDetailScreen(
    templateId: String,
    viewModel: DayTemplateViewModel,
    navController: NavController,
    onBack: () -> Unit
) {
    LaunchedEffect(templateId) {
        Log.d("TemplateDetailScreen", "Открыт шаблон с id: $templateId")
    }

    val templateWithTasks by viewModel.getTemplateById(templateId).collectAsState(initial = null)

    if (templateWithTasks == null) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
        return
    }

    val template = templateWithTasks!!.template
    val taskTemplates = templateWithTasks!!.taskTemplates

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Шаблон: ${template.name}") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = {
                navController.navigate("add_task_to_template/${templateId}")
            }) {
                Text("+")
            }
        }
    ) { padding ->
        LazyColumn(
            contentPadding = padding,
            modifier = Modifier.fillMaxSize()
        ) {
            items(taskTemplates) { taskTemplate ->
                TaskTemplateItem(
                    task = taskTemplate,
                    onEdit = {
                        // TODO: реализовать редактирование задачи шаблона
                    },
                    onDelete = {
                        viewModel.deleteTaskTemplate(taskTemplate)
                    }
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
            .padding(horizontal = 12.dp, vertical = 4.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            verticalAlignment = Alignment.Top
        ) {
            Box(
                modifier = Modifier
                    .width(5.dp)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(8.dp))
                    .background(categoryColor)
            )

            Spacer(modifier = Modifier.width(8.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    task.defaultTitle,
                    style = MaterialTheme.typography.titleMedium
                )

                if (!task.defaultDescription.isNullOrEmpty()) {
                    Text(
                        task.defaultDescription,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }

                task.defaultStartTime?.let {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(top = 2.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Schedule,
                            contentDescription = "Время",
                            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = formatTimeRange(task.defaultStartTime, task.defaultEndTime),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                }

                if (task.subtasks.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        task.subtasks.forEach { subtask ->
                            Text("• ${subtask.title}", style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }

            IconButton(
                onClick = onDelete,
                modifier = Modifier
                    .padding(start = 4.dp)
                    .size(32.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Удалить",
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}
