package com.myprojects.routinemanager.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.myprojects.routinemanager.data.model.TaskTemplate
import com.myprojects.routinemanager.ui.viewmodel.TaskViewModel

@Composable
fun AddTaskFromTemplateScreen(
    viewModel: TaskViewModel,
    onTaskAdded: () -> Unit
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
                viewModel.addTaskFromTemplate(template)
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
                Text(text = template.name, style = MaterialTheme.typography.titleMedium)
                Text(
                    text = "По умолчанию: ${template.defaultTitle}",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            Button(onClick = onTemplateClick) {
                Text("Добавить")
            }
        }
    }
}