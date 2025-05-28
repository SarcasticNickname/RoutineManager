package com.myprojects.routinemanager.data.model

import androidx.compose.ui.graphics.Color
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDate
import java.time.LocalTime

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
    val subtasks: List<Subtask> = emptyList(),
    val displayOrder: Int = 0
)

// Обновленная функция с новыми категориями и цветами
fun getCategoryColor(category: TaskCategory): Color {
    return when (category) {
        TaskCategory.WORK -> Color(0xFF42A5F5)    // Насыщенный голубой (оставили)
        TaskCategory.STUDY -> Color(0xFFFFCA28)   // Насыщенный жёлтый (оставили)
        TaskCategory.HEALTH -> Color(0xFF66BB6A)   // Ярко-зелёный (был у SPORT)
        TaskCategory.PERSONAL -> Color(0xFF7E57C2) // Яркий фиолетовый (был у MORNING)
        TaskCategory.LEISURE -> Color(0xFFEC407A)   // Ярко-розовый (новый)
        TaskCategory.OTHER -> Color(0xFFBDBDBD)   // Нейтральный серый (оставили)
    }
}