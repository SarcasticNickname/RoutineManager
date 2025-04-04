package com.myprojects.routinemanager.data.room

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.myprojects.routinemanager.data.model.Task

/**
 * Класс базы данных Room.
 * Позволяет получать доступ к DAO для работы с таблицей tasks.
 */
@Database(entities = [Task::class], version = 1, exportSchema = false)
@TypeConverters(Converters::class)
abstract class RoutineManagerDatabase : RoomDatabase() {
    abstract fun taskDao(): TaskDao
}
