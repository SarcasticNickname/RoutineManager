package com.myprojects.routinemanager.data.model

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
    val dueDateTime: String?,  // Храним как строку для упрощения
    val isDone: Boolean
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
