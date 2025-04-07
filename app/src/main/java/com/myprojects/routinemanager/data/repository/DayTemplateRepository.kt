package com.myprojects.routinemanager.data.repository

import com.myprojects.routinemanager.data.model.*
import com.myprojects.routinemanager.data.room.DayTemplateDao
import com.myprojects.routinemanager.data.room.TaskDao
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import java.util.UUID
import java.time.DayOfWeek

class DayTemplateRepository(
    private val taskDao: TaskDao,
    private val dayTemplateDao: DayTemplateDao
) {
    fun getAllTemplates(): Flow<List<DayTemplateWithTasks>> =
        dayTemplateDao.getAllTemplatesWithTasks()

    fun getWeeklyTemplates(): Flow<List<DayTemplateWithTasks>> =
        dayTemplateDao.getWeeklyTemplatesWithTasks()

    fun getCustomTemplates(): Flow<List<DayTemplateWithTasks>> =
        dayTemplateDao.getCustomTemplatesWithTasks()

    suspend fun insertTemplate(template: DayTemplate) =
        dayTemplateDao.insertDayTemplate(template)

    suspend fun deleteTemplate(template: DayTemplate) =
        dayTemplateDao.deleteDayTemplate(template)

    suspend fun insertTaskTemplate(taskTemplate: TaskTemplate) =
        dayTemplateDao.insertTaskTemplate(taskTemplate)

    suspend fun deleteTaskTemplate(taskTemplate: TaskTemplate) =
        dayTemplateDao.deleteTaskTemplate(taskTemplate)

    suspend fun getTemplateForWeekday(weekday: DayOfWeek): DayTemplateWithTasks? =
        dayTemplateDao.getTemplateForWeekday(weekday)

    fun getTemplateById(id: String): Flow<DayTemplateWithTasks?> =
        dayTemplateDao.getByIdWithTasks(id)

    suspend fun applyDayTemplate(template: DayTemplateWithTasks, date: LocalDate) {
        template.taskTemplates.forEach { tpl ->
            val task = Task(
                id = UUID.randomUUID().toString(),
                title = tpl.defaultTitle,
                description = tpl.defaultDescription,
                isDone = false,
                category = tpl.category,
                startTime = tpl.defaultStartTime,
                endTime = tpl.defaultEndTime,
                date = date,
                subtasks = tpl.subtasks
            )
            taskDao.insertTask(task)
        }
    }

    suspend fun addTaskTemplateToTemplate(templateId: String, taskTemplate: TaskTemplate) {
        val withOwner = taskTemplate.copy(
            id = UUID.randomUUID().toString(),
            templateId = templateId
        )
        dayTemplateDao.insertTaskTemplate(withOwner)
    }

}
