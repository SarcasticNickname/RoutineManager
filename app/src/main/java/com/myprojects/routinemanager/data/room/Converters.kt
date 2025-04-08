package com.myprojects.routinemanager.data.room

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.myprojects.routinemanager.data.model.Subtask
import com.myprojects.routinemanager.data.model.TaskCategory
import com.myprojects.routinemanager.data.model.TaskTemplate
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime

class Converters {
    @TypeConverter
    fun fromCategory(value: TaskCategory): String {
        return value.name
    }

    @TypeConverter
    fun toCategory(value: String): TaskCategory {
        return TaskCategory.valueOf(value)
    }

    @TypeConverter
    fun fromSubtaskList(value: List<Subtask>): String {
        return Gson().toJson(value)
    }

    @TypeConverter
    fun toSubtaskList(value: String): List<Subtask> {
        val listType = object : TypeToken<List<Subtask>>() {}.type
        return Gson().fromJson(value, listType)
    }

    @TypeConverter
    fun fromLocalTime(value: LocalTime?): String? {
        return value?.toString()  // формат HH:mm:ss
    }

    @TypeConverter
    fun toLocalTime(value: String?): LocalTime? {
        return value?.let { LocalTime.parse(it) }
    }

    @TypeConverter
    fun fromLocalDate(date: LocalDate?): String? = date?.toString()

    @TypeConverter
    fun toLocalDate(value: String?): LocalDate? =
        value?.let { LocalDate.parse(it) }

    @TypeConverter
    fun fromDayOfWeek(value: DayOfWeek?): String? = value?.name

    @TypeConverter
    fun toDayOfWeek(value: String?): DayOfWeek? = value?.let { DayOfWeek.valueOf(it) }

    @TypeConverter
    fun fromTaskTemplateList(value: List<TaskTemplate>): String = Gson().toJson(value)

    @TypeConverter
    fun toTaskTemplateList(value: String): List<TaskTemplate> {
        val listType = object : TypeToken<List<TaskTemplate>>() {}.type
        return Gson().fromJson(value, listType)
    }
}
