package com.myprojects.routinemanager.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.myprojects.routinemanager.datastore.SettingsDataStore
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    @ApplicationContext context: Context
) : ViewModel() {

    private val settingsDataStore = SettingsDataStore(context)

    // Состояние переключателя уведомлений; используется stateIn для преобразования Flow в StateFlow.
    val notificationsEnabled = settingsDataStore.notificationsEnabledFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    // Состояние интервала уведомлений; значение по умолчанию – 5 минут.
    val notificationOffset = settingsDataStore.notificationOffsetFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 5)

    fun setNotificationsEnabled(enabled: Boolean) {
        viewModelScope.launch {
            settingsDataStore.updateNotificationsEnabled(enabled)
        }
    }

    fun setNotificationOffset(offset: Int) {
        viewModelScope.launch {
            settingsDataStore.updateNotificationOffset(offset)
        }
    }
}
