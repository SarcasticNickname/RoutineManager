package com.myprojects.routinemanager.data.room

import androidx.room.*
import com.myprojects.routinemanager.data.model.DayTemplate
import com.myprojects.routinemanager.data.model.DayTemplateWithTasks
import com.myprojects.routinemanager.data.model.TaskTemplate
import kotlinx.coroutines.flow.Flow
import java.time.DayOfWeek

@Dao
interface DayTemplateDao {

    @Transaction
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

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDayTemplate(template: DayTemplate)

    @Delete
    suspend fun deleteDayTemplate(template: DayTemplate)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTaskTemplate(template: TaskTemplate)

    @Delete
    suspend fun deleteTaskTemplate(template: TaskTemplate)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllDayTemplates(templates: List<DayTemplate>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllTaskTemplates(taskTemplates: List<TaskTemplate>)
}
