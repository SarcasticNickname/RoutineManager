package com.myprojects.routinemanager.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.myprojects.routinemanager.data.model.Subtask // Keep if needed by formatTimeRangeInternal
import com.myprojects.routinemanager.data.model.TaskTemplate
import com.myprojects.routinemanager.ui.viewmodel.DayTemplateViewModel
import com.myprojects.routinemanager.ui.viewmodel.TaskViewModel
import java.time.format.DateTimeFormatter

@Composable
fun AddTaskFromTemplateScreen(
    taskViewModel: TaskViewModel, // Needed to add task/template
    dayTemplateViewModel: DayTemplateViewModel, // Needed to get template list
    onTaskAdded: () -> Unit,
    templateId: String? = null // ID of DayTemplate to add to (if not null)
) {
    // Collect standalone task templates directly from the StateFlow
    val templates by dayTemplateViewModel.standaloneTaskTemplates.collectAsState()

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(bottom = 16.dp)
    ) {
        item {
            Text("Выберите шаблон", style = MaterialTheme.typography.titleLarge)
        }

        items(templates, key = { it.id }) { template ->
            TemplateItem(template = template) {
                if (templateId == null) {
                    // Create a regular task from this TaskTemplate
                    taskViewModel.addTaskFromTemplate(template)
                } else {
                    // Add this existing TaskTemplate to the specified DayTemplate
                    taskViewModel.addTaskTemplateToTemplate(templateId, template)
                }
                onTaskAdded() // Go back
            }
        }
    }
}

// TemplateItem Composable
@Composable
fun TemplateItem(
    template: TaskTemplate,
    onTemplateClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(text = template.defaultTitle, style = MaterialTheme.typography.titleMedium)
                if (template.defaultStartTime != null || template.defaultEndTime != null) {
                    val timeString = formatTimeRangeInternal(template.defaultStartTime, template.defaultEndTime) // Use internal helper
                    Text(timeString, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.secondary)
                }
                if (!template.defaultDescription.isNullOrBlank()){
                    Text(
                        template.defaultDescription,
                        style = MaterialTheme.typography.bodySmall,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                if (template.subtasks.isNotEmpty()) {
                    Text("Подзадач: ${template.subtasks.size}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.outline)
                }
            }
            Spacer(Modifier.width(8.dp))
            Button(onClick = onTemplateClick) {
                Text("Добавить")
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