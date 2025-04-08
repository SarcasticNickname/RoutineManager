package com.myprojects.routinemanager.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.myprojects.routinemanager.data.model.Subtask
import com.myprojects.routinemanager.data.model.Task
import com.myprojects.routinemanager.data.model.TaskCategory
import com.myprojects.routinemanager.data.model.TaskTemplate
import com.myprojects.routinemanager.data.repository.DayTemplateRepository
import com.myprojects.routinemanager.data.repository.TaskRepository
import com.myprojects.routinemanager.datastore.SettingsDataStore
import com.myprojects.routinemanager.util.TaskNotificationManager
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class TaskViewModel @Inject constructor(
    private val repository: TaskRepository,
    private val dayTemplateRepository: DayTemplateRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _tasks = MutableStateFlow<List<Task>>(emptyList())
    val tasks: StateFlow<List<Task>> = _tasks.asStateFlow()

    private val _templates = MutableStateFlow<List<TaskTemplate>>(emptyList())
    val templates: StateFlow<List<TaskTemplate>> = _templates.asStateFlow()

    // Создаем экземпляр менеджера уведомлений, которому передаём applicationContext
    private val notificationManager = TaskNotificationManager(context)

    // Создаем DataStore для настроек
    private val settingsDataStore = SettingsDataStore(context)

    // Состояние, включены ли уведомления (по умолчанию: true)
    private val notificationsEnabledState = settingsDataStore.notificationsEnabledFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    // Состояние выбранного интервала уведомлений (по умолчанию: 5 минут)
    private val notificationOffsetState = settingsDataStore.notificationOffsetFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 5)

    init {
        viewModelScope.launch {
            repository.getAllTasks().collectLatest { taskList ->
                _tasks.value = taskList
            }
        }
        _templates.value = repository.getAllTemplates()
    }

    /**
     * Добавление новой задачи.
     * Генерируется уникальный идентификатор, после вставки в БД планируются уведомления,
     * если уведомления включены в настройках.
     */
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
            val taskId = UUID.randomUUID().toString()
            val newTask = Task(
                id = taskId,
                title = title,
                description = description,
                isDone = false,
                category = category,
                startTime = startTime,
                endTime = endTime,
                date = date,
                subtasks = subtasks.map { Subtask(it) }
            )
            repository.insertTask(newTask)
            if (notificationsEnabledState.value) {
                val offset = notificationOffsetState.value.toLong()
                if (startTime != null) {
                    val notificationTime = LocalDateTime.of(date, startTime).minusMinutes(offset)
                    try {
                        notificationManager.scheduleNotification(taskId, notificationTime)
                    } catch (e: SecurityException) {
                        e.printStackTrace()
                    }
                }
                if (endTime != null) {
                    val notificationTime = LocalDateTime.of(date, endTime).minusMinutes(offset)
                    try {
                        notificationManager.scheduleNotification(taskId + "_end", notificationTime)
                    } catch (e: SecurityException) {
                        e.printStackTrace()
                    }
                }
            }
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

    /**
     * Переключение выполненного состояния задачи.
     * Если задача помечается выполненной, уведомления отменяются;
     * иначе – уведомления планируются, если они включены в настройках.
     */
    fun toggleTaskDone(task: Task) {
        viewModelScope.launch {
            val updatedTask = task.copy(isDone = !task.isDone)
            repository.updateTask(updatedTask)
            if (updatedTask.isDone) {
                try {
                    notificationManager.cancelNotification(task.id)
                    notificationManager.cancelNotification(task.id + "_end")
                } catch (e: SecurityException) {
                    e.printStackTrace()
                }
            } else if (notificationsEnabledState.value) {
                val offset = notificationOffsetState.value.toLong()
                if (updatedTask.startTime != null) {
                    val notificationTime = LocalDateTime.of(updatedTask.date, updatedTask.startTime)
                        .minusMinutes(offset)
                    try {
                        notificationManager.scheduleNotification(task.id, notificationTime)
                    } catch (e: SecurityException) {
                        e.printStackTrace()
                    }
                }
                if (updatedTask.endTime != null) {
                    val notificationTime = LocalDateTime.of(updatedTask.date, updatedTask.endTime)
                        .minusMinutes(offset)
                    try {
                        notificationManager.scheduleNotification(task.id + "_end", notificationTime)
                    } catch (e: SecurityException) {
                        e.printStackTrace()
                    }
                }
            }
        }
    }

    /**
     * Удаляет задачу и отменяет запланированные уведомления.
     */
    fun deleteTask(task: Task) {
        viewModelScope.launch {
            repository.deleteTask(task)
            try {
                notificationManager.cancelNotification(task.id)
                notificationManager.cancelNotification(task.id + "_end")
            } catch (e: SecurityException) {
                e.printStackTrace()
            }
        }
    }

    /**
     * Обновление задачи.
     * После обновления перезагружается список, а уведомления пересчитываются.
     */
    fun updateTask(updated: Task) {
        viewModelScope.launch {
            repository.updateTask(updated)
            loadTasksFor(updated.date)
            try {
                notificationManager.cancelNotification(updated.id)
                notificationManager.cancelNotification(updated.id + "_end")
                if (!updated.isDone && notificationsEnabledState.value) {
                    val offset = notificationOffsetState.value.toLong()
                    if (updated.startTime != null) {
                        val notificationTime =
                            LocalDateTime.of(updated.date, updated.startTime).minusMinutes(offset)
                        notificationManager.scheduleNotification(updated.id, notificationTime)
                    }
                    if (updated.endTime != null) {
                        val notificationTime =
                            LocalDateTime.of(updated.date, updated.endTime).minusMinutes(offset)
                        notificationManager.scheduleNotification(
                            updated.id + "_end",
                            notificationTime
                        )
                    }
                }
            } catch (e: SecurityException) {
                e.printStackTrace()
            }
        }
    }

    fun getAllTemplates(): List<TaskTemplate> = templates.value

    fun getTaskById(taskId: String): Task? {
        return _tasks.value.find { it.id == taskId }
    }

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

    fun addTaskTemplateToTemplate(
        templateId: String,
        template: TaskTemplate
    ) {
        viewModelScope.launch {
            dayTemplateRepository.addTaskTemplateToTemplate(templateId, template)
        }
    }

    fun deleteAllTasksForDate(date: LocalDate) {
        viewModelScope.launch {
            repository.deleteAllTasksForDate(date)
        }
    }

    /**
     * Переключает состояние подзадачи в задаче.
     */
    fun toggleSubtask(task: Task, subtaskIndex: Int) {
        viewModelScope.launch {
            val updatedSubtasks = task.subtasks.mapIndexed { index, subtask ->
                if (index == subtaskIndex) subtask.copy(isDone = !subtask.isDone)
                else subtask
            }
            val updatedTask = task.copy(subtasks = updatedSubtasks)
            updateTask(updatedTask)
        }
    }
}
