package com.myprojects.routinemanager.data.repository

import com.myprojects.routinemanager.data.model.Subtask
import com.myprojects.routinemanager.data.model.Task
import com.myprojects.routinemanager.data.model.TaskCategory
import com.myprojects.routinemanager.data.model.TaskTemplate
import com.myprojects.routinemanager.data.room.TaskDao
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.util.UUID

/**
 * Репозиторий для работы с задачами и шаблонами задач.
 */
class TaskRepository(
    private val taskDao: TaskDao
) {

    fun getAllTasks(): Flow<List<Task>> = taskDao.getAllTasks()

    fun getTasksForDate(date: LocalDate): Flow<List<Task>> =
        taskDao.getTasksForDate(date)

    /**
     * Новый метод для вставки задачи, сгенерированной вне репозитория.
     */
    suspend fun insertTask(task: Task) {
        taskDao.insertTask(task)
    }

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

    suspend fun updateTasks(tasks: List<Task>) {
        taskDao.updateTasks(tasks)
    }

    suspend fun deleteTask(task: Task) {
        taskDao.deleteTask(task)
    }

    suspend fun deleteAllTasksForDate(date: LocalDate) {
        taskDao.deleteTasksForDate(date)
    }


    suspend fun deleteAllTasks() {
        taskDao.deleteAllTasks()
    }

    suspend fun markTaskDone(taskId: String, done: Boolean) {
        val task = taskDao.getTaskById(taskId)
        if (task != null) {
            val updatedTask = task.copy(isDone = done)
            taskDao.updateTask(updatedTask)
        }
    }
}
