package com.myprojects.routinemanager.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.myprojects.routinemanager.data.model.Subtask
import com.myprojects.routinemanager.data.model.Task
import com.myprojects.routinemanager.data.model.TaskCategory
import com.myprojects.routinemanager.data.model.TaskTemplate
import com.myprojects.routinemanager.data.repository.TaskRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime
import javax.inject.Inject

/**
 * ViewModel хранит состояние списка задач и логику их изменения.
 * Обращается к репозиторию для выполнения операций над данными.
 */
@HiltViewModel
class TaskViewModel @Inject constructor(
    private val repository: TaskRepository
) : ViewModel() {

    private val _tasks = MutableStateFlow<List<Task>>(emptyList())
    val tasks: StateFlow<List<Task>> = _tasks.asStateFlow()

    private val _templates = MutableStateFlow<List<TaskTemplate>>(emptyList())
    val templates: StateFlow<List<TaskTemplate>> = _templates.asStateFlow()

    init {
        viewModelScope.launch {
            repository.getAllTasks().collectLatest { taskList ->
                _tasks.value = taskList
            }
        }
        _templates.value = repository.getAllTemplates()
    }

    fun addTask(
        title: String,
        description: String?,
        category: TaskCategory,
        startTime: LocalTime?,
        endTime: LocalTime?,
        date: LocalDate,
        subtasks: List<String>
    ) {
        viewModelScope.launch {
            repository.addTask(
                title = title,
                description = description,
                category = category,
                startTime = startTime,
                endTime = endTime,
                date = date,
                subtasks = subtasks.map { Subtask(it) }
            )
        }
    }

    fun loadTasksFor(date: LocalDate) {
        viewModelScope.launch {
            repository.getTasksForDate(date).collectLatest {
                _tasks.value = it
            }
        }
    }

    fun addTaskFromTemplate(template: TaskTemplate) {
        viewModelScope.launch {
            repository.addTaskFromTemplate(template)
        }
    }

    fun toggleTaskDone(task: Task) {
        viewModelScope.launch {
            val updatedTask = task.copy(isDone = !task.isDone)
            repository.updateTask(updatedTask)
        }
    }

    fun deleteTask(task: Task) {
        viewModelScope.launch {
            repository.deleteTask(task)
        }
    }

    fun updateTask(updated: Task) {
        viewModelScope.launch {
            repository.updateTask(updated)
        }
    }

    fun getAllTemplates(): List<TaskTemplate> = templates.value
}
