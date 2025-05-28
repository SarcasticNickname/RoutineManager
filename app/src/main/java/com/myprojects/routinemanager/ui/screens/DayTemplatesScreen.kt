package com.myprojects.routinemanager.ui.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.ListAlt
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.myprojects.routinemanager.data.model.DayTemplateWithTasks
import com.myprojects.routinemanager.ui.viewmodel.DayTemplateViewModel
import com.myprojects.routinemanager.ui.viewmodel.TaskViewModel // Import TaskViewModel
import kotlinx.coroutines.flow.first // Import first() for Flow
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun DayTemplatesScreen(
    taskViewModel: TaskViewModel, // TaskViewModel to check for existing tasks
    dayTemplateViewModel: DayTemplateViewModel,
    navController: NavController,
    selectedDateForApply: LocalDate?, // Date to apply the template to
    onApplyTemplate: (DayTemplateWithTasks) -> Unit, // Callback might be less needed now
    onOpenDetails: (DayTemplateWithTasks) -> Unit,
    onCreateCustomTemplate: () -> Unit,
    onDeleteTemplate: (DayTemplateWithTasks) -> Unit,
    showApplyButton: Boolean = true
) {
    val weekly by dayTemplateViewModel.weeklyTemplates.collectAsState()
    val custom by dayTemplateViewModel.customTemplates.collectAsState()
    var selectedTab by rememberSaveable { mutableStateOf(0) }

    // State for the confirmation dialog
    var showApplyDialog by remember { mutableStateOf(false) }
    var templateToApply by remember { mutableStateOf<DayTemplateWithTasks?>(null) }

    // Fetch tasks for the selected date to check for conflicts
    // Use produceState for a simple way to fetch data once based on selectedDateForApply
    val tasksForSelectedDate by produceState<List<com.myprojects.routinemanager.data.model.Task>?>(
        initialValue = null, // Start with null to indicate loading/no date
        key1 = selectedDateForApply // Re-trigger if the date changes
    ) {
        value = if (selectedDateForApply != null) {
            // Fetch tasks directly from repository (can cause recomposition if tasks change)
            try {
                taskViewModel.taskRepository.getTasksForDate(selectedDateForApply).first()
            } catch (e: Exception){
                // Handle potential errors during fetch
                emptyList()
            }
        } else {
            emptyList() // No date selected, no tasks to check
        }
    }

    // Action to handle applying the template, including the confirmation check
    val applyTemplateAction: (DayTemplateWithTasks) -> Unit = { template ->
        if (selectedDateForApply != null) {
            if (tasksForSelectedDate?.isNotEmpty() == true) {
                // Tasks exist, show confirmation dialog
                templateToApply = template
                showApplyDialog = true
            } else {
                // No tasks exist, apply template directly
                dayTemplateViewModel.applyTemplate(template, selectedDateForApply)
                taskViewModel.loadTasksFor(selectedDateForApply) // Refresh tasks on main screen
                navController.popBackStack() // Go back
            }
        }
    }

    Scaffold(
        floatingActionButton = {
            if (selectedTab == 1) { // Show FAB only on "Custom" tab
                FloatingActionButton(onClick = onCreateCustomTemplate) {
                    Icon(Icons.Default.Add, contentDescription = "Создать шаблон")
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            TabRow(selectedTabIndex = selectedTab) {
                Tab( selected = selectedTab == 0, onClick = { selectedTab = 0 }, text = { Text("По дням недели") } )
                Tab( selected = selectedTab == 1, onClick = { selectedTab = 1 }, text = { Text("Пользовательские") } )
            }

            val contentModifier = Modifier.fillMaxSize().padding(8.dp)

            when (selectedTab) {
                0 -> { // Weekly Templates Grid
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        modifier = contentModifier,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(weekly, key = { it.template.id }) { templateWithTasks ->
                            TemplateGridCard(
                                template = templateWithTasks,
                                onApply = applyTemplateAction, // Use the action with check
                                onDetails = onOpenDetails,
                                showApplyButton = showApplyButton,
                                modifier = Modifier.animateItemPlacement()
                            )
                        }
                    }
                }

                1 -> { // Custom Templates List
                    LazyColumn(
                        modifier = contentModifier,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(custom, key = { it.template.id }) { templateWithTasks ->
                            TemplateListCard(
                                template = templateWithTasks,
                                onApply = applyTemplateAction, // Use the action with check
                                onOpenDetails = onOpenDetails,
                                onDelete = onDeleteTemplate,
                                showApplyButton = showApplyButton,
                                modifier = Modifier.animateItemPlacement()
                            )
                        }
                    }
                }
            }
        }
    }

    // Confirmation Dialog
    if (showApplyDialog && templateToApply != null && selectedDateForApply != null) {
        val dateText = remember(selectedDateForApply) {
            selectedDateForApply.format(DateTimeFormatter.ofPattern("d MMMM yyyy"))
        }
        AlertDialog(
            onDismissRequest = {
                showApplyDialog = false
                templateToApply = null
            },
            title = { Text("Применить шаблон?") },
            text = { Text("Применение шаблона \"${templateToApply?.template?.name}\" удалит все существующие задачи на $dateText. Продолжить?") },
            confirmButton = {
                TextButton(onClick = {
                    templateToApply?.let { template ->
                        // 1. Delete existing tasks for the date
                        taskViewModel.deleteAllTasksForDate(selectedDateForApply)
                        // 2. Apply the new template
                        dayTemplateViewModel.applyTemplate(template, selectedDateForApply)
                        // 3. Ensure task list is refreshed on the main screen
                        // loadTasksFor is called again inside deleteAllTasksForDate and applyTemplate should trigger recomposition
                        // Optionally force reload if needed:
                        // taskViewModel.loadTasksFor(selectedDateForApply)
                    }
                    showApplyDialog = false
                    templateToApply = null
                    navController.popBackStack() // Go back after applying
                }) {
                    Text("Удалить и применить")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showApplyDialog = false
                    templateToApply = null
                }) {
                    Text("Отмена")
                }
            }
        )
    }
}

