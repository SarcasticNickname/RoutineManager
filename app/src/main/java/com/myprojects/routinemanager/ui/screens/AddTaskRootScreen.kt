package com.myprojects.routinemanager.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import com.myprojects.routinemanager.ui.viewmodel.DayTemplateViewModel
import com.myprojects.routinemanager.ui.viewmodel.TaskViewModel
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTaskRootScreen(
    navController: NavController,
    taskViewModel: TaskViewModel,
    dayTemplateViewModel: DayTemplateViewModel,
    selectedDate: LocalDate,
    // ID шаблона дня, если мы добавляем задачу в него
    // (пока не используется, но оставим для будущего)
    dayTemplateId: String? = null
) {
    var selectedTab by remember { mutableStateOf(AddTaskTab.NEW) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Добавить задачу") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Назад")
                    }
                }
            )
        },
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = selectedTab == AddTaskTab.NEW,
                    onClick = { selectedTab = AddTaskTab.NEW },
                    icon = { Icon(Icons.Default.Add, contentDescription = null) },
                    label = { Text("Новая") }
                )
                NavigationBarItem(
                    selected = selectedTab == AddTaskTab.TEMPLATE,
                    onClick = { selectedTab = AddTaskTab.TEMPLATE },
                    icon = { Icon(Icons.AutoMirrored.Filled.List, contentDescription = null) },
                    label = { Text("Из шаблона") }
                )
            }
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding)) {
            when (selectedTab) {
                AddTaskTab.NEW -> {
                    // Показываем контент для создания новой задачи
                    // Передаем ему navController, чтобы он мог сам вернуться назад после сохранения
                    CreateTaskScreen(
                        viewModel = taskViewModel,
                        selectedDate = selectedDate,
                        onNavigateBack = { navController.popBackStack() }
                    )
                }

                AddTaskTab.TEMPLATE -> {
                    // Показываем контент для выбора из шаблона
                    AddTaskFromTemplateScreen(
                        taskViewModel = taskViewModel,
                        dayTemplateViewModel = dayTemplateViewModel,
                        onTaskAdded = { navController.popBackStack() },
                        templateId = dayTemplateId
                    )
                }
            }
        }
    }
}

enum class AddTaskTab { NEW, TEMPLATE }