package com.myprojects.routinemanager.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.myprojects.routinemanager.data.room.Converters
import java.time.DayOfWeek
import java.util.UUID

@Entity(tableName = "day_templates")
@TypeConverters(Converters::class)
data class DayTemplate(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val name: String,
    val taskTemplates: List<TaskTemplate>,
    val isWeekly: Boolean = false,             // true — шаблон привязан к дню недели
    val weekday: DayOfWeek? = null             // понедельник, вторник и т.д. (если weekly)
)
