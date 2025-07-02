package com.myprojects.routinemanager.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.myprojects.routinemanager.data.model.Subtask
import com.myprojects.routinemanager.data.model.TaskCategory
import com.myprojects.routinemanager.ui.screens.components.TaskFormContent
import com.myprojects.routinemanager.ui.viewmodel.DayTemplateViewModel
import java.time.LocalTime

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateTaskTemplateScreen(
    viewModel: DayTemplateViewModel,
    onNavigateBack: () -> Unit
) {
    // Состояния для всех полей формы
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf(TaskCategory.OTHER) }
    var startTime by remember { mutableStateOf<LocalTime?>(null) }
    var endTime by remember { mutableStateOf<LocalTime?>(null) }
    val subtasks = remember { mutableStateListOf<String>() }
    var newSubtaskText by remember { mutableStateOf("") }

    val isFormValid =
        title.isNotBlank() && (startTime == null || endTime == null || !startTime!!.isAfter(endTime))

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Новый шаблон задачи") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .padding(bottom = 16.dp)
        ) {
            TaskFormContent(
                modifier = Modifier.weight(1f),
                headerText = "Создать шаблон задачи",
                title = title,
                onTitleChange = { title = it },
                description = description,
                onDescriptionChange = { description = it },
                selectedCategory = selectedCategory,
                onCategorySelected = { selectedCategory = it },
                startTime = startTime,
                onStartTimeSelected = { startTime = it },
                endTime = endTime,
                onEndTimeSelected = { endTime = it },
                subtasks = subtasks,
                newSubtaskText = newSubtaskText,
                onNewSubtaskTextChange = { newSubtaskText = it },
                onAddSubtask = {
                    if (newSubtaskText.isNotBlank()) {
                        subtasks.add(newSubtaskText.trim())
                        newSubtaskText = ""
                    }
                },
                onDeleteSubtask = { index -> subtasks.removeAt(index) }
            )

            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = {
                    viewModel.createStandaloneTaskTemplate(
                        title = title.trim(),
                        description = description.trim().ifEmpty { null },
                        category = selectedCategory,
                        startTime = startTime,
                        endTime = endTime,
                        subtasks = subtasks.map { Subtask(it) } // Преобразуем List<String> в List<Subtask>
                    )
                    onNavigateBack()
                },
                enabled = isFormValid,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                Text("Сохранить шаблон")
            }
        }
    }
}