package com.myprojects.routinemanager.ui.screens

import android.app.DatePickerDialog
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*

import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.TextFieldDefaults

import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.wear.compose.material.ContentAlpha
import com.myprojects.routinemanager.data.model.Task
import com.myprojects.routinemanager.data.model.getCategoryColor
import com.myprojects.routinemanager.ui.navigation.NavRoutes // Import NavRoutes
import com.myprojects.routinemanager.ui.viewmodel.TaskViewModel
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun TaskListScreen(
    viewModel: TaskViewModel,
    navController: NavController,
    onAddTaskClick: () -> Unit, // Parameter remains but FAB uses direct navigation
    onTaskClick: (String) -> Unit
) {
    var selectedDate by remember { mutableStateOf(LocalDate.now()) }
    val taskList by viewModel.tasks.collectAsState()
    val context = LocalContext.current
    var showDeleteDialog by remember { mutableStateOf(false) }
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val coroutineScope = rememberCoroutineScope()

    // Состояние для текста в Quick Add Bar
    var quickTaskTitle by remember { mutableStateOf("") }
    val keyboardController = LocalSoftwareKeyboardController.current // Для скрытия клавиатуры

    LaunchedEffect(selectedDate) {
        viewModel.loadTasksFor(selectedDate)
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(modifier = Modifier.width(280.dp)) {
                // Drawer items...
                NavigationDrawerItem(
                    label = { Text("Главная") },
                    selected = true,
                    onClick = { coroutineScope.launch { drawerState.close() } },
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                )
                NavigationDrawerItem(
                    label = { Text("Статистика") },
                    selected = false,
                    onClick = {
                        coroutineScope.launch { drawerState.close() }; navController.navigate("statistics")
                    },
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                )
                NavigationDrawerItem(
                    label = { Text("Шаблон задачи") },
                    selected = false,
                    onClick = {
                        coroutineScope.launch { drawerState.close() }; navController.navigate("task_template")
                    },
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                )
                NavigationDrawerItem(
                    label = { Text("Шаблон дней/пользовательские") },
                    selected = false,
                    onClick = {
                        coroutineScope.launch { drawerState.close() }; navController.navigate("manage_templates")
                    },
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                )
                NavigationDrawerItem(
                    label = { Text("Бэкап задач") },
                    selected = false,
                    onClick = {
                        coroutineScope.launch { drawerState.close() }; navController.navigate("backup")
                    },
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                )
                NavigationDrawerItem(
                    label = { Text("Настройки") },
                    selected = false,
                    onClick = {
                        coroutineScope.launch { drawerState.close() }; navController.navigate("settings")
                    },
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                )
            }
        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Менеджер рутины") },
                    navigationIcon = {
                        IconButton(onClick = { coroutineScope.launch { drawerState.open() } }) {
                            Icon(Icons.Default.Menu, contentDescription = "Открыть меню")
                        }
                    },
                    actions = {
                        IconButton(onClick = { showDeleteDialog = true }) {
                            Icon(Icons.Default.Delete, contentDescription = "Удалить все задачи")
                        }
                        IconButton(onClick = { navController.navigate("day_templates?date=${selectedDate}") }) {
                            Icon(Icons.Default.ViewModule, contentDescription = "Шаблоны дня")
                        }
                    }
                )
            },
            floatingActionButton = {
                FloatingActionButton(onClick = {
                    // Correct navigation route for adding a task
                    navController.navigate(NavRoutes.AddTask.route)
                }) {
                    Icon(Icons.Default.Add, contentDescription = "Добавить задачу")
                }
            },

            bottomBar = {
                QuickAddBottomBar(
                    text = quickTaskTitle,
                    onTextChange = { quickTaskTitle = it },
                    onAddTask = {
                        viewModel.quickAddTask(quickTaskTitle, selectedDate)
                        quickTaskTitle = "" // Очищаем поле
                        keyboardController?.hide() // Скрываем клавиатуру
                    }
                )
            }

        ) { padding ->
            Column(modifier = Modifier.padding(padding)) {
                // Date selection Card
                val isToday = selectedDate == LocalDate.now()
                val locale = Locale("ru")
                val formatter = DateTimeFormatter.ofPattern("EEEE, d MMMM", locale)
                val formattedDate =
                    selectedDate.format(formatter).replaceFirstChar { it.uppercase() }

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(formattedDate, style = MaterialTheme.typography.bodyMedium)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (!isToday) {
                                IconButton(
                                    onClick = { selectedDate = LocalDate.now() },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(
                                        Icons.Default.Restore,
                                        contentDescription = "Сегодня",
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(4.dp))
                            }
                            IconButton(onClick = {
                                DatePickerDialog(
                                    context,
                                    { _, year, month, dayOfMonth ->
                                        selectedDate = LocalDate.of(year, month + 1, dayOfMonth)
                                    },
                                    selectedDate.year,
                                    selectedDate.monthValue - 1,
                                    selectedDate.dayOfMonth
                                ).show()
                            }) {
                                Icon(
                                    Icons.Default.CalendarToday,
                                    contentDescription = "Выбрать дату"
                                )
                            }
                        }
                    }
                }

                // Task List
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(taskList, key = { it.id }) { task ->
                        TaskItem(
                            task = task,
                            onCheck = { viewModel.toggleTaskDone(task) },
                            onEdit = { navController.navigate("edit_task/${task.id}") },
                            onDelete = { viewModel.deleteTask(task) },
                            onLongClick = { onTaskClick(task.id) } // Use parameter here
                        )
                    }
                    item { Spacer(modifier = Modifier.height(80.dp)) } // Spacer for FAB overlap
                }
            }
        }

        // Delete confirmation dialog
        if (showDeleteDialog) {
            AlertDialog(
                onDismissRequest = { showDeleteDialog = false },
                title = { Text("Подтверждение") },
                text = { Text("Вы действительно хотите удалить все задачи за $selectedDate?") },
                confirmButton = {
                    TextButton(onClick = {
                        viewModel.deleteAllTasksForDate(selectedDate)
                        showDeleteDialog = false
                    }) { Text("Удалить") }
                },
                dismissButton = {
                    TextButton(onClick = {
                        showDeleteDialog = false
                    }) { Text("Отмена") }
                }
            )
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuickAddBottomBar(
    text: String,
    onTextChange: (String) -> Unit,
    onAddTask: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shadowElevation = 8.dp // Небольшая тень для выделения
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .navigationBarsPadding(), // Отступ от системных панелей навигации
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = text,
                onValueChange = onTextChange,
                modifier = Modifier.weight(1f),
                placeholder = { Text("Быстро добавить задачу...") },
                singleLine = true,
                keyboardOptions = KeyboardOptions.Default.copy(
                    capitalization = KeyboardCapitalization.Sentences,
                    imeAction = ImeAction.Send // Действие "Отправить" на клавиатуре
                ),
                keyboardActions = KeyboardActions(
                    onSend = {
                        if (text.isNotBlank()) {
                            onAddTask()
                        }
                    }
                )
            )
            IconButton(
                onClick = onAddTask,
                enabled = text.isNotBlank()
            ) {
                Icon(
                    imageVector = Icons.Filled.Send,
                    contentDescription = "Добавить задачу",
                    tint = if (text.isNotBlank()) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(
                        alpha = ContentAlpha.disabled
                    )
                )
            }
        }
    }
}

