package com.myprojects.routinemanager.data.model

import androidx.compose.ui.graphics.Color
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
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
    val date: LocalDate,
    val isDone: Boolean,
    val category: TaskCategory = TaskCategory.OTHER,
    val startTime: LocalTime? = null,
    val endTime: LocalTime? = null,
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
        TaskCategory.MORNING -> Color(0xFF7E57C2) // яркий фиолетовый
        TaskCategory.WORK -> Color(0xFF42A5F5)    // насыщенный голубой
        TaskCategory.SPORT -> Color(0xFF66BB6A)   // ярко-зелёный
        TaskCategory.STUDY -> Color(0xFFFFCA28)   // насыщенный жёлтый
        TaskCategory.OTHER -> Color(0xFFBDBDBD)   // нейтральный серый
    }
}
