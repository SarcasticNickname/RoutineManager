package com.myprojects.routinemanager.data.repository

import com.myprojects.routinemanager.data.model.*
import com.myprojects.routinemanager.data.room.TaskDao
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.util.*

/**
 * Репозиторий для работы с задачами и шаблонами задач.
 */
class TaskRepository(
    private val taskDao: TaskDao
) {
    // Пример одиночных шаблонов задач (в будущем может заменяться на БД)
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

    fun getTasksForDate(date: LocalDate): Flow<List<Task>> =
        taskDao.getTasksForDate(date)

    suspend fun addTask(
        title: String,
        description: String? = null,
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

    suspend fun updateTask(task: Task) {
        taskDao.updateTask(task)
    }

    suspend fun deleteTask(task: Task) {
        taskDao.deleteTask(task)
    }

    // Новый метод: удаление всех задач за выбранную дату
    suspend fun deleteAllTasksForDate(date: LocalDate) {
        taskDao.deleteTasksForDate(date)
    }

    suspend fun markTaskDone(taskId: String, done: Boolean) {
        // TODO: Реализация, если нужна (например, updateTask с новым флагом isDone)
    }
}
