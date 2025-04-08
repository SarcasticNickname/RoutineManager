package com.myprojects.routinemanager.datastore

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

// Экстеншен для создания DataStore в контексте
val Context.dataStore by preferencesDataStore(name = "user_settings")

object SettingsPreferencesKeys {
    val NOTIFICATIONS_ENABLED = booleanPreferencesKey("notifications_enabled")
    val NOTIFICATION_OFFSET = intPreferencesKey("notification_offset")
}

class SettingsDataStore(private val context: Context) {

    // Читаем состояние уведомлений; значение по умолчанию – включены (true)
    val notificationsEnabledFlow: Flow<Boolean> = context.dataStore.data
        .map { preferences -> preferences[SettingsPreferencesKeys.NOTIFICATIONS_ENABLED] ?: true }

    // Читаем интервал уведомлений; значение по умолчанию – 5 минут
    val notificationOffsetFlow: Flow<Int> = context.dataStore.data
        .map { preferences -> preferences[SettingsPreferencesKeys.NOTIFICATION_OFFSET] ?: 5 }

    // Обновление состояния уведомлений
    suspend fun updateNotificationsEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[SettingsPreferencesKeys.NOTIFICATIONS_ENABLED] = enabled
        }
    }

    // Обновление интервала уведомлений
    suspend fun updateNotificationOffset(offset: Int) {
        context.dataStore.edit { preferences ->
            preferences[SettingsPreferencesKeys.NOTIFICATION_OFFSET] = offset
        }
    }
}