// TaskItem Composable (remains the same)
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TaskItem(
    task: Task,
    onCheck: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onLongClick: () -> Unit
) {
    val categoryColor = getCategoryColor(task.category)
    var expanded by remember { mutableStateOf(false) }
    // val subtasks = remember { task.subtasks.toMutableStateList() } // Not needed if toggling via ViewModel

    val cardColor by animateColorAsState(
        targetValue = if (task.isDone) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f) // Make completed slightly transparent
        else MaterialTheme.colorScheme.surfaceVariant, // Use surfaceVariant for default
        label = "CardColorAnimation"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp) // Adjusted padding
            .combinedClickable(onClick = { expanded = !expanded }, onLongClick = onLongClick),
        colors = CardDefaults.cardColors(containerColor = cardColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp) // Slightly less elevation
    ) {
        Column {
            Row(modifier = Modifier.height(IntrinsicSize.Min)) {
                Box(
                    modifier = Modifier
                        .width(6.dp)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(topStart = 12.dp, bottomStart = 12.dp))
                        .background(categoryColor)
                )
                Column(
                    modifier = Modifier
                        .padding(horizontal = 12.dp, vertical = 10.dp)
                        .weight(1f)
                ) { // Adjusted padding
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .padding(end = 8.dp)
                        ) { // Add padding to prevent overlap with icons
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.AutoMirrored.Filled.Assignment,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(task.title, style = MaterialTheme.typography.titleMedium)
                            }
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(top = 4.dp)
                            ) {
                                Icon(
                                    Icons.Default.Schedule,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.outline,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    formatTimeRange(
                                        task.startTime,
                                        task.endTime
                                    ), // Use internal version
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.outline
                                )
                            }
                        }
                        // Actions on the right
                        Row(verticalAlignment = Alignment.Top) {
                            var menuExpanded by remember { mutableStateOf(false) }
                            Box { // Box to anchor the dropdown
                                IconButton(
                                    onClick = { menuExpanded = true },
                                    modifier = Modifier.size(36.dp)
                                ) { // Smaller icon button
                                    Icon(Icons.Default.MoreVert, contentDescription = "Меню")
                                }
                                DropdownMenu(
                                    expanded = menuExpanded,
                                    onDismissRequest = { menuExpanded = false }) {
                                    DropdownMenuItem(
                                        text = { Text("Редактировать") },
                                        onClick = { menuExpanded = false; onEdit() })
                                    DropdownMenuItem(
                                        text = { Text("Удалить") },
                                        onClick = { menuExpanded = false; onDelete() })
                                }
                            }
                            Checkbox(
                                checked = task.isDone,
                                onCheckedChange = { onCheck() },
                                modifier = Modifier.size(36.dp)
                            ) // Smaller checkbox area
                        }
                    }
                }
            }
            // Animated section for details
            AnimatedVisibility(visible = expanded) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        // .background(MaterialTheme.colorScheme.surfaceColorAtElevation(1.dp)) // Optional background
                        .padding(
                            start = 18.dp,
                            end = 12.dp,
                            bottom = 10.dp,
                            top = 4.dp
                        ) // Adjusted padding (start = 12 + 6 from colored bar)
                ) {
                    Divider( // Divider moved inside AnimatedVisibility
                        modifier = Modifier
                            .padding(bottom = 8.dp)
                            .fillMaxWidth(),
                        thickness = 1.dp,
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                    )
                    if (!task.description.isNullOrEmpty()) {
                        Row(verticalAlignment = Alignment.Top) { // Align icon to top
                            Icon(
                                Icons.Default.Article,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier
                                    .size(16.dp)
                                    .padding(top = 2.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(task.description, style = MaterialTheme.typography.bodyMedium)
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                    if (task.subtasks.isNotEmpty()) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.List,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Подзадачи:", style = MaterialTheme.typography.labelMedium)
                        }
                        task.subtasks.forEachIndexed { index, subtask ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 0.dp)
                            ) { // Reduced padding
                                Checkbox(
                                    checked = subtask.isDone,
                                    onCheckedChange = null,
                                    modifier = Modifier.size(36.dp)
                                ) // Read-only checkbox in list item
                                Text(subtask.title, style = MaterialTheme.typography.bodyMedium)
                            }
                        }
                    }
                }
            }
        }
    }
}

// Helper function to format time range (made internal to avoid conflicts)
public fun formatTimeRange(start: LocalTime?, end: LocalTime?): String {
    val formatter = DateTimeFormatter.ofPattern("HH:mm")
    return when {
        start != null && end != null -> "${start.format(formatter)} - ${end.format(formatter)}"
        start != null -> "с ${start.format(formatter)}"
        end != null -> "до ${end.format(formatter)}"
        else -> "Время не указано"
    }
}