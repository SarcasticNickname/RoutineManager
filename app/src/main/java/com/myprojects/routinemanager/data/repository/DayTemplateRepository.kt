package com.myprojects.routinemanager.data.repository

import com.myprojects.routinemanager.data.model.DayTemplate
import com.myprojects.routinemanager.data.model.DayTemplateWithTasks
import com.myprojects.routinemanager.data.model.Task
import com.myprojects.routinemanager.data.model.TaskTemplate
import com.myprojects.routinemanager.data.room.DayTemplateDao
import com.myprojects.routinemanager.data.room.TaskDao
import kotlinx.coroutines.flow.Flow
import java.time.DayOfWeek
import java.time.LocalDate
import java.util.UUID

class DayTemplateRepository(
    private val taskDao: TaskDao, // Needed for applyDayTemplate
    private val dayTemplateDao: DayTemplateDao
) {
    // --- Methods for DayTemplateWithTasks ---
    fun getAllTemplates(): Flow<List<DayTemplateWithTasks>> =
        dayTemplateDao.getAllTemplatesWithTasks()

    fun getWeeklyTemplates(): Flow<List<DayTemplateWithTasks>> =
        dayTemplateDao.getWeeklyTemplatesWithTasks()

    fun getCustomTemplates(): Flow<List<DayTemplateWithTasks>> =
        dayTemplateDao.getCustomTemplatesWithTasks()

    suspend fun getTemplateForWeekday(weekday: DayOfWeek): DayTemplateWithTasks? =
        dayTemplateDao.getTemplateForWeekday(weekday)

    fun getTemplateById(id: String): Flow<DayTemplateWithTasks?> =
        dayTemplateDao.getByIdWithTasks(id)

    // --- Methods for DayTemplate ---
    fun getAllDayTemplates(): Flow<List<DayTemplate>> =
        dayTemplateDao.getAllDayTemplates()

    suspend fun insertTemplate(template: DayTemplate) =
        dayTemplateDao.insertDayTemplate(template)

    suspend fun deleteTemplate(template: DayTemplate) =
        dayTemplateDao.deleteDayTemplate(template)

    suspend fun deleteAllDayTemplates() {
        dayTemplateDao.deleteAllDayTemplates()
    }

    // --- Methods for TaskTemplate ---
    suspend fun insertTaskTemplate(taskTemplate: TaskTemplate) =
        dayTemplateDao.insertTaskTemplate(taskTemplate)

    suspend fun deleteTaskTemplate(taskTemplate: TaskTemplate) =
        dayTemplateDao.deleteTaskTemplate(taskTemplate)

    suspend fun deleteAllTaskTemplates() {
        dayTemplateDao.deleteAllTaskTemplates()
    }

    // --- NEW METHOD: Stream TaskTemplates for a specific DayTemplate ---
    fun getTaskTemplatesStream(dayTemplateId: String): Flow<List<TaskTemplate>> {
        return dayTemplateDao.getTaskTemplatesForDayTemplateFlow(dayTemplateId)
    }
    // --- END NEW METHOD ---

    // --- Business Logic ---
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
            taskDao.insertTask(task) // Use injected TaskDao
        }
    }

    // Optional: Can be removed if logic is fully in ViewModel
    suspend fun addTaskTemplateToTemplate(templateId: String, taskTemplate: TaskTemplate) {
        val withOwner = taskTemplate.copy(
            id = UUID.randomUUID().toString(),
            templateId = templateId
        )
        dayTemplateDao.insertTaskTemplate(withOwner)
    }
}