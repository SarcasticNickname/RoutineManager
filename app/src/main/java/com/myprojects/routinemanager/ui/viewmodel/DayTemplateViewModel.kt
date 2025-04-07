package com.myprojects.routinemanager.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.myprojects.routinemanager.data.model.DayTemplate
import com.myprojects.routinemanager.data.model.TaskTemplate
import com.myprojects.routinemanager.data.repository.DayTemplateRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalDate
import javax.inject.Inject
import android.util.Log
import com.myprojects.routinemanager.data.model.DayTemplateWithTasks

@HiltViewModel
class DayTemplateViewModel @Inject constructor(
    private val repository: DayTemplateRepository
) : ViewModel() {

    private val _weeklyTemplates = MutableStateFlow<List<DayTemplateWithTasks>>(emptyList())
    val weeklyTemplates: StateFlow<List<DayTemplateWithTasks>> = _weeklyTemplates.asStateFlow()

    private val _customTemplates = MutableStateFlow<List<DayTemplateWithTasks>>(emptyList())
    val customTemplates: StateFlow<List<DayTemplateWithTasks>> = _customTemplates.asStateFlow()

    init {
        viewModelScope.launch {
            repository.getAllTemplates().collect { allTemplates ->
                Log.d("DayTemplateVM", "🔥 Всего шаблонов в базе: ${allTemplates.size}")
                allTemplates.forEach { template ->
                    Log.d("DayTemplateVM", "📋 name=${template.template.name}, isWeekly=${template.template.isWeekly}, weekday=${template.template.weekday}, tasks=${template.taskTemplates.size}")
                }

                _weeklyTemplates.value = allTemplates.filter { it.template.isWeekly }
                _customTemplates.value = allTemplates.filter { !it.template.isWeekly }
            }
        }
    }

    fun addTemplate(template: DayTemplate, tasks: List<TaskTemplate>) {
        val fullTemplate = template.copy(taskTemplates = tasks)
        viewModelScope.launch {
            repository.insertTemplate(fullTemplate)
        }
    }

    fun deleteTemplate(template: DayTemplate) {
        viewModelScope.launch {
            repository.deleteTemplate(template)
        }
    }

    fun deleteTaskTemplate(taskTemplate: TaskTemplate) {
        viewModelScope.launch {
            repository.deleteTaskTemplate(taskTemplate)
        }
    }

    fun applyTemplate(templateWithTasks: DayTemplateWithTasks, date: LocalDate) {
        viewModelScope.launch {
            repository.applyDayTemplate(templateWithTasks, date)
        }
    }

    suspend fun getTemplateForWeekday(weekday: DayOfWeek): DayTemplateWithTasks? {
        return repository.getTemplateForWeekday(weekday)
    }

    fun getTemplateById(id: String): Flow<DayTemplateWithTasks?> {
        return repository.getTemplateById(id)
    }

    fun addTaskToTemplate(taskTemplate: TaskTemplate) {
        viewModelScope.launch {
            repository.insertTaskTemplate(taskTemplate)
        }
    }
}
