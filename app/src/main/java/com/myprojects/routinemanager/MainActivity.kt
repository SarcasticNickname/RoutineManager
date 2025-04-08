package com.myprojects.routinemanager

import android.app.AlarmManager
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.navigation.compose.rememberNavController
import com.myprojects.routinemanager.ui.navigation.AppNavGraph
import com.myprojects.routinemanager.ui.theme.RoutineManagerTheme
import com.myprojects.routinemanager.ui.viewmodel.DayTemplateViewModel
import com.myprojects.routinemanager.ui.viewmodel.TaskViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val taskViewModel: TaskViewModel by viewModels()
    private val dayTemplateViewModel: DayTemplateViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Проверяем разрешение на точные будильники (требуется на Android 12+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
            if (!alarmManager.canScheduleExactAlarms()) {
                showExactAlarmPermissionDialog()
            }
        }

        setContent {
            RoutineManagerTheme {
                val navController = rememberNavController()

                AppNavGraph(
                    navController = navController,
                    taskViewModel = taskViewModel,
                    dayTemplateViewModel = dayTemplateViewModel
                )
            }
        }
    }

    private fun showExactAlarmPermissionDialog() {
        AlertDialog.Builder(this)
            .setTitle("Разрешите точные будильники")
            .setMessage(
                "Для корректной работы уведомлений разрешите приложению использовать точные будильники. " +
                        "Перейдите в настройки и включите соответствующий доступ."
            )
            .setPositiveButton("Настройки") { _, _ ->
                val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
                startActivity(intent)
            }
            .setNegativeButton("Отмена", null)
            .show()
    }
}
