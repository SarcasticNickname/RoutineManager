package com.myprojects.routinemanager.ui.screens.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.EditCalendar
import androidx.compose.material.icons.outlined.Notes
import androidx.compose.material.icons.outlined.PlaylistAdd
import androidx.compose.material.icons.outlined.TextFields
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

@Composable
fun QuickEditActionsBar(
    onEditTimeClick: () -> Unit,
    onAddSubtaskClick: () -> Unit,
    onEditTitleClick: () -> Unit,
    onEditDescriptionClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 0.dp, horizontal = 16.dp), // Уменьшили вертикальный отступ
        horizontalArrangement = Arrangement.SpaceAround,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Вызываем новую, упрощенную версию QuickAction
        QuickAction(
            icon = Icons.Outlined.EditCalendar,
            contentDescription = "Изменить время", // Важно для доступности
            onClick = onEditTimeClick
        )
        QuickAction(
            icon = Icons.Outlined.PlaylistAdd,
            contentDescription = "Добавить подзадачу",
            onClick = onAddSubtaskClick
        )
        QuickAction(
            icon = Icons.Outlined.TextFields,
            contentDescription = "Изменить название",
            onClick = onEditTitleClick
        )
        QuickAction(
            icon = Icons.Outlined.Notes,
            contentDescription = "Изменить описание",
            onClick = onEditDescriptionClick
        )
    }
}

/**
 * Упрощенная версия кнопки: теперь это просто иконка без подписи.
 */
@Composable
private fun QuickAction(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit
) {
    IconButton(
        onClick = onClick,
        // Ограничиваем размер самой кнопки для лучшего вида
        modifier = Modifier.size(48.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            // Задаем иконке стандартный размер
            modifier = Modifier.size(24.dp),
            tint = MaterialTheme.colorScheme.primary
        )
    }
}