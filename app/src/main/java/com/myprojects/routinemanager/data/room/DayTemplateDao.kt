package com.myprojects.routinemanager.data.room

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.myprojects.routinemanager.data.model.DayTemplate
import kotlinx.coroutines.flow.Flow
import java.time.DayOfWeek

@Dao
interface DayTemplateDao {

    @Query("SELECT * FROM day_templates")
    fun getAllDayTemplates(): Flow<List<DayTemplate>>

    @Query("SELECT * FROM day_templates WHERE isWeekly = 1")
    fun getWeeklyTemplates(): Flow<List<DayTemplate>>

    @Query("SELECT * FROM day_templates WHERE isWeekly = 0")
    fun getCustomTemplates(): Flow<List<DayTemplate>>

    @Query("SELECT * FROM day_templates WHERE weekday = :day LIMIT 1")
    suspend fun getTemplateForWeekday(day: DayOfWeek): DayTemplate?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDayTemplate(template: DayTemplate)

    @Delete
    suspend fun deleteDayTemplate(template: DayTemplate)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(templates: List<DayTemplate>)
}
