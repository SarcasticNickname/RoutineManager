package com.myprojects.routinemanager.data.room

import androidx.room.TypeConverter
import com.myprojects.routinemanager.data.model.Subtask
import com.myprojects.routinemanager.data.model.TaskCategory
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

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
}
