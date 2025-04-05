package com.myprojects.routinemanager.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.myprojects.routinemanager.data.model.DayTemplate
import com.myprojects.routinemanager.data.model.TaskTemplate
import com.myprojects.routinemanager.ui.viewmodel.DayTemplateViewModel
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditDayTemplateScreen(
    viewModel: DayTemplateViewModel,
    initialTemplate: DayTemplate?,
    onSave: () -> Unit
) {
    val isWeekly = initialTemplate?.isWeekly ?: false
    val weekday = initialTemplate?.weekday

    var name by remember { mutableStateOf(initialTemplate?.name ?: "") }
    val tasks = remember { mutableStateListOf<TaskTemplate>().apply { initialTemplate?.taskTemplates?.let { addAll(it) } } }

    var newTaskTitle by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text(if (initialTemplate == null) "Создание шаблона" else "Редактирование шаблона") })
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    val updated = DayTemplate(
                        id = initialTemplate?.id ?: UUID.randomUUID().toString(),
                        name = name,
                        isWeekly = isWeekly,
                        weekday = weekday,
                        taskTemplates = tasks.toList()
                    )
                    viewModel.addTemplate(updated)
                    onSave()
                },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Text("✓")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
        ) {
            if (!isWeekly) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Название шаблона") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))
            } else {
                Text("Шаблон для дня недели: ${weekday?.name ?: ""}", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(12.dp))
            }

            Text("Задачи", style = MaterialTheme.typography.titleMedium)

            LazyColumn {
                itemsIndexed(tasks) { index, task ->
                    OutlinedCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .padding(12.dp)
                                .fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(task.defaultTitle, style = MaterialTheme.typography.bodyLarge)
                                task.defaultStartTime?.let {
                                    Text("Начало: $it", style = MaterialTheme.typography.bodySmall)
                                }
                            }
                            IconButton(onClick = { tasks.removeAt(index) }) {
                                Icon(Icons.Default.Delete, contentDescription = "Удалить задачу")
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = newTaskTitle,
                onValueChange = { newTaskTitle = it },
                label = { Text("Новая задача") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = {
                    if (newTaskTitle.isNotBlank()) {
                        tasks.add(TaskTemplate(defaultTitle = newTaskTitle))
                        newTaskTitle = ""
                    }
                },
                modifier = Modifier.align(Alignment.End)
            ) {
                Text("Добавить")
            }
        }
    }
}
