package com.myprojects.routinemanager.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.myprojects.routinemanager.ui.viewmodel.TaskViewModel

@Composable
fun AddTaskRootScreen(
    viewModel: TaskViewModel,
    onTaskAdded: () -> Unit,
    templateId: String? = null
) {
    var selectedTab by remember { mutableStateOf(AddTaskTab.NEW) }

    Scaffold(
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = selectedTab == AddTaskTab.NEW,
                    onClick = { selectedTab = AddTaskTab.NEW },
                    icon = { Icon(Icons.Default.Add, contentDescription = null) },
                    label = { Text("Новое") }
                )
                NavigationBarItem(
                    selected = selectedTab == AddTaskTab.TEMPLATE,
                    onClick = { selectedTab = AddTaskTab.TEMPLATE },
                    icon = { Icon(Icons.AutoMirrored.Filled.List, contentDescription = null) },
                    label = { Text("Шаблон") }
                )
            }
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding)) {
            when (selectedTab) {
                AddTaskTab.NEW -> AddTaskScreen(viewModel, onTaskAdded, templateId)
                AddTaskTab.TEMPLATE -> AddTaskFromTemplateScreen(viewModel, onTaskAdded, templateId)
            }
        }
    }
}

enum class AddTaskTab { NEW, TEMPLATE }
