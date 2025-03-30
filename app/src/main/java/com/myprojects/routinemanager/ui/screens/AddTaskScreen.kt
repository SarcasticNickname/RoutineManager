package com.myprojects.routinemanager.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.myprojects.routinemanager.data.model.TaskTemplate
import com.myprojects.routinemanager.ui.viewmodel.TaskViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTaskScreen(
    viewModel: TaskViewModel,
    onTaskAdded: () -> Unit
) {
    val templates by viewModel.templates.collectAsState()

    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Добавить задачу") })
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Ввод произвольной задачи
            TextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Название задачи") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            TextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Описание задачи") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = {
                    viewModel.addTask(title, description.ifBlank { null })
                    onTaskAdded()
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Добавить задачу вручную")
            }

            Divider(modifier = Modifier.padding(vertical = 16.dp))

            // Список доступных шаблонов
            Text("Или выбрать шаблон:")
            templates.forEach { template ->
                TemplateItem(template = template) {
                    viewModel.addTaskFromTemplate(template)
                    onTaskAdded()
                }
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
