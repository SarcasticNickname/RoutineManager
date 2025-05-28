package com.myprojects.routinemanager.util

import android.Manifest
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresPermission
import com.myprojects.routinemanager.util.TaskNotificationReceiver
import java.time.LocalDateTime
import java.time.ZoneId

class TaskNotificationManager(private val context: Context) {

    @RequiresPermission(Manifest.permission.SCHEDULE_EXACT_ALARM)
    fun scheduleNotification(
        taskId: String,
        taskTitle: String,
        taskDescription: String?,
        isEndTimeNotification: Boolean, // Флаг: true - уведомление о конце, false - о начале
        time: LocalDateTime
    ) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        val intent = Intent(context, TaskNotificationReceiver::class.java).apply {
            putExtra(TaskNotificationReceiver.EXTRA_TASK_ID, taskId)
            putExtra(TaskNotificationReceiver.EXTRA_TASK_TITLE, taskTitle)
            putExtra(TaskNotificationReceiver.EXTRA_TASK_DESCRIPTION, taskDescription)
            putExtra(TaskNotificationReceiver.EXTRA_IS_END_TIME, isEndTimeNotification)
        }

        // Уникальный requestCode для уведомлений о начале и конце
        val requestCode = (taskId + if (isEndTimeNotification) "_end" else "_start").hashCode()

        val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        } else {
            PendingIntent.FLAG_UPDATE_CURRENT
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            flags
        )

        val triggerTime = time.atZone(ZoneId.systemDefault()).toEpochSecond() * 1000

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (!alarmManager.canScheduleExactAlarms()) {
                Log.w("TaskNotificationManager", "Cannot schedule exact alarms, permission denied.")
                return
            }
        }

        try {
            // Используем setExactAndAllowWhileIdle для точного срабатывания даже в спящем режиме
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                triggerTime,
                pendingIntent
            )
            Log.d("TaskNotificationManager", "Scheduled notification for task '$taskTitle' ($taskId) at $time (End: $isEndTimeNotification)")
        } catch (se: SecurityException){
            Log.e("TaskNotificationManager", "SecurityException while scheduling alarm.", se)
        } catch (e: Exception) {
            Log.e("TaskNotificationManager", "Exception while scheduling alarm.", e)
        }
    }

    fun cancelNotification(taskId: String, isEndTimeNotification: Boolean) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, TaskNotificationReceiver::class.java)

        val requestCode = (taskId + if (isEndTimeNotification) "_end" else "_start").hashCode()

        val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        } else {
            PendingIntent.FLAG_UPDATE_CURRENT
        }

        // Важно использовать те же параметры Intent (без extras) и requestCode/flags, что и при создании
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            flags
        )
        try {
            alarmManager.cancel(pendingIntent)
            Log.d("TaskNotificationManager", "Cancelled notification for task ID: $taskId (End: $isEndTimeNotification)")
        } catch (e: Exception) {
            Log.e("TaskNotificationManager", "Exception while cancelling notification for task ID: $taskId (End: $isEndTimeNotification)", e)
        }
    }
}