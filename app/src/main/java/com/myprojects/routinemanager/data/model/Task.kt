package com.myprojects.routinemanager.data.model

import androidx.compose.ui.graphics.Color
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/**
 * Модель задачи, хранящаяся в базе данных.
 * Аннотация @Entity указывает, что это таблица Room.
 */
@Entity(tableName = "tasks")
data class Task(
    @PrimaryKey val id: String,
    val title: String,
    val description: String?,
    val dueDateTime: String?,
    val isDone: Boolean,
    val category: TaskCategory = TaskCategory.OTHER,
    val subtasks: List<Subtask> = emptyList()
) {
    companion object {
        /**
         * Утилитный метод для генерации строкового представления даты.
         */
        fun localDateTimeToString(dateTime: LocalDateTime?): String? {
            return dateTime?.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
        }

        fun stringToLocalDateTime(value: String?): LocalDateTime? {
            return value?.let {
                LocalDateTime.parse(it, DateTimeFormatter.ISO_LOCAL_DATE_TIME)
            }
        }
    }
}

fun getCategoryColor(category: TaskCategory): Color {
    return when (category) {
        TaskCategory.MORNING -> Color(0xFFB39DDB)
        TaskCategory.WORK -> Color(0xFF90CAF9)
        TaskCategory.SPORT -> Color(0xFFA5D6A7)
        TaskCategory.STUDY -> Color(0xFFFFF59D)
        TaskCategory.OTHER -> Color(0xFFE0E0E0)
    }
}
