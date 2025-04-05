package com.myprojects.routinemanager.data.repository

import com.myprojects.routinemanager.data.model.DayTemplate
import com.myprojects.routinemanager.data.model.Task
import com.myprojects.routinemanager.data.room.DayTemplateDao
import com.myprojects.routinemanager.data.room.TaskDao
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import java.util.UUID

class DayTemplateRepository(
    private val taskDao: TaskDao,
    private val dayTemplateDao: DayTemplateDao
) {
    fun getAllTemplates(): Flow<List<DayTemplate>> =
        dayTemplateDao.getAllDayTemplates()

    fun getWeeklyTemplates(): Flow<List<DayTemplate>> =
        dayTemplateDao.getWeeklyTemplates()

    fun getCustomTemplates(): Flow<List<DayTemplate>> =
        dayTemplateDao.getCustomTemplates()

    suspend fun insertTemplate(template: DayTemplate) =
        dayTemplateDao.insertDayTemplate(template)

    suspend fun deleteTemplate(template: DayTemplate) =
        dayTemplateDao.deleteDayTemplate(template)

    suspend fun getTemplateForWeekday(weekday: java.time.DayOfWeek): DayTemplate? =
        dayTemplateDao.getTemplateForWeekday(weekday)

    suspend fun applyDayTemplate(template: DayTemplate, date: LocalDate) {
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
}
