package com.myprojects.routinemanager.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.myprojects.routinemanager.data.model.Task
import com.myprojects.routinemanager.data.model.TaskTemplate
import com.myprojects.routinemanager.data.repository.TaskRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

/**
 * ViewModel хранит состояние списка задач и логику их изменения.
 * Обращается к репозиторию для выполнения операций над данными.
 */
class TaskViewModel(
    private val repository: TaskRepository
) : ViewModel() {

    private val _tasks = MutableStateFlow<List<Task>>(emptyList())
    val tasks: StateFlow<List<Task>> = _tasks.asStateFlow()

    private val _templates = MutableStateFlow<List<TaskTemplate>>(emptyList())
    val templates: StateFlow<List<TaskTemplate>> = _templates.asStateFlow()

    init {
        viewModelScope.launch {
            // Собираем поток задач из репозитория
            repository.getAllTasks().collectLatest { taskList ->
                _tasks.value = taskList
            }
        }
        // Шаблоны не меняются, поэтому читаем их один раз
        _templates.value = repository.getAllTemplates()
    }

    fun addTask(title: String, description: String?) {
        viewModelScope.launch {
            repository.addTask(title, description)
        }
    }

    fun addTaskFromTemplate(template: TaskTemplate) {
        viewModelScope.launch {
            repository.addTaskFromTemplate(template)
        }
    }

    fun toggleTaskDone(task: Task) {
        viewModelScope.launch {
            // Меняем значение isDone и вызываем метод обновления
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

    }
}