// Grid Card Composable (structure remains the same, uses onApply passed in)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TemplateGridCard(
    template: DayTemplateWithTasks,
    onApply: (DayTemplateWithTasks) -> Unit,
    onDetails: (DayTemplateWithTasks) -> Unit,
    showApplyButton: Boolean,
    modifier: Modifier = Modifier
) {
    val data = template.template
    val tasks = template.taskTemplates

    ElevatedCard(
        onClick = { onDetails(template) },
        modifier = modifier.fillMaxWidth().aspectRatio(1f)
    ) {
        Column(
            modifier = Modifier.padding(12.dp).fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Icon( Icons.Default.CalendarToday, contentDescription = null, tint = MaterialTheme.colorScheme.secondary )
                Spacer(modifier = Modifier.height(8.dp))
                Text( data.name, style = MaterialTheme.typography.titleMedium, maxLines = 1, overflow = TextOverflow.Ellipsis )
                Spacer(modifier = Modifier.height(4.dp))
                Text( text = if (tasks.isNotEmpty()) "Задач: ${tasks.size}" else "Нет задач",
                    style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant )
            }
            if (showApplyButton) {
                Row( modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End ) {
                    TextButton(onClick = { onApply(template) }) { Text("Применить") }
                }
            } else {
                Spacer(modifier = Modifier.height(48.dp)) // Placeholder for button height
            }
        }
    }
}

// List Card Composable (structure remains the same, uses onApply passed in)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TemplateListCard(
    template: DayTemplateWithTasks,
    onApply: (DayTemplateWithTasks) -> Unit,
    onOpenDetails: (DayTemplateWithTasks) -> Unit,
    onDelete: (DayTemplateWithTasks) -> Unit,
    showApplyButton: Boolean,
    modifier: Modifier = Modifier
) {
    val data = template.template
    val tasks = template.taskTemplates

    OutlinedCard(
        onClick = { onOpenDetails(template) },
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row( modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween ) {
                Text( data.name, style = MaterialTheme.typography.titleMedium, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f, fill = false) )
                IconButton(onClick = { onDelete(template) }, modifier = Modifier.size(36.dp)) {
                    Icon( Icons.Default.DeleteOutline, contentDescription = "Удалить шаблон", tint = MaterialTheme.colorScheme.error )
                }
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon( Icons.Default.ListAlt, contentDescription = null, modifier = Modifier.size(18.dp), tint = MaterialTheme.colorScheme.secondary )
                Spacer(Modifier.width(8.dp))
                Text( text = if (tasks.isNotEmpty()) "Задач: ${tasks.size}" else "Нет задач",
                    style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant )
            }
            if (showApplyButton) {
                Spacer(modifier = Modifier.height(4.dp))
                Row( modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End ) {
                    TextButton(onClick = { onApply(template) }) { Text("Применить") }
                }
            }
        }
    }
}