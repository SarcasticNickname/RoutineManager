package com.myprojects.routinemanager.data.room

import android.util.Log
import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.myprojects.routinemanager.data.model.Subtask
import com.myprojects.routinemanager.data.model.TaskCategory
import com.myprojects.routinemanager.data.model.TaskTemplate
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeParseException

class Converters {
    @TypeConverter
    fun fromCategory(value: TaskCategory): String {
        return value.name
    }

    @TypeConverter
    fun toCategory(value: String): TaskCategory {
        return try {
            TaskCategory.valueOf(value)
        } catch (e: IllegalArgumentException) {
            Log.e("Converters", "Invalid TaskCategory value found: $value, defaulting to OTHER")
            TaskCategory.OTHER
        }
    }

    @TypeConverter
    fun fromSubtaskList(value: List<Subtask>): String {
        return Gson().toJson(value)
    }

    @TypeConverter
    fun toSubtaskList(value: String): List<Subtask> {
        return try {
            val listType = object : TypeToken<List<Subtask>>() {}.type
            Gson().fromJson(value, listType) ?: emptyList()
        } catch (e: Exception) {
            Log.e("Converters", "Failed to parse SubtaskList from JSON: $value", e)
            emptyList()
        }
    }

    @TypeConverter
    fun fromLocalTime(value: LocalTime?): String? {
        return value?.toString()
    }

    @TypeConverter
    fun toLocalTime(value: String?): LocalTime? {
        if (value == null) return null
        return try {
            LocalTime.parse(value)
        } catch (e: DateTimeParseException) {
            Log.e("Converters", "Failed to parse LocalTime string: $value", e)
            null
        }
    }

    @TypeConverter
    fun fromLocalDate(date: LocalDate?): String? = date?.toString()

    @TypeConverter
    fun toLocalDate(value: String?): LocalDate { // <-- Изменено: возвращаемый тип теперь НЕ nullable
        val defaultDate = LocalDate.of(1970, 1, 1) // Дефолтная дата, если парсинг не удался

        if (value == null || value == "0000-00-00") {
            Log.w("Converters", "Invalid or null date string found: $value, returning default date $defaultDate")
            return defaultDate // Возвращаем дефолтную дату
        }
        return try {
            LocalDate.parse(value)
        } catch (e: DateTimeParseException) {
            Log.e("Converters", "Failed to parse LocalDate string: $value, returning default date $defaultDate", e)
            defaultDate // Возвращаем дефолтную дату при ошибке парсинга
        }
    }

    @TypeConverter
    fun fromDayOfWeek(value: DayOfWeek?): String? = value?.name

    @TypeConverter
    fun toDayOfWeek(value: String?): DayOfWeek? {
        if (value == null) return null
        return try {
            DayOfWeek.valueOf(value)
        } catch (e: IllegalArgumentException) {
            Log.e("Converters", "Invalid DayOfWeek value found: $value, returning null")
            null
        }
    }

    @TypeConverter
    fun fromTaskTemplateList(value: List<TaskTemplate>): String = Gson().toJson(value)

    @TypeConverter
    fun toTaskTemplateList(value: String): List<TaskTemplate> {
        return try {
            val listType = object : TypeToken<List<TaskTemplate>>() {}.type
            Gson().fromJson(value, listType) ?: emptyList()
        } catch (e: Exception) {
            Log.e("Converters", "Failed to parse TaskTemplateList from JSON: $value", e)
            emptyList()
        }
    }
}