package com.myprojects.routinemanager

import android.Manifest // <<<--- Добавить импорт
import android.app.AlarmManager
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager // <<<--- Добавить импорт
import android.net.Uri // <<<--- Добавить импорт
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts // <<<--- Добавить импорт
import androidx.activity.viewModels
import androidx.core.content.ContextCompat // <<<--- Добавить импорт
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

    // --- НОВОЕ: Регистрация лаунчера для запроса разрешений ---
    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (isGranted) {
                // Разрешение на уведомления получено
                // Можно ничего не делать дополнительно или показать Toast
            } else {
                // Разрешение на уведомления НЕ получено
                // Можно показать Snackbar/Toast, объясняющий, что уведомления не будут работать
                // или кнопку для перехода в настройки приложения.
                showNotificationsPermissionDeniedDialog()
            }
        }
    // --- КОНЕЦ НОВОГО ---

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // --- НОВОЕ: Запрос разрешений ---
        checkAndRequestPermissions()
        // --- КОНЕЦ НОВОГО ---

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

    // --- НОВАЯ ФУНКЦИЯ: Проверка и запрос разрешений ---
    private fun checkAndRequestPermissions() {
        // 1. Проверка и запрос SCHEDULE_EXACT_ALARM (для Android 12+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
            if (!alarmManager.canScheduleExactAlarms()) {
                showExactAlarmPermissionDialog() // Показываем диалог с переходом в настройки
            }
        }

        // 2. Проверка и запрос POST_NOTIFICATIONS (для Android 13+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) { // TIRAMISU = API 33
            when {
                ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED -> {
                    // Разрешение уже есть
                }
                shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS) -> {
                    // Пользователь уже отклонял запрос. Показать объяснение перед повторным запросом.
                    // (Опционально, можно просто запросить еще раз)
                    showNotificationsPermissionRationaleDialog()
                }
                else -> {
                    // Запрашиваем разрешение
                    requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            }
        }
    }
    // --- КОНЕЦ НОВОЙ ФУНКЦИИ ---


    // Диалог для перехода в настройки SCHEDULE_EXACT_ALARM (оставляем как было)
    private fun showExactAlarmPermissionDialog() {
        AlertDialog.Builder(this)
            .setTitle("Разрешите точные будильники")
            .setMessage(
                "Для корректной работы уведомлений по времени разрешите приложению использовать точные будильники. " +
                        "Нажмите 'Настройки' и включите соответствующий доступ для RoutineManager."
            )
            .setPositiveButton("Настройки") { _, _ ->
                // Открываем настройки конкретно для нашего приложения, если возможно
                val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
                // Опционально: можно добавить Uri для нацеливания на ваше приложение
                // intent.data = Uri.fromParts("package", packageName, null)
                try {
                    startActivity(intent)
                } catch (e: Exception) {
                    // На случай, если Intent не может быть обработан
                    e.printStackTrace()
                    // Можно открыть общие настройки приложения
                    val fallbackIntent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                    fallbackIntent.data = Uri.fromParts("package", packageName, null)
                    startActivity(fallbackIntent)
                }
            }
            .setNegativeButton("Отмена", null)
            .setCancelable(false) // Важно, чтобы пользователь сделал выбор
            .show()
    }

    // --- НОВЫЙ ДИАЛОГ: Объяснение, зачем нужны уведомления (если пользователь отклонил ранее) ---
    private fun showNotificationsPermissionRationaleDialog() {
        AlertDialog.Builder(this)
            .setTitle("Нужны уведомления")
            .setMessage("Чтобы напоминать вам о задачах вовремя, приложению нужно разрешение на отправку уведомлений. Пожалуйста, разрешите его при следующем запросе.")
            .setPositiveButton("Понятно") { _, _ ->
                // Повторно запрашиваем разрешение после объяснения
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            }
            .setNegativeButton("Отмена", null)
            .show()
    }
    // --- КОНЕЦ НОВОГО ДИАЛОГА ---

    // --- НОВЫЙ ДИАЛОГ: Сообщение, если пользователь окончательно запретил уведомления ---
    private fun showNotificationsPermissionDeniedDialog() {
        AlertDialog.Builder(this)
            .setTitle("Уведомления отключены")
            .setMessage("Вы запретили отправку уведомлений. Напоминания о задачах работать не будут. Вы можете включить их позже в настройках приложения.")
            .setPositiveButton("Настройки") { _, _ ->
                // Отправляем пользователя в системные настройки приложения
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                intent.data = Uri.fromParts("package", packageName, null)
                startActivity(intent)
            }
            .setNegativeButton("Понятно", null)
            .show()
    }
    // --- КОНЕЦ НОВОГО ДИАЛОГА ---
}