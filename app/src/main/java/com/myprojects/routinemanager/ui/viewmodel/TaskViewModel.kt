package com.myprojects.routinemanager.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.myprojects.routinemanager.data.model.*
import com.myprojects.routinemanager.data.repository.TaskRepository
import com.myprojects.routinemanager.data.repository.DayTemplateRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class TaskViewModel @Inject constructor(
    private val repository: TaskRepository,
    private val dayTemplateRepository: DayTemplateRepository
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

    // Метод для получения задачи по её идентификатору.
    fun getTaskById(taskId: String): Task? {
        return _tasks.value.find { it.id == taskId }
    }

    // Новый метод: добавление задачи в шаблон (через параметры)
    fun addTaskTemplateToTemplate(
        templateId: String,
        title: String,
        description: String?,
        category: TaskCategory,
        startTime: LocalTime?,
        endTime: LocalTime?,
        subtasks: List<String>
    ) {
        val taskTemplate = TaskTemplate(
            id = UUID.randomUUID().toString(),
            defaultTitle = title,
            defaultDescription = description,
            category = category,
            defaultStartTime = startTime,
            defaultEndTime = endTime,
            subtasks = subtasks.map { Subtask(it) },
            templateId = templateId
        )
        addTaskTemplateToTemplate(templateId, taskTemplate)
    }

    // Новый метод: добавление задачи в шаблон (из существующего TaskTemplate)
    fun addTaskTemplateToTemplate(
        templateId: String,
        template: TaskTemplate
    ) {
        viewModelScope.launch {
            dayTemplateRepository.addTaskTemplateToTemplate(templateId, template)
        }
    }

    // Новый метод: удаление всех задач за выбранную дату
    fun deleteAllTasksForDate(date: LocalDate) {
        viewModelScope.launch {
            repository.deleteAllTasksForDate(date)
        }
    }
}
