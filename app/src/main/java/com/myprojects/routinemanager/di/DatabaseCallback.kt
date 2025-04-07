package com.myprojects.routinemanager.data.room

import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.myprojects.routinemanager.data.model.DayTemplate
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.DayOfWeek

class DatabaseCallback(
    private val database: RoutineManagerDatabase
) : RoomDatabase.Callback() {

    override fun onCreate(db: SupportSQLiteDatabase) {
        super.onCreate(db)

        CoroutineScope(Dispatchers.IO).launch {
            val dayNamesInRussian = mapOf(
                DayOfWeek.MONDAY to "Понедельник",
                DayOfWeek.TUESDAY to "Вторник",
                DayOfWeek.WEDNESDAY to "Среда",
                DayOfWeek.THURSDAY to "Четверг",
                DayOfWeek.FRIDAY to "Пятница",
                DayOfWeek.SATURDAY to "Суббота",
                DayOfWeek.SUNDAY to "Воскресенье"
            )

            val defaultWeeklyTemplates = DayOfWeek.values().map { day ->
                DayTemplate(
                    name = dayNamesInRussian[day] ?: day.name,
                    isWeekly = true,
                    taskTemplates = emptyList(),
                    weekday = day
                )
            }

            database.dayTemplateDao().insertAll(defaultWeeklyTemplates)
        }
    }
}
