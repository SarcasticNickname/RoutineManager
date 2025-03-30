package com.example.routinemanager.util

import android.content.Context
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Intent
import java.time.LocalDateTime

/**
 * Каркас класса для планирования напоминаний о задачах.
 * Можно использовать AlarmManager, WorkManager или другие механизмы.
 */
class TaskNotificationManager(private val context: Context) {

    /**
     * Запланировать локальное напоминание на указанное время.
     */
    fun scheduleNotification(taskId: String, time: LocalDateTime) {
        // Пример использования AlarmManager (упрощённо)
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        // В реальном проекте надо подготовить PendingIntent с BroadcastReceiver
        // либо Service, который сработает в нужное время и покажет уведомление.
        val intent = Intent(context, /* ВашReceiverКласс::class.java */ javaClass)
        intent.putExtra("TASK_ID", taskId)

        // pendingIntent — просто пример
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Преобразование LocalDateTime в millis
        // (в реальном проекте используем TimeZone, Instant и т.д.)
        val triggerTime = time.atZone(java.time.ZoneId.systemDefault()).toEpochSecond() * 1000

        // Настраиваем AlarmManager
        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            triggerTime,
            pendingIntent
        )
    }

    /**
     * Отменить напоминание.
     */
    fun cancelNotification(taskId: String) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, /* ВашReceiverКласс::class.java */ javaClass)
        intent.putExtra("TASK_ID", taskId)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pendingIntent)
    }
}
