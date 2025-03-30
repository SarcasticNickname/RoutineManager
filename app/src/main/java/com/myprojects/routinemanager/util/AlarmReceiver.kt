package com.myprojects.routinemanager.util

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat

/**
 * BroadcastReceiver, который вызывается при срабатывании AlarmManager.
 * Должен быть прописан в манифесте.
 */
class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val taskId = intent.getStringExtra("TASK_ID") ?: ""
        Log.d("AlarmReceiver", "Будильник сработал для задачи ID=$taskId")

        // Показать уведомление
        val builder = NotificationCompat.Builder(context, "routine_channel")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("Напоминание о задаче")
            .setContentText("Пора выполнить задачу (ID = $taskId)")
            .setPriority(NotificationCompat.PRIORITY_HIGH)



        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return
        }
        NotificationManagerCompat.from(context).notify(taskId.hashCode(), builder.build())
    }
}
