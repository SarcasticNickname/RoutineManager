package com.myprojects.routinemanager.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.myprojects.routinemanager.data.model.DayTemplate
import com.myprojects.routinemanager.data.repository.TaskRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalDate

class DayTemplateViewModel(
    private val repository: TaskRepository
) : ViewModel() {

    private val _weeklyTemplates = MutableStateFlow<List<DayTemplate>>(emptyList())
    val weeklyTemplates: StateFlow<List<DayTemplate>> = _weeklyTemplates.asStateFlow()

    private val _customTemplates = MutableStateFlow<List<DayTemplate>>(emptyList())
    val customTemplates: StateFlow<List<DayTemplate>> = _customTemplates.asStateFlow()

    init {
        viewModelScope.launch {
            repository.getAllDayTemplates().collect { allTemplates ->
                _weeklyTemplates.value = allTemplates.filter { it.isWeekly }
                _customTemplates.value = allTemplates.filter { !it.isWeekly }
            }
        }
    }

    fun addTemplate(template: DayTemplate) {
        viewModelScope.launch {
            repository.saveDayTemplate(template)
        }
    }

    fun deleteTemplate(template: DayTemplate) {
        viewModelScope.launch {
            repository.deleteDayTemplate(template)
        }
    }

    fun applyTemplate(template: DayTemplate, date: LocalDate) {
        viewModelScope.launch {
            repository.applyDayTemplate(template, date)
        }
    }

    suspend fun getTemplateForWeekday(weekday: DayOfWeek): DayTemplate? {
        return repository.getTemplateForWeekday(weekday)
    }
}
