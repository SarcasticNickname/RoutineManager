package com.myprojects.routinemanager.data.repository

import com.myprojects.routinemanager.data.model.*
import com.myprojects.routinemanager.data.room.DayTemplateDao
import com.myprojects.routinemanager.data.room.TaskDao
import kotlinx.coroutines.flow.Flow
import java.time.DayOfWeek
import java.util.UUID
import java.time.LocalDate
import java.time.LocalTime
import java.time.LocalDateTime

/**
 * Репозиторий для работы с задачами, шаблонами задач и шаблонами дней.
 */
open class TaskRepository(
    private val taskDao: TaskDao,
    private val dayTemplateDao: DayTemplateDao
) {
    // Пример одиночных шаблонов задач (устаревающее)
    private val templateList = listOf(
        TaskTemplate(
            templateId = "tpl1",
            defaultTitle = "Подъём в 7:00",
            defaultDescription = "Проверить почту, позавтракать"
        ),
        TaskTemplate(
            templateId = "tpl2",
            defaultTitle = "Лечь в 23:00",
            defaultDescription = "Подготовить одежду на завтра"
        )
    )

    fun getAllTemplates(): List<TaskTemplate> = templateList

    fun getAllTasks(): Flow<List<Task>> = taskDao.getAllTasks()

    fun getTasksForDate(date: LocalDate): Flow<List<Task>> = taskDao.getTasksForDate(date)

    suspend fun addTask(
        title: String,
        description: String?,
        category: TaskCategory = TaskCategory.OTHER,
        startTime: LocalTime? = null,
        endTime: LocalTime? = null,
        date: LocalDate,
        subtasks: List<Subtask> = emptyList()
    ) {
        val newTask = Task(
            id = UUID.randomUUID().toString(),
            title = title,
            description = description,
            isDone = false,
            category = category,
            startTime = startTime,
            endTime = endTime,
            date = date,
            subtasks = subtasks
        )
        taskDao.insertTask(newTask)
    }

    suspend fun addTaskFromTemplate(template: TaskTemplate) {
        val now = LocalDateTime.now()
        val newTask = Task(
            id = UUID.randomUUID().toString(),
            title = template.defaultTitle,
            description = template.defaultDescription,
            isDone = false,
            category = template.category,
            startTime = template.defaultStartTime,
            endTime = template.defaultEndTime,
            date = now.toLocalDate(),
            subtasks = template.subtasks
        )
        taskDao.insertTask(newTask)
    }

    // --- Работа с шаблонами дней ---

    fun getAllDayTemplates(): Flow<List<DayTemplate>> =
        dayTemplateDao.getAllDayTemplates()

    suspend fun saveDayTemplate(template: DayTemplate) =
        dayTemplateDao.insertDayTemplate(template)

    suspend fun deleteDayTemplate(template: DayTemplate) =
        dayTemplateDao.deleteDayTemplate(template)

    suspend fun applyDayTemplate(template: DayTemplate, date: LocalDate) {
        template.taskTemplates.forEach { tpl ->
            addTask(
                title = tpl.defaultTitle,
                description = tpl.defaultDescription,
                category = tpl.category,
                startTime = tpl.defaultStartTime,
                endTime = tpl.defaultEndTime,
                date = date,
                subtasks = tpl.subtasks
            )
        }
    }

    suspend fun getTemplateForWeekday(weekday: DayOfWeek): DayTemplate? {
        return dayTemplateDao.getTemplateForWeekday(weekday)
    }

    suspend fun updateTask(task: Task) {
        taskDao.updateTask(task)
    }

    suspend fun deleteTask(task: Task) {
        taskDao.deleteTask(task)
    }

    suspend fun markTaskDone(taskId: String, done: Boolean) {
        // (на будущее)
    }
}
