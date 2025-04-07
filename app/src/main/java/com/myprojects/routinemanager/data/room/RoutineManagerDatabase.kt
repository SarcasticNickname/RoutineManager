package com.myprojects.routinemanager.data.room

import android.util.Log
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import com.myprojects.routinemanager.data.model.DayTemplate
import com.myprojects.routinemanager.data.model.Task
import com.myprojects.routinemanager.data.model.TaskTemplate
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.DayOfWeek

@Database(
    entities = [
        Task::class,
        DayTemplate::class,
        TaskTemplate::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class RoutineManagerDatabase : RoomDatabase() {
    abstract fun taskDao(): TaskDao

    abstract fun dayTemplateDao(): DayTemplateDao

    companion object {
        fun createCallback(): Callback {
            return object : Callback() {
                override fun onCreate(db: SupportSQLiteDatabase) {
                    super.onCreate(db)

                    // Это будет вызвано один раз при первом создании базы
                    CoroutineScope(Dispatchers.IO).launch {
                        Log.d("RoomInit", "⚡ onCreate вызван — запускаем prepopulate")

                        val dayNames = mapOf(
                            DayOfWeek.MONDAY to "Понедельник",
                            DayOfWeek.TUESDAY to "Вторник",
                            DayOfWeek.WEDNESDAY to "Среда",
                            DayOfWeek.THURSDAY to "Четверг",
                            DayOfWeek.FRIDAY to "Пятница",
                            DayOfWeek.SATURDAY to "Суббота",
                            DayOfWeek.SUNDAY to "Воскресенье"
                        )

                        // Подождём, пока база построится
                        delay(300) // можно и без этого, если всё и так работает

                        // Нельзя напрямую получить DAO, но можно передать внешний reference через AppModule
                        databaseRef?.dayTemplateDao()?.insertAllDayTemplates(
                            DayOfWeek.values().map { day ->
                                DayTemplate(
                                    name = dayNames[day] ?: day.name,
                                    isWeekly = true,
                                    taskTemplates = emptyList(),
                                    weekday = day
                                )
                            }
                        )

                        Log.d("RoomInit", "✅ 7 шаблонов недели добавлены")
                    }
                }
            }
        }

        // Чтобы передать ссылку на базу (см. ниже)
        var databaseRef: RoutineManagerDatabase? = null
    }
}
