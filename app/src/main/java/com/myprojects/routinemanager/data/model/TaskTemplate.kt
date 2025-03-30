package com.myprojects.routinemanager.data.model

/**
 * Модель шаблона задачи.
 * Шаблоны можно хранить в базе данных или в отдельном источнике.
 */
data class TaskTemplate(
    val templateId: String,
    val name: String,
    val defaultTitle: String,
    val defaultDescription: String? = null
)
