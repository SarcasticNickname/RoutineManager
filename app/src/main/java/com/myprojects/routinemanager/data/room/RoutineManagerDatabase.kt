package com.myprojects.routinemanager.data.room

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.myprojects.routinemanager.data.model.DayTemplate
import com.myprojects.routinemanager.data.model.Task

@Database(
    entities = [
        Task::class,
        DayTemplate::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class RoutineManagerDatabase : RoomDatabase() {
    abstract fun taskDao(): TaskDao

    abstract fun dayTemplateDao(): DayTemplateDao
}
