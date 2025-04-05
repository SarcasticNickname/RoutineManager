package com.myprojects.routinemanager.data.model

import java.time.LocalTime
import java.util.UUID

data class TaskTemplate(
    val templateId: String = UUID.randomUUID().toString(),
    val defaultTitle: String,
    val defaultDescription: String? = null,
    val category: TaskCategory = TaskCategory.OTHER,
    val defaultStartTime: LocalTime? = null,
    val defaultEndTime: LocalTime? = null,
    val subtasks: List<Subtask> = emptyList()
)
