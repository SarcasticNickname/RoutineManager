package com.myprojects.routinemanager.data.repository

import com.myprojects.routinemanager.data.model.Task
import com.myprojects.routinemanager.data.model.TaskTemplate
import com.myprojects.routinemanager.data.room.TaskDao
import kotlinx.coroutines.flow.Flow
import java.util.UUID
import java.time.LocalDateTime
import com.myprojects.routinemanager.data.model.Task.Companion.localDateTimeToString

/**
 * Репозиторий для работы с задачами и их шаблонами.
 * Выполняет обращение к DAO для операций над базой данных.
 */
class TaskRepository(
    private var taskDao: TaskDao
) {
    // Можно хранить шаблоны в памяти или в отдельной таблице.
    // Здесь — просто пример нескольких шаблонов.
    private val templateList = listOf(
        TaskTemplate(
            templateId = "tpl1",
            name = "Утренняя рутина",
            defaultTitle = "Подъём в 7:00",
            defaultDescription = "Проверить почту, позавтракать"
        ),
        TaskTemplate(
            templateId = "tpl2",
            name = "Вечерняя рутина",
            defaultTitle = "Лечь в 23:00",
            defaultDescription = "Подготовить одежду на завтра"
        )
    )

    fun getAllTemplates(): List<TaskTemplate> {
        return templateList
    }

    fun getAllTasks(): Flow<List<Task>> = taskDao.getAllTasks()

    suspend fun addTask(title: String, description: String?, dueTime: LocalDateTime? = null) {
        val newTask = Task(
            id = UUID.randomUUID().toString(),
            title = title,
            description = description,
            dueDateTime = localDateTimeToString(dueTime),
            isDone = false
        )
        taskDao.insertTask(newTask)
    }

    suspend fun addTaskFromTemplate(template: TaskTemplate) {
        val newTask = Task(
            id = UUID.randomUUID().toString(),
            title = template.defaultTitle,
            description = template.defaultDescription,
            dueDateTime = localDateTimeToString(LocalDateTime.now().plusHours(1)),
            isDone = false
        )
        taskDao.insertTask(newTask)
    }

    suspend fun markTaskDone(taskId: String, done: Boolean) {
        // Для обновления нужно получить объект из базы (упрощённый пример)
        val currentList = taskDao.getAllTasks() // Flow, но мы можем собрать первым значением
        // Этот шаг обычно требует скоупа корутин и сбора Flow, поэтому в реальном коде
        // пишут иначе (через отдельный метод DAO).
    }

    suspend fun deleteTask(task: Task) {
        taskDao.deleteTask(task)
    }

    /**
     * Пример метода, когда нужно найти задачу и обновить поле isDone.
     * Здесь для наглядности сделана отдельная операция в DAO (updateTask).
     */
    suspend fun updateTask(task: Task) {
        taskDao.updateTask(task)
    }
}
