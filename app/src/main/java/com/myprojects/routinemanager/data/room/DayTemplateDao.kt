package com.myprojects.routinemanager.data.room

import androidx.room.*
import com.myprojects.routinemanager.data.model.DayTemplate
import com.myprojects.routinemanager.data.model.DayTemplateWithTasks
import com.myprojects.routinemanager.data.model.TaskTemplate
import kotlinx.coroutines.flow.Flow
import java.time.DayOfWeek

@Dao
interface DayTemplateDao {

    // --- Queries for DayTemplate ---
    @Query("SELECT * FROM day_templates")
    fun getAllDayTemplates(): Flow<List<DayTemplate>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDayTemplate(template: DayTemplate)

    @Delete
    suspend fun deleteDayTemplate(template: DayTemplate)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllDayTemplates(templates: List<DayTemplate>)

    @Query("DELETE FROM day_templates")
    suspend fun deleteAllDayTemplates()

    // --- Queries for TaskTemplate ---
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTaskTemplate(template: TaskTemplate)

    @Delete
    suspend fun deleteTaskTemplate(template: TaskTemplate)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllTaskTemplates(taskTemplates: List<TaskTemplate>)

    @Query("DELETE FROM task_templates")
    suspend fun deleteAllTaskTemplates()

    // --- Queries combining DayTemplate and TaskTemplate ---
    @Transaction // Ensures atomic read
    @Query("SELECT * FROM day_templates")
    fun getAllTemplatesWithTasks(): Flow<List<DayTemplateWithTasks>>

    @Transaction
    @Query("SELECT * FROM day_templates WHERE isWeekly = 1")
    fun getWeeklyTemplatesWithTasks(): Flow<List<DayTemplateWithTasks>>

    @Transaction
    @Query("SELECT * FROM day_templates WHERE isWeekly = 0")
    fun getCustomTemplatesWithTasks(): Flow<List<DayTemplateWithTasks>>

    @Transaction
    @Query("SELECT * FROM day_templates WHERE weekday = :day LIMIT 1")
    suspend fun getTemplateForWeekday(day: DayOfWeek): DayTemplateWithTasks?

    @Transaction
    @Query("SELECT * FROM day_templates WHERE id = :id")
    fun getByIdWithTasks(id: String): Flow<DayTemplateWithTasks?>

    // --- NEW METHOD: Get TaskTemplates for a specific DayTemplate as a Flow ---
    @Query("SELECT * FROM task_templates WHERE templateId = :dayTemplateId ORDER BY defaultTitle ASC")
    fun getTaskTemplatesForDayTemplateFlow(dayTemplateId: String): Flow<List<TaskTemplate>>
}