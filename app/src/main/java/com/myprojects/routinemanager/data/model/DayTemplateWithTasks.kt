package com.myprojects.routinemanager.data.model

import androidx.room.Embedded
import androidx.room.Relation

data class DayTemplateWithTasks(
    @Embedded val template: DayTemplate,
    @Relation(
        parentColumn = "id",
        entityColumn = "templateId"
    )
    val taskTemplates: List<TaskTemplate>
)
