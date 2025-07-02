package com.myprojects.routinemanager.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.myprojects.routinemanager.data.model.DayTemplate
import com.myprojects.routinemanager.data.model.DayTemplateWithTasks
import com.myprojects.routinemanager.data.model.Subtask
import com.myprojects.routinemanager.data.model.TaskCategory
import com.myprojects.routinemanager.data.model.TaskTemplate
import com.myprojects.routinemanager.data.repository.DayTemplateRepository
import com.myprojects.routinemanager.data.room.STANDALONE_TASK_TEMPLATE_HOLDER_ID
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class DayTemplateViewModel @Inject constructor(
    val repository: DayTemplateRepository
) : ViewModel() {

    // Internal flow holding all templates with tasks, used for filtering weekly/custom
    private val _allTemplatesFlow = MutableStateFlow<List<DayTemplateWithTasks>>(emptyList())

    // Public flows for UI (Weekly/Custom Day Templates)
    private val _weeklyTemplates = MutableStateFlow<List<DayTemplateWithTasks>>(emptyList())
    val weeklyTemplates: StateFlow<List<DayTemplateWithTasks>> = _weeklyTemplates.asStateFlow()

    private val _customTemplates = MutableStateFlow<List<DayTemplateWithTasks>>(emptyList())
    val customTemplates: StateFlow<List<DayTemplateWithTasks>> = _customTemplates.asStateFlow()

    // NEW: Direct StateFlow for standalone TaskTemplates using the new repository method
    val standaloneTaskTemplates: StateFlow<List<TaskTemplate>> =
        repository.getTaskTemplatesStream(STANDALONE_TASK_TEMPLATE_HOLDER_ID)
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000L), // Start collecting when UI observes
                initialValue = emptyList() // Initial empty list
            )

    init {
        viewModelScope.launch {
            // Collect DayTemplateWithTasks for weekly/custom filtering using collectLatest
            repository.getAllTemplates().collectLatest { allTemplates ->
                Log.d(
                    "DayTemplateVM",
                    "Updating DayTemplateWithTasks flow, count: ${allTemplates.size}"
                )
                _allTemplatesFlow.value = allTemplates // Update the internal holder flow
                // Update public filtered flows
                _weeklyTemplates.value = allTemplates.filter { it.template.isWeekly }
                _customTemplates.value = allTemplates.filter {
                    !it.template.isWeekly && it.template.id != STANDALONE_TASK_TEMPLATE_HOLDER_ID
                }
            }
        }
        // standaloneTaskTemplates will be collected automatically by stateIn when needed
    }

    // Adds a new DayTemplate
    fun addTemplate(template: DayTemplate) {
        viewModelScope.launch {
            try {
                repository.insertTemplate(template.copy(taskTemplates = emptyList()))
                // Flow updates handled by collectLatest in init
            } catch (e: Exception) {
                Log.e("DayTemplateVM", "Error adding DayTemplate", e)
            }
        }
    }

    // Deletes a DayTemplate
    fun deleteTemplate(template: DayTemplate) {
        viewModelScope.launch {
            try {
                // TODO: Implement deletion of associated TaskTemplates if needed
                repository.deleteTemplate(template)
                // Flow updates handled by collectLatest
            } catch (e: Exception) {
                Log.e("DayTemplateVM", "Error deleting DayTemplate", e)
            }
        }
    }

    // Deletes a specific TaskTemplate
    fun deleteTaskTemplate(taskTemplate: TaskTemplate) {
        viewModelScope.launch {
            try {
                repository.deleteTaskTemplate(taskTemplate)
                // standaloneTaskTemplates flow will update automatically
            } catch (e: Exception) {
                Log.e("DayTemplateVM", "Error deleting TaskTemplate", e)
            }
        }
    }

    // Applies a DayTemplate to a date
    fun applyTemplate(templateWithTasks: DayTemplateWithTasks, date: LocalDate) {
        viewModelScope.launch {
            try {
                repository.applyDayTemplate(templateWithTasks, date)
            } catch (e: Exception) {
                Log.e("DayTemplateVM", "Error applying DayTemplate", e)
            }
        }
    }

    // Gets a specific DayTemplate for a weekday
    suspend fun getTemplateForWeekday(weekday: DayOfWeek): DayTemplateWithTasks? {
        return repository.getTemplateForWeekday(weekday)
    }

    // Gets a specific DayTemplateWithTasks by ID
    fun getTemplateById(id: String): Flow<DayTemplateWithTasks?> {
        return repository.getTemplateById(id)
    }

    // Adds a TaskTemplate to an existing DayTemplate
    fun addTaskTemplateToDayTemplate(templateId: String, taskTemplate: TaskTemplate) {
        viewModelScope.launch {
            try {
                val templateToInsert = taskTemplate.copy(
                    id = UUID.randomUUID().toString(), // Ensure unique ID
                    templateId = templateId // Link to parent DayTemplate
                )
                repository.insertTaskTemplate(templateToInsert)
                // _allTemplatesFlow updates via collectLatest if templateId is visible
            } catch (e: Exception) {
                Log.e("DayTemplateVM", "Error adding TaskTemplate to DayTemplate", e)
            }
        }
    }

    // Creates a standalone TaskTemplate (linked to the hidden holder)
    fun createStandaloneTaskTemplate(
        title: String, description: String?, category: TaskCategory,
        startTime: LocalTime?, endTime: LocalTime?, subtasks: List<Subtask>
    ) {
        viewModelScope.launch {
            val newTaskTemplate = TaskTemplate(
                id = UUID.randomUUID().toString(),
                templateId = STANDALONE_TASK_TEMPLATE_HOLDER_ID, // Link to holder
                defaultTitle = title, defaultDescription = description,
                defaultStartTime = startTime, defaultEndTime = endTime,
                category = category, subtasks = subtasks
            )
            try {
                repository.insertTaskTemplate(newTaskTemplate)
                // standaloneTaskTemplates flow will update automatically
            } catch (e: Exception) {
                Log.e("DayTemplateVM", "Error creating standalone task template", e)
            }
        }
    }


    fun addTaskToDayTemplate(
        templateId: String, title: String, description: String?,
        category: TaskCategory, startTime: LocalTime?, endTime: LocalTime?,
        subtasks: List<Subtask>
    ) {
        viewModelScope.launch {
            val taskTemplate = TaskTemplate(
                id = UUID.randomUUID().toString(),
                templateId = templateId,
                defaultTitle = title, defaultDescription = description, category = category,
                defaultStartTime = startTime, defaultEndTime = endTime,
                subtasks = subtasks // <-- Теперь типы совпадают, преобразование не нужно
            )
            try {
                repository.insertTaskTemplate(taskTemplate)
            } catch (e: Exception) {
                Log.e("DayTemplateViewModel", "Error adding task template to day template", e)
            }
        }
    }

    fun addTaskToDayTemplate(templateId: String, template: TaskTemplate) {
        viewModelScope.launch {
            try {
                repository.insertTaskTemplate(template.copy(templateId = templateId))
            } catch (e: Exception) {
                Log.e(
                    "TaskViewModel",
                    "Error adding task template object to day template via repository",
                    e
                )
            }
        }
    }

}