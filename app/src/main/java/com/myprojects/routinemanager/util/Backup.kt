package com.myprojects.routinemanager.util

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.gson.Gson
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.tasks.await
import com.myprojects.routinemanager.data.repository.TaskRepository
import com.myprojects.routinemanager.data.repository.DayTemplateRepository
import com.myprojects.routinemanager.data.model.BackupData
import com.myprojects.routinemanager.data.model.DayTemplate // Импортируем DayTemplate
import java.util.UUID

suspend fun backupAllData(
    taskRepository: TaskRepository,
    dayTemplateRepository: DayTemplateRepository,
    onResult: (Boolean, String) -> Unit
) {
    try {
        // 1. Извлекаем обычные задачи
        val tasks = taskRepository.getAllTasks().first()

        // --- ИЗМЕНЕНИЕ: Получаем DayTemplate ВМЕСТЕ с TaskTemplate ---
        // Используем метод, возвращающий Flow<List<DayTemplateWithTasks>>
        val dayTemplatesWithTasks = dayTemplateRepository.getAllTemplates().first()

        // Преобразуем List<DayTemplateWithTasks> в List<DayTemplate>,
        // где у каждого DayTemplate будет заполненное поле taskTemplates
        val dayTemplatesForBackup: List<DayTemplate> = dayTemplatesWithTasks.map { dtw ->
            // Создаем копию DayTemplate из DayTemplateWithTasks,
            // явно присваивая ему список TaskTemplate из связанной сущности.
            // Room сам не заполняет это поле при простом SELECT * FROM day_templates.
            dtw.template.copy(taskTemplates = dtw.taskTemplates)
        }
        // --- КОНЕЦ ИЗМЕНЕНИЯ ---

        // 2. Собираем объект бэкапа с правильными dayTemplates
        val backupData = BackupData(tasks = tasks, dayTemplates = dayTemplatesForBackup)

        // 3. Сериализуем объект в JSON
        val gson = Gson()
        val backupJson = gson.toJson(backupData)

        // 4. Готовим Map для Firestore
        val backupMap = hashMapOf(
            "backupDate" to System.currentTimeMillis(),
            "backupJson" to backupJson
        )

        // 5. Получаем пользователя Firebase
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser == null) {
            onResult(false, "Пользователь не авторизован")
            return
        }

        // 6. Сохраняем в Firestore
        val firestore = FirebaseFirestore.getInstance()
        firestore.collection("backups")
            .document(currentUser.uid)
            .set(backupMap)
            .await()

        Log.d("BackupUtil", "Backup created successfully. Tasks: ${tasks.size}, DayTemplates: ${dayTemplatesForBackup.size}")
        dayTemplatesForBackup.forEach { dt ->
            Log.d("BackupUtil", "  Template '${dt.name}' has ${dt.taskTemplates.size} task templates.")
        }

        onResult(true, "Бэкап успешно создан!")

    } catch (e: Exception) {
        Log.e("BackupUtil", "Error during backup process", e)
        onResult(false, "Ошибка создания бэкапа: ${e.message}")
    }
}

// Функция restoreAllData остается без изменений, так как она уже
// корректно обрабатывает вложенные taskTemplates, ЕСЛИ они есть в backupData
suspend fun restoreAllData(
    taskRepository: TaskRepository,
    dayTemplateRepository: DayTemplateRepository,
    onResult: (Boolean, String) -> Unit
) {
    // ... (код restoreAllData как в вашем последнем рабочем варианте) ...
    // Важно, что внутри цикла по backupData.dayTemplates есть:
    // template.taskTemplates.forEach { taskTemplate ->
    //     dayTemplateRepository.insertTaskTemplate(...)
    // }
    // Этот код теперь будет работать, так как taskTemplates будут не пустыми.

    val currentUser = FirebaseAuth.getInstance().currentUser
    if (currentUser == null) { /* ... обработка ошибки ... */ return }
    val firestore = FirebaseFirestore.getInstance()
    var skippedTasks = 0
    var skippedDayTemplates = 0
    var skippedTaskTemplates = 0
    var successfullyRestored = true

    try {
        val document = firestore.collection("backups").document(currentUser.uid).get().await()
        if (!document.exists()) { /* ... обработка ошибки ... */ return }
        val backupJson = document.getString("backupJson") ?: ""
        if (backupJson.isEmpty()) { /* ... обработка ошибки ... */ return }

        val gson = Gson()
        val backupData: BackupData = try {
            gson.fromJson(backupJson, BackupData::class.java)
        } catch (e: Exception) { /* ... обработка ошибки ... */ return }

        // Очистка базы
        try {
            taskRepository.deleteAllTasks()
            dayTemplateRepository.deleteAllTaskTemplates()
            dayTemplateRepository.deleteAllDayTemplates()
        } catch(e: Exception) { /* ... обработка ошибки ... */ return }

        // Восстановление DayTemplates и вложенных TaskTemplates
        backupData.dayTemplates.forEach { template ->
            try {
                val dayTemplateToInsert = template.copy(id = if (template.id.isNullOrBlank()) UUID.randomUUID().toString() else template.id)
                dayTemplateRepository.insertTemplate(dayTemplateToInsert) // Вставляем сам DayTemplate

                Log.d("RestoreUtil", "Restoring DayTemplate '${template.name}', TaskTemplates found: ${template.taskTemplates.size}")

                template.taskTemplates.forEach { taskTemplate -> // Итерируем по вложенным TaskTemplate
                    try {
                        val taskTemplateToInsert = taskTemplate.copy(
                            id = UUID.randomUUID().toString(), // Новый ID для TaskTemplate
                            templateId = dayTemplateToInsert.id // Связь с восстановленным DayTemplate
                        )
                        dayTemplateRepository.insertTaskTemplate(taskTemplateToInsert) // Вставляем TaskTemplate
                    } catch (e: Exception) {
                        Log.e("RestoreUtil", "Error restoring task template: ${taskTemplate.defaultTitle}", e)
                        skippedTaskTemplates++
                    }
                }
            } catch (e: Exception) {
                Log.e("RestoreUtil", "Error restoring day template: ${template.name}", e)
                skippedDayTemplates++
            }
        }

        // Восстановление Task
        backupData.tasks.forEach { task ->
            try {
                val taskToInsert = task.copy(id = UUID.randomUUID().toString())
                if (taskToInsert.date != null) { // Проверка на null дату
                    taskRepository.insertTask(taskToInsert)
                } else {
                    Log.w("RestoreUtil", "Skipping task with null date: ${task.title}")
                    skippedTasks++
                }
            } catch (e: Exception) {
                Log.e("RestoreUtil", "Error restoring task: ${task.title}", e)
                skippedTasks++
            }
        }

        // Финальный результат
        val totalSkipped = skippedTasks + skippedDayTemplates + skippedTaskTemplates
        if (totalSkipped > 0) {
            onResult(successfullyRestored, "Восстановление завершено. Пропущено записей: $totalSkipped.")
        } else {
            onResult(true, "Восстановление завершено успешно!")
        }

    } catch (e: Exception) {
        Log.e("RestoreUtil", "General error during restore process", e)
        onResult(false, "Ошибка восстановления: ${e.message}")
    }
}