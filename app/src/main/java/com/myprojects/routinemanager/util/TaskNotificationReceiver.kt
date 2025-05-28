package com.myprojects.routinemanager.util

import android.Manifest
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.myprojects.routinemanager.MainActivity
import com.myprojects.routinemanager.R // Убедитесь, что R импортирован правильно

class TaskNotificationReceiver : BroadcastReceiver() {

    companion object {
        // Ключи для передачи данных через Intent
        const val EXTRA_TASK_ID = "TASK_ID"
        const val EXTRA_TASK_TITLE = "TASK_TITLE"
        const val EXTRA_TASK_DESCRIPTION = "TASK_DESCRIPTION"
        const val EXTRA_IS_END_TIME = "IS_END_TIME"
    }

    override fun onReceive(context: Context, intent: Intent) {
        val taskId = intent.getStringExtra(EXTRA_TASK_ID) ?: return
        val taskTitle = intent.getStringExtra(EXTRA_TASK_TITLE) ?: "Напоминание о задаче"
        val taskDescription = intent.getStringExtra(EXTRA_TASK_DESCRIPTION)
        val isEndTime = intent.getBooleanExtra(EXTRA_IS_END_TIME, false)

        Log.d("TaskNotificationReceiver", "onReceive called for task: '$taskTitle' ($taskId), IsEndTime: $isEndTime")

        // Intent для открытия MainActivity при нажатии на уведомление
        val mainActivityIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK // Используем CLEAR_TASK для лучшего поведения
            // Можно добавить ID задачи для навигации к деталям
            // putExtra("NAVIGATE_TO_TASK_ID", taskId)
        }

        val pendingIntentFlags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        } else {
            PendingIntent.FLAG_UPDATE_CURRENT
        }

        // PendingIntent для действия по нажатию
        val contentPendingIntent = PendingIntent.getActivity(
            context,
            (taskId + if(isEndTime) "_end" else "_start").hashCode(), // Уникальный requestCode для PendingIntent
            mainActivityIntent,
            pendingIntentFlags
        )

        val notificationTitle = if (isEndTime) "Завершение задачи" else "Напоминание о задаче"
        val contentText = if (isEndTime) {
            "Время задачи '$taskTitle' скоро закончится."
        } else {
            "Скоро начнется задача: '$taskTitle'."
        }

        // Создание уведомления
        val notificationBuilder = NotificationCompat.Builder(context, "TASK_CHANNEL_ID") // Используем ID канала из RoutineManagerApp
            .setSmallIcon(R.drawable.ic_assignment) // Иконка уведомления
            .setContentTitle(notificationTitle)
            .setContentText(contentText)
            .setPriority(NotificationCompat.PRIORITY_HIGH) // Высокий приоритет для напоминаний
            .setContentIntent(contentPendingIntent) // Действие по нажатию
            .setAutoCancel(true) // Закрывать уведомление после нажатия

        // Добавляем расширенный текст, если есть описание
        if (!taskDescription.isNullOrBlank()) {
            val fullText = "$contentText\nОписание: $taskDescription"
            notificationBuilder.setStyle(NotificationCompat.BigTextStyle().bigText(fullText))
        }

        val notification = notificationBuilder.build()
        val notificationManager = NotificationManagerCompat.from(context)

        // Проверка разрешения перед показом (для Android 13+)
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            Log.w("TaskNotificationReceiver", "POST_NOTIFICATIONS permission not granted. Cannot show notification.")
            return
        }

        // Уникальный ID для самого уведомления (чтобы начало и конец не заменяли друг друга)
        val notificationId = (taskId + if(isEndTime) "_end" else "_start").hashCode()
        notificationManager.notify(notificationId, notification)
        Log.d("TaskNotificationReceiver", "Notification shown with ID: $notificationId")
    }
}