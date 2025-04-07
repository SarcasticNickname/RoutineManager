package com.myprojects.routinemanager.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.myprojects.routinemanager.data.room.Converters
import java.time.LocalTime
import java.util.UUID

@Entity(tableName = "task_templates")
@TypeConverters(Converters::class)
data class TaskTemplate(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val templateId: String, // foreign key to DayTemplate
    val defaultTitle: String,
    val defaultDescription: String? = null,
    val defaultStartTime: LocalTime? = null,
    val defaultEndTime: LocalTime? = null,
    val category: TaskCategory = TaskCategory.OTHER,
    val subtasks: List<Subtask> = emptyList()
)
