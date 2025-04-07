package com.myprojects.routinemanager.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.myprojects.routinemanager.data.model.TaskTemplate
import com.myprojects.routinemanager.ui.viewmodel.TaskViewModel

@Composable
fun AddTaskFromTemplateScreen(
    viewModel: TaskViewModel,
    onTaskAdded: () -> Unit,
    templateId: String? = null
) {
    val templates by viewModel.templates.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Выберите шаблон", style = MaterialTheme.typography.titleLarge)

        templates.forEach { template ->
            TemplateItem(template = template) {
                if (templateId == null) {
                    viewModel.addTaskFromTemplate(template)
                } else {
                    viewModel.addTaskTemplateToTemplate(templateId, template)
                }
                onTaskAdded()
            }
        }
    }
}

@Composable
fun TemplateItem(
    template: TaskTemplate,
    onTemplateClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(modifier = Modifier.padding(16.dp)) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = template.defaultTitle, style = MaterialTheme.typography.titleMedium)
                template.defaultStartTime?.let {
                    Text("Начало: $it", style = MaterialTheme.typography.bodySmall)
                }
                template.defaultDescription?.let {
                    Text(it, style = MaterialTheme.typography.bodySmall)
                }
            }
            Button(onClick = onTemplateClick) {
                Text("Добавить")
            }
        }
    }
}
