package com.myprojects.routinemanager.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.myprojects.routinemanager.data.model.TaskCategory
import com.myprojects.routinemanager.ui.screens.components.TaskFormContent
import com.myprojects.routinemanager.ui.viewmodel.TaskViewModel
import java.time.LocalDate
import java.time.LocalTime

/**
 * Composable-функция, содержащая ТОЛЬКО контент для создания новой задачи.
 * Не содержит Scaffold или TopAppBar, предназначена для встраивания в другие экраны.
 */
@Composable
fun CreateTaskScreen(
    modifier: Modifier = Modifier, // Добавляем modifier для гибкости
    viewModel: TaskViewModel,
    selectedDate: LocalDate,
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

    // Основная колонка с формой и кнопкой
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(bottom = 16.dp) // Отступ снизу для кнопки
    ) {
        // "Глупый" компонент с UI формы, занимает все доступное место
        TaskFormContent(
            modifier = Modifier.weight(1f),
            headerText = "Новая задача",
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

        // Кнопка сохранения вынесена из формы и находится внизу
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = {
                viewModel.addTask(
                    title = title.trim(),
                    description = description.trim().ifEmpty { null },
                    category = selectedCategory,
                    startTime = startTime,
                    endTime = endTime,
                    date = selectedDate,
                    subtasks = subtasks.toList()
                )
                onNavigateBack()
            },
            enabled = isFormValid,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        ) {
            Text("Добавить задачу")
        }
    }
}