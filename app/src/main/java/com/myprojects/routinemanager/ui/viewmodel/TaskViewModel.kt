package com.myprojects.routinemanager.ui.viewmodel

// Import the constant for the holder ID
import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.myprojects.routinemanager.data.model.Subtask
import com.myprojects.routinemanager.data.model.Task
import com.myprojects.routinemanager.data.model.TaskCategory
import com.myprojects.routinemanager.data.model.TaskTemplate
import com.myprojects.routinemanager.data.repository.DayTemplateRepository
import com.myprojects.routinemanager.data.repository.TaskRepository
import com.myprojects.routinemanager.data.room.STANDALONE_TASK_TEMPLATE_HOLDER_ID
import com.myprojects.routinemanager.datastore.SettingsDataStore
import com.myprojects.routinemanager.util.TaskNotificationManager
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.YearMonth
import java.util.UUID
import javax.inject.Inject

// MonthlyStats Data Class
data class MonthlyStats(
    val totalTasks: Int = 0,
    val completedTasks: Int = 0,
    val pendingTasks: Int = 0,
    val monthName: String = "",
    val isLoading: Boolean = true
)

@HiltViewModel
class TaskViewModel @Inject constructor(
    private val repository: TaskRepository,
    val dayTemplateRepository: DayTemplateRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {

    val taskRepository = repository // Expose for BackupScreen

    private val _tasks = MutableStateFlow<List<Task>>(emptyList())
    val tasks: StateFlow<List<Task>> = _tasks.asStateFlow()

    private val notificationManager = TaskNotificationManager(context)
    private val settingsDataStore = SettingsDataStore(context)

    private val notificationsEnabledState = settingsDataStore.notificationsEnabledFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)
    private val notificationOffsetState = settingsDataStore.notificationOffsetFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 5)

    private val _monthlyStats = MutableStateFlow(MonthlyStats(isLoading = true))
    val monthlyStats: StateFlow<MonthlyStats> = _monthlyStats.asStateFlow()

    init {
        viewModelScope.launch {
            // Загружаем задачи для текущей даты при инициализации
            // getTasksForDate уже должен возвращать их отсортированными по displayOrder
            repository.getTasksForDate(LocalDate.now()).collectLatest {
                _tasks.value = it
            }
        }
        calculateMonthlyStats()
    }

    fun calculateMonthlyStats() {
        viewModelScope.launch {
            _monthlyStats.value = MonthlyStats(isLoading = true)
            try {
                val allTasks = repository.getAllTasks().first() // Получаем все задачи один раз
                val currentMonth = YearMonth.now()
                val firstDayOfMonth = currentMonth.atDay(1)
                val lastDayOfMonth = currentMonth.atEndOfMonth()
                val monthName = currentMonth.month.getDisplayName(
                    java.time.format.TextStyle.FULL_STANDALONE, java.util.Locale("ru")
                ).replaceFirstChar { it.uppercase() }

                val tasksThisMonth = allTasks.filter { task ->
                    task.date != null && !task.date.isBefore(firstDayOfMonth) && !task.date.isAfter(
                        lastDayOfMonth
                    )
                }
                val total = tasksThisMonth.size
                val completed = tasksThisMonth.count { it.isDone }
                _monthlyStats.value = MonthlyStats(
                    totalTasks = total,
                    completedTasks = completed,
                    pendingTasks = total - completed,
                    monthName = monthName,
                    isLoading = false
                )
            } catch (e: Exception) {
                _monthlyStats.value = MonthlyStats(isLoading = false)
                Log.e("TaskViewModel", "Error calculating monthly stats", e)
            }
        }
    }

    // Вспомогательная функция для получения следующего displayOrder для указанной даты
    private fun getNextDisplayOrderForDate(date: LocalDate): Int {
        val tasksForDate = _tasks.value.filter { it.date == date }
        return tasksForDate.maxOfOrNull { it.displayOrder }?.plus(1) ?: 0
    }

    fun addTask(
        title: String, description: String?, category: TaskCategory,
        startTime: LocalTime?, endTime: LocalTime?, date: LocalDate,
        subtasks: List<String>
    ) {
        viewModelScope.launch {
            val trimmedTitle = title.trim()
            if (trimmedTitle.isBlank()) return@launch

            val taskId = UUID.randomUUID().toString()
            val newDisplayOrder = getNextDisplayOrderForDate(date)

            val newTask = Task(
                id = taskId, title = trimmedTitle, description = description, isDone = false,
                category = category, startTime = startTime, endTime = endTime, date = date,
                subtasks = subtasks.map { Subtask(it) },
                displayOrder = newDisplayOrder
            )
            repository.insertTask(newTask)
            if (YearMonth.from(date) == YearMonth.now()) calculateMonthlyStats()

            if (notificationsEnabledState.value) {
                val offset = notificationOffsetState.value.toLong()
                if (startTime != null) scheduleNotificationWithCheck(
                    taskId,
                    trimmedTitle, // Используем trimmedTitle
                    description,
                    false,
                    LocalDateTime.of(date, startTime).minusMinutes(offset)
                )
                if (endTime != null) scheduleNotificationWithCheck(
                    taskId,
                    trimmedTitle, // Используем trimmedTitle
                    description,
                    true,
                    LocalDateTime.of(date, endTime).minusMinutes(offset)
                )
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
            val date = LocalDate.now()
            val newDisplayOrder = getNextDisplayOrderForDate(date)

            val newTask = Task(
                id = UUID.randomUUID().toString(),
                title = template.defaultTitle, description = template.defaultDescription,
                isDone = false, category = template.category,
                startTime = template.defaultStartTime, endTime = template.defaultEndTime,
                date = date, subtasks = template.subtasks,
                displayOrder = newDisplayOrder
            )
            repository.insertTask(newTask)
            calculateMonthlyStats()

            if (notificationsEnabledState.value) {
                val offset = notificationOffsetState.value.toLong()
                if (newTask.startTime != null) scheduleNotificationWithCheck(
                    newTask.id,
                    newTask.title,
                    newTask.description,
                    false,
                    LocalDateTime.of(date, newTask.startTime).minusMinutes(offset)
                )
                if (newTask.endTime != null) scheduleNotificationWithCheck(
                    newTask.id,
                    newTask.title,
                    newTask.description,
                    true,
                    LocalDateTime.of(date, newTask.endTime).minusMinutes(offset)
                )
            }
        }
    }

    fun quickAddTask(title: String, date: LocalDate) {
        viewModelScope.launch {
            val trimmedTitle = title.trim()
            if (trimmedTitle.isBlank()) {
                return@launch
            }
            val taskId = UUID.randomUUID().toString()
            val newDisplayOrder = getNextDisplayOrderForDate(date)

            val newTask = Task(
                id = taskId,
                title = trimmedTitle,
                description = null,
                isDone = false,
                category = TaskCategory.OTHER,
                startTime = null,
                endTime = null,
                date = date,
                subtasks = emptyList(),
                displayOrder = newDisplayOrder
            )
            repository.insertTask(newTask)
            if (YearMonth.from(date) == YearMonth.now()) {
                calculateMonthlyStats()
            }
        }
    }

    fun toggleTaskDone(task: Task) {
        viewModelScope.launch {
            val updatedTask = task.copy(isDone = !task.isDone)
            repository.updateTask(updatedTask)
            if (task.date != null && YearMonth.from(task.date) == YearMonth.now()) calculateMonthlyStats()

            if (updatedTask.isDone) {
                cancelNotificationWithCheck(task.id, false)
                cancelNotificationWithCheck(task.id, true)
            } else if (notificationsEnabledState.value && updatedTask.date != null) {
                val offset = notificationOffsetState.value.toLong()
                if (updatedTask.startTime != null) scheduleNotificationWithCheck(
                    updatedTask.id,
                    updatedTask.title,
                    updatedTask.description,
                    false,
                    LocalDateTime.of(updatedTask.date, updatedTask.startTime).minusMinutes(offset)
                )
                if (updatedTask.endTime != null) scheduleNotificationWithCheck(
                    updatedTask.id,
                    updatedTask.title,
                    updatedTask.description,
                    true,
                    LocalDateTime.of(updatedTask.date, updatedTask.endTime).minusMinutes(offset)
                )
            }
        }
    }

    fun deleteTask(task: Task) {
        viewModelScope.launch {
            repository.deleteTask(task)
            if (task.date != null && YearMonth.from(task.date) == YearMonth.now()) calculateMonthlyStats()
            cancelNotificationWithCheck(task.id, false)
            cancelNotificationWithCheck(task.id, true)
            // После удаления, хорошо бы обновить displayOrder у оставшихся задач для этой даты,
            // чтобы не было "дыр", но это усложнение. Пока оставим так.
            // Если это важно, то после deleteTask нужно будет вызвать loadTasksFor(task.date)
            // и затем пересчитать и обновить displayOrder для _tasks.value.
        }
    }

    fun updateTask(updated: Task) {
        viewModelScope.launch {
            repository.updateTask(updated) // displayOrder должен быть частью объекта updated
            if (_tasks.value.any { it.date == updated.date }) {
                loadTasksFor(updated.date) // Перезагрузит и отсортирует
            }
            if (updated.date != null && YearMonth.from(updated.date) == YearMonth.now()) calculateMonthlyStats()

            cancelNotificationWithCheck(updated.id, false)
            cancelNotificationWithCheck(updated.id, true)
            if (!updated.isDone && notificationsEnabledState.value && updated.date != null) {
                val offset = notificationOffsetState.value.toLong()
                if (updated.startTime != null) scheduleNotificationWithCheck(
                    updated.id,
                    updated.title,
                    updated.description,
                    false,
                    LocalDateTime.of(updated.date, updated.startTime).minusMinutes(offset)
                )
                if (updated.endTime != null) scheduleNotificationWithCheck(
                    updated.id,
                    updated.title,
                    updated.description,
                    true,
                    LocalDateTime.of(updated.date, updated.endTime).minusMinutes(offset)
                )
            }
        }
    }

    fun updateTaskOrder(tasksWithNewOrder: List<Task>) {
        viewModelScope.launch {
            if (tasksWithNewOrder.isNotEmpty()) {
                repository.updateTasks(tasksWithNewOrder) // Метод для батчевого обновления
                // _tasks.value обновится через collectLatest в loadTasksFor,
                // если текущая дата совпадает, или при следующем вызове loadTasksFor.
                // Для немедленного отклика UI, если это та же дата, что и в _tasks:
                val currentDate = tasksWithNewOrder.first().date
                if (_tasks.value.isNotEmpty() && _tasks.value.first().date == currentDate) {
                    _tasks.value = tasksWithNewOrder.sortedBy { it.displayOrder }
                }
            }
        }
    }

    private fun scheduleNotificationWithCheck(
        taskId: String,
        taskTitle: String,
        taskDescription: String?,
        isEndTimeNotification: Boolean,
        time: LocalDateTime
    ) {
        if (time.isAfter(LocalDateTime.now())) {
            try {
                notificationManager.scheduleNotification(
                    taskId,
                    taskTitle,
                    taskDescription,
                    isEndTimeNotification,
                    time
                )
            } catch (e: SecurityException) {
                Log.e("TaskViewModel", "Permission error scheduling notification", e)
            } catch (e: Exception) {
                Log.e("TaskViewModel", "Error scheduling notification", e)
            }
        } else {
            Log.w("TaskViewModel", "Skipped scheduling notification, time in past: $time")
        }
    }

    private fun cancelNotificationWithCheck(taskId: String, isEndTimeNotification: Boolean) {
        try {
            notificationManager.cancelNotification(taskId, isEndTimeNotification)
        } catch (e: Exception) {
            Log.e("TaskViewModel", "Error cancelling notification", e)
        }
    }

    fun getTaskById(taskId: String): Task? = _tasks.value.find { it.id == taskId }

    fun createStandaloneTaskTemplate(
        title: String, description: String?, category: TaskCategory,
        startTime: LocalTime?, endTime: LocalTime?, subtasks: List<Subtask>
    ) {
        viewModelScope.launch {
            val newTaskTemplate = TaskTemplate(
                id = UUID.randomUUID().toString(),
                templateId = STANDALONE_TASK_TEMPLATE_HOLDER_ID,
                defaultTitle = title, defaultDescription = description,
                defaultStartTime = startTime, defaultEndTime = endTime,
                category = category, subtasks = subtasks
            )
            try {
                dayTemplateRepository.insertTaskTemplate(newTaskTemplate)
            } catch (e: Exception) {
                Log.e("TaskViewModel", "Error creating standalone task template via repository", e)
            }
        }
    }

    fun addTaskTemplateToTemplate(
        templateId: String, title: String, description: String?,
        category: TaskCategory, startTime: LocalTime?, endTime: LocalTime?,
        subtasks: List<String>
    ) {
        viewModelScope.launch {
            val taskTemplate = TaskTemplate(
                id = UUID.randomUUID().toString(),
                templateId = templateId,
                defaultTitle = title, defaultDescription = description, category = category,
                defaultStartTime = startTime, defaultEndTime = endTime,
                subtasks = subtasks.map { Subtask(it) }
            )
            try {
                dayTemplateRepository.insertTaskTemplate(taskTemplate)
            } catch (e: Exception) {
                Log.e(
                    "TaskViewModel",
                    "Error adding task template to day template via repository",
                    e
                )
            }
        }
    }

    fun addTaskTemplateToTemplate(templateId: String, template: TaskTemplate) {
        viewModelScope.launch {
            try {
                dayTemplateRepository.insertTaskTemplate(template.copy(templateId = templateId))
            } catch (e: Exception) {
                Log.e(
                    "TaskViewModel",
                    "Error adding task template object to day template via repository",
                    e
                )
            }
        }
    }

    fun deleteAllTasksForDate(date: LocalDate) {
        viewModelScope.launch {
            val tasksToDelete = repository.getTasksForDate(date).first()
            tasksToDelete.forEach { task ->
                cancelNotificationWithCheck(task.id, false)
                cancelNotificationWithCheck(task.id, true)
            }
            repository.deleteAllTasksForDate(date)
            if (YearMonth.from(date) == YearMonth.now()) calculateMonthlyStats()
            // После удаления всех задач для даты, _tasks.value должен стать пустым
            // если это была текущая отображаемая дата
            if (_tasks.value.isNotEmpty() && _tasks.value.firstOrNull()?.date == date) {
                _tasks.value = emptyList()
            }
        }
    }

    fun toggleSubtask(task: Task, subtaskIndex: Int) {
        viewModelScope.launch {
            if (subtaskIndex < 0 || subtaskIndex >= task.subtasks.size) return@launch
            val updatedSubtasks = task.subtasks.mapIndexed { index, subtask ->
                if (index == subtaskIndex) subtask.copy(isDone = !subtask.isDone) else subtask
            }
            val updatedTask = task.copy(subtasks = updatedSubtasks)
            updateTask(updatedTask)
        }
    }
}