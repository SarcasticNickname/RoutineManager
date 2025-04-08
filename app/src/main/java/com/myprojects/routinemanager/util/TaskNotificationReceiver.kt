package com.myprojects.routinemanager.util

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.annotation.RequiresPermission
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.myprojects.routinemanager.R

/**
 * Принимает сигнал от AlarmManager и показывает уведомление.
 */
class TaskNotificationReceiver : BroadcastReceiver() {
    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    override fun onReceive(context: Context, intent: Intent) {
        // Получаем идентификатор задачи, по которому было запланировано уведомление.
        val taskId = intent.getStringExtra("TASK_ID") ?: return

        // Для примера можно задать фиксированный текст уведомления.
        // В реальном проекте можно получить подробности задачи (например, через репозиторий)
        // и сформировать более осмысленный текст уведомления.
        val notification = NotificationCompat.Builder(context, "TASK_CHANNEL_ID")
            .setSmallIcon(R.drawable.ic_assignment)
            .setContentTitle("Напоминание о задаче")
            .setContentText("Скоро наступит время для задачи: $taskId")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        val notificationManager = NotificationManagerCompat.from(context)
        // Используем hashCode taskId в качестве идентификатора уведомления.
        notificationManager.notify(taskId.hashCode(), notification)
    }
}
