package com.myprojects.routinemanager.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.myprojects.routinemanager.ui.viewmodel.DayTemplateViewModel // Import DayTemplateViewModel
import com.myprojects.routinemanager.ui.viewmodel.TaskViewModel

@Composable
fun AddTaskRootScreen(
    taskViewModel: TaskViewModel,
    dayTemplateViewModel: DayTemplateViewModel, // Add DayTemplateViewModel parameter
    onTaskAdded: () -> Unit,
    templateId: String? = null // ID of the DayTemplate we are adding to
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
                // Navigating to AddTaskScreen to create a new task (potentially linked to templateId)
                AddTaskTab.NEW -> AddTaskScreen(
                    viewModel = taskViewModel,
                    onTaskAdded = onTaskAdded,
                    taskId = null, // Not editing
                    templateId = templateId, // Pass DayTemplate ID if available
                    isCreatingTemplate = false // Not creating a standalone template
                )
                // Navigating to AddTaskFromTemplateScreen to pick a template to add
                AddTaskTab.TEMPLATE -> AddTaskFromTemplateScreen(
                    taskViewModel = taskViewModel,
                    dayTemplateViewModel = dayTemplateViewModel, // Pass both ViewModels
                    onTaskAdded = onTaskAdded,
                    templateId = templateId // Pass DayTemplate ID if available
                )
            }
        }
    }
}

enum class AddTaskTab { NEW, TEMPLATE }