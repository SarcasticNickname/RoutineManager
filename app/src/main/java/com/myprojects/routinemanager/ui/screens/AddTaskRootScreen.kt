package com.myprojects.routinemanager.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.List
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.myprojects.routinemanager.ui.viewmodel.TaskViewModel

@Composable
fun AddTaskRootScreen(
    viewModel: TaskViewModel,
    onTaskAdded: () -> Unit
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
                AddTaskTab.NEW -> AddTaskScreen(viewModel, onTaskAdded)
                AddTaskTab.TEMPLATE -> AddTaskFromTemplateScreen(viewModel, onTaskAdded)
            }
        }
    }
}

enum class AddTaskTab { NEW, TEMPLATE }
