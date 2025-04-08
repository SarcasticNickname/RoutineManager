package com.myprojects.routinemanager.util

import android.Manifest
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.annotation.RequiresPermission
import java.time.LocalDateTime
import java.time.ZoneId

/**
 * Класс для планирования уведомлений о задачах с использованием AlarmManager.
 */
class TaskNotificationManager(private val context: Context) {

    /**
     * Запланировать уведомление для задачи с указанным taskId.
     *
     * @param taskId Идентификатор задачи.
     * @param time Время, когда должно сработать уведомление (например, за 5 минут до начала).
     */
    @RequiresPermission(Manifest.permission.SCHEDULE_EXACT_ALARM)
    fun scheduleNotification(taskId: String, time: LocalDateTime) {
        // Получаем AlarmManager из системы.
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        // Создаем Intent, который будет отправлен Receiver'у.
        val intent = Intent(context, TaskNotificationReceiver::class.java).apply {
            putExtra("TASK_ID", taskId)
        }

        // Для возможности планирования нескольких уведомлений (если нужно),
        // можно использовать уникальный requestCode, например, на основе taskId.hashCode().
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            taskId.hashCode(), // уникальный requestCode для каждого taskId
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Конвертируем LocalDateTime в миллисекунды.
        val triggerTime = time.atZone(ZoneId.systemDefault()).toEpochSecond() * 1000

        // Планируем уведомление с точным временем.
        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            triggerTime,
            pendingIntent
        )
    }

    /**
     * Отменить запланированное уведомление для задачи.
     *
     * @param taskId Идентификатор задачи.
     */
    fun cancelNotification(taskId: String) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, TaskNotificationReceiver::class.java).apply {
            putExtra("TASK_ID", taskId)
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            taskId.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pendingIntent)
    }
}
