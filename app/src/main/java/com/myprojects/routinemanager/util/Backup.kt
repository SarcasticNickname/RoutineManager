package com.myprojects.routinemanager.util

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.gson.Gson
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.tasks.await
import com.myprojects.routinemanager.data.repository.TaskRepository
import com.myprojects.routinemanager.data.repository.DayTemplateRepository
import com.myprojects.routinemanager.data.model.BackupData

suspend fun backupAllData(
    taskRepository: TaskRepository,
    dayTemplateRepository: DayTemplateRepository,
    onResult: (Boolean, String) -> Unit
) {
    // Извлекаем задачи из базы
    val tasks = taskRepository.getAllTasks().first()
    // Извлекаем шаблоны дня. Предполагается, что такой метод добавлен:
    val dayTemplates = dayTemplateRepository.getAllDayTemplates().first()

    // Собираем объект бэкапа
    val backupData = BackupData(tasks = tasks, dayTemplates = dayTemplates)

    // Сериализуем объект в JSON с помощью Gson
    val gson = Gson()
    val backupJson = gson.toJson(backupData)

    // Готовим Map, который будем сохранять в Firestore
    val backupMap = hashMapOf(
        "backupDate" to System.currentTimeMillis(),
        "backupJson" to backupJson
    )

    // Получаем текущего пользователя Firebase (автоматически через Google Sign-In)
    val currentUser = FirebaseAuth.getInstance().currentUser
    if (currentUser == null) {
        onResult(false, "Пользователь не авторизован")
        return
    }

    val firestore = FirebaseFirestore.getInstance()

    try {
        // Сохраняем бэкап в коллекции "backups", документ имеет ID равный UID пользователя
        firestore.collection("backups")
            .document(currentUser.uid)
            .set(backupMap)
            .await()
        onResult(true, "Бэкап успешно создан!")
    } catch (e: Exception) {
        e.printStackTrace()
        onResult(false, "Ошибка создания бэкапа: ${e.message}")
    }
}

suspend fun restoreAllData(
    taskRepository: TaskRepository,
    dayTemplateRepository: DayTemplateRepository,
    onResult: (Boolean, String) -> Unit
) {
    val currentUser = FirebaseAuth.getInstance().currentUser
    if (currentUser == null) {
        onResult(false, "Пользователь не авторизован")
        return
    }

    val firestore = FirebaseFirestore.getInstance()

    try {
        val document = firestore.collection("backups")
            .document(currentUser.uid)
            .get()
            .await()

        if (document.exists()) {
            val backupJson = document.getString("backupJson") ?: ""
            if (backupJson.isNotEmpty()) {
                val gson = Gson()
                val backupData = gson.fromJson(backupJson, BackupData::class.java)

                // Вставляем задачи
                backupData.tasks.forEach { task ->
                    taskRepository.insertTask(task)
                }
                // Вставляем dayTemplate. Предполагается, что метод insertTemplate доступен.
                backupData.dayTemplates.forEach { template ->
                    dayTemplateRepository.insertTemplate(template)
                }
                onResult(true, "Восстановление завершено успешно!")
            } else {
                onResult(false, "Бэкап пуст")
            }
        } else {
            onResult(false, "Бэкап не найден")
        }

    } catch (e: Exception) {
        e.printStackTrace()
        onResult(false, "Ошибка восстановления: ${e.message}")
    }
}

