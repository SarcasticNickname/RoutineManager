package com.myprojects.routinemanager.data.room

import android.util.Log
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import com.myprojects.routinemanager.data.model.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime
import java.util.UUID

// Constant ID for the hidden DayTemplate holding standalone TaskTemplates
const val STANDALONE_TASK_TEMPLATE_HOLDER_ID = "__standalone_task_templates__"

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
                    // Prepopulate database on creation
                    CoroutineScope(Dispatchers.IO).launch {
                        Log.d("RoomInit", "Database created, PREPOPULATING with sample data...")
                        delay(500) // Short delay just in case

                        val dayDao = databaseRef?.dayTemplateDao()
                        val taskDao = databaseRef?.taskDao()

                        if (dayDao == null || taskDao == null) {
                            Log.e("RoomInit", "DAO instances are null during prepopulation!")
                            return@launch
                        }

                        // --- 1. Create Day Templates (Weekly + Holder + Custom) ---
                        Log.d("RoomInit", "Creating Day Templates...")
                        val weeklyTemplatesMap = mutableMapOf<DayOfWeek, String>() // To store IDs of weekly templates
                        val dayNames = mapOf(
                            DayOfWeek.MONDAY to "Понедельник", DayOfWeek.TUESDAY to "Вторник",
                            DayOfWeek.WEDNESDAY to "Среда", DayOfWeek.THURSDAY to "Четверг",
                            DayOfWeek.FRIDAY to "Пятница", DayOfWeek.SATURDAY to "Суббота",
                            DayOfWeek.SUNDAY to "Воскресенье"
                        )

                        // Create Weekly Templates
                        val weeklyDayTemplates = DayOfWeek.values().map { day ->
                            val templateId = UUID.randomUUID().toString()
                            weeklyTemplatesMap[day] = templateId // Store ID for later use
                            DayTemplate(
                                id = templateId, name = dayNames[day] ?: day.name,
                                isWeekly = true, taskTemplates = emptyList(), weekday = day
                            )
                        }
                        try {
                            dayDao.insertAllDayTemplates(weeklyDayTemplates)
                            Log.d("RoomInit", "Added 7 weekly DayTemplates.")
                        } catch (e: Exception) {
                            Log.e("RoomInit", "Error inserting weekly DayTemplates", e)
                        }


                        // Create Hidden Holder Template
                        val standaloneHolder = DayTemplate(
                            id = STANDALONE_TASK_TEMPLATE_HOLDER_ID, name = "Standalone Holder",
                            isWeekly = false, taskTemplates = emptyList(), weekday = null
                        )
                        try {
                            dayDao.insertDayTemplate(standaloneHolder)
                            Log.d("RoomInit", "Added Standalone Task Template Holder.")
                        } catch (e: Exception) {
                            Log.e("RoomInit", "Error inserting standalone holder", e)
                        }

                        // Create Custom Day Templates
                        val customTemplate1Id = UUID.randomUUID().toString()
                        val customTemplate1 = DayTemplate(
                            id = customTemplate1Id, name = "Утренняя Рутина",
                            isWeekly = false, taskTemplates = emptyList(), weekday = null
                        )
                        val customTemplate2Id = UUID.randomUUID().toString()
                        val customTemplate2 = DayTemplate(
                            id = customTemplate2Id, name = "Подготовка к Сну",
                            isWeekly = false, taskTemplates = emptyList(), weekday = null
                        )
                        try {
                            dayDao.insertDayTemplate(customTemplate1)
                            dayDao.insertDayTemplate(customTemplate2)
                            Log.d("RoomInit", "Added 2 custom DayTemplates.")
                        } catch (e: Exception) {
                            Log.e("RoomInit", "Error inserting custom DayTemplates", e)
                        }

                        // --- 2. Create Task Templates ---
                        Log.d("RoomInit", "Creating Task Templates...")
                        val taskTemplatesToInsert = mutableListOf<TaskTemplate>()

                        // Templates for Monday
                        weeklyTemplatesMap[DayOfWeek.MONDAY]?.let { mondayId ->
                            taskTemplatesToInsert.add(TaskTemplate(templateId = mondayId, defaultTitle = "Планирование недели", category = TaskCategory.WORK, defaultStartTime = LocalTime.of(9, 0)))
                            taskTemplatesToInsert.add(TaskTemplate(templateId = mondayId, defaultTitle = "Утренний Стендап", category = TaskCategory.WORK, defaultStartTime = LocalTime.of(9, 30)))
                        }
                        // Templates for Friday
                        weeklyTemplatesMap[DayOfWeek.FRIDAY]?.let { fridayId ->
                            taskTemplatesToInsert.add(TaskTemplate(templateId = fridayId, defaultTitle = "Подведение итогов недели", category = TaskCategory.WORK, defaultStartTime = LocalTime.of(17, 0)))
                            taskTemplatesToInsert.add(TaskTemplate(templateId = fridayId, defaultTitle = "Заказ продуктов", category = TaskCategory.PERSONAL))
                        }
                        // Templates for "Утренняя Рутина" (Custom)
                        taskTemplatesToInsert.add(TaskTemplate(templateId = customTemplate1Id, defaultTitle = "Выпить стакан воды", category = TaskCategory.HEALTH))
                        taskTemplatesToInsert.add(TaskTemplate(templateId = customTemplate1Id, defaultTitle = "Легкая зарядка", defaultDescription = "15 минут", category = TaskCategory.HEALTH, defaultStartTime = LocalTime.of(7, 15)))
                        taskTemplatesToInsert.add(TaskTemplate(templateId = customTemplate1Id, defaultTitle = "Завтрак", category = TaskCategory.PERSONAL, defaultStartTime = LocalTime.of(7, 45)))

                        // Templates for "Подготовка к Сну" (Custom)
                        taskTemplatesToInsert.add(TaskTemplate(templateId = customTemplate2Id, defaultTitle = "Приглушить свет", category = TaskCategory.PERSONAL))
                        taskTemplatesToInsert.add(TaskTemplate(templateId = customTemplate2Id, defaultTitle = "Чтение книги", category = TaskCategory.LEISURE, defaultStartTime = LocalTime.of(22, 0)))
                        taskTemplatesToInsert.add(TaskTemplate(templateId = customTemplate2Id, defaultTitle = "Убрать телефон", category = TaskCategory.HEALTH, defaultStartTime = LocalTime.of(22, 30)))

                        // Standalone Task Templates (linked to holder)
                        taskTemplatesToInsert.add(TaskTemplate(templateId = STANDALONE_TASK_TEMPLATE_HOLDER_ID, defaultTitle = "Проверить почту", category = TaskCategory.WORK))
                        taskTemplatesToInsert.add(TaskTemplate(templateId = STANDALONE_TASK_TEMPLATE_HOLDER_ID, defaultTitle = "Обед", category = TaskCategory.PERSONAL, defaultStartTime = LocalTime.of(13, 0), defaultEndTime = LocalTime.of(13, 45)))
                        taskTemplatesToInsert.add(TaskTemplate(templateId = STANDALONE_TASK_TEMPLATE_HOLDER_ID, defaultTitle = "Позвонить родителям", category = TaskCategory.PERSONAL))
                        taskTemplatesToInsert.add(TaskTemplate(templateId = STANDALONE_TASK_TEMPLATE_HOLDER_ID, defaultTitle = "Прогулка", category = TaskCategory.LEISURE))

                        try {
                            dayDao.insertAllTaskTemplates(taskTemplatesToInsert)
                            Log.d("RoomInit", "Added ${taskTemplatesToInsert.size} TaskTemplates.")
                        } catch (e: Exception) {
                            Log.e("RoomInit", "Error inserting TaskTemplates", e)
                        }


                        // --- 3. Create Tasks for specific dates ---
                        Log.d("RoomInit", "Creating Tasks...")
                        val today = LocalDate.now()
                        val tomorrow = today.plusDays(1)
                        val yesterday = today.minusDays(1)

                        var orderCounterToday = 0
                        var orderCounterTomorrow = 0
                        var orderCounterYesterday = 0

                        // Ensure all non-nullable parameters are provided or have defaults in the data class
                        val tasksToInsert = listOf(
                            // Today's Tasks
                            Task(id = UUID.randomUUID().toString(), title = "Закончить отчет X", description = "Проверить данные за Q2", date = today, isDone = false, category = TaskCategory.WORK, startTime = LocalTime.of(10, 0), endTime = LocalTime.of(12, 30), subtasks = emptyList(), displayOrder = orderCounterToday++),
                            Task(id = UUID.randomUUID().toString(), title = "Сходить в магазин", description = "Молоко, хлеб, яйца", date = today, isDone = false, category = TaskCategory.PERSONAL, startTime = null, endTime = null, subtasks = listOf(Subtask("Молоко"), Subtask("Хлеб"), Subtask("Яйца")), displayOrder = orderCounterToday++),
                            Task(id = UUID.randomUUID().toString(), title = "Тренировка", description = "Зал - ноги", date = today, isDone = true, category = TaskCategory.HEALTH, startTime = LocalTime.of(18, 0), endTime = LocalTime.of(19, 30), subtasks = emptyList(), displayOrder = orderCounterToday++),

                            // Tomorrow's Tasks
                            Task(id = UUID.randomUUID().toString(), title = "Встреча с клиентом Y", description = null, date = tomorrow, isDone = false, category = TaskCategory.WORK, startTime = LocalTime.of(11, 0), endTime = null, subtasks = emptyList(), displayOrder = orderCounterTomorrow++),
                            Task(id = UUID.randomUUID().toString(), title = "Записаться к врачу", description = null, date = tomorrow, isDone = false, category = TaskCategory.HEALTH, startTime = null, endTime = null, subtasks = emptyList(), displayOrder = orderCounterTomorrow++),

                            // Yesterday's Tasks
                            Task(id = UUID.randomUUID().toString(), title = "Лекция по Kotlin", description = null, date = yesterday, isDone = true, category = TaskCategory.STUDY, startTime = LocalTime.of(14, 0), endTime = LocalTime.of(16, 0), subtasks = emptyList(), displayOrder = orderCounterYesterday++),
                            Task(id = UUID.randomUUID().toString(), title = "Уборка", description = null, date = yesterday, isDone = true, category = TaskCategory.PERSONAL, startTime = null, endTime = null, subtasks = emptyList(), displayOrder = orderCounterYesterday++)
                        )

                        try {
                            tasksToInsert.forEach { taskDao.insertTask(it) } // Insert one by one
                            Log.d("RoomInit", "Added ${tasksToInsert.size} sample Tasks.")
                        } catch (e: Exception) {
                            Log.e("RoomInit", "Error inserting sample Tasks", e)
                        }


                        Log.d("RoomInit", "PREPOPULATION COMPLETED!")
                    }
                }
            }
        }
        // Reference to the database instance
        @Volatile
        var databaseRef: RoutineManagerDatabase? = null
    }
}