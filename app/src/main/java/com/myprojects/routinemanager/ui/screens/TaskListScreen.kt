package com.myprojects.routinemanager.ui.screens

import android.app.DatePickerDialog
import android.util.Log
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.myprojects.routinemanager.data.model.Subtask
import com.myprojects.routinemanager.data.model.Task
import com.myprojects.routinemanager.ui.navigation.NavRoutes
import com.myprojects.routinemanager.ui.screens.components.QuickAddBottomBar
import com.myprojects.routinemanager.ui.screens.components.SubtaskEditorContent
import com.myprojects.routinemanager.ui.screens.components.TaskItem
import com.myprojects.routinemanager.ui.viewmodel.TaskViewModel
import kotlinx.coroutines.launch
import org.burnoutcrew.reorderable.ReorderableItem
import org.burnoutcrew.reorderable.detectReorderAfterLongPress
import org.burnoutcrew.reorderable.rememberReorderableLazyListState
import org.burnoutcrew.reorderable.reorderable
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun TaskListScreen(
    viewModel: TaskViewModel,
    navController: NavController,
) {
    var selectedDate by remember { mutableStateOf(LocalDate.now()) }
    val taskListState by viewModel.tasks.collectAsState()
    val context = LocalContext.current
    var showDeleteDialog by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var taskToEditSubtasks by remember { mutableStateOf<Task?>(null) }
    var currentSubtasksList = remember { mutableStateListOf<Subtask>() }
    var newSubtaskTextInSheet by remember { mutableStateOf("") }

    val isSubtaskSheetVisible = sheetState.isVisible && taskToEditSubtasks != null

    LaunchedEffect(selectedDate) {
        viewModel.loadTasksFor(selectedDate)
    }

    val reorderableState = rememberReorderableLazyListState(
        onMove = { from, to ->
            val currentList = taskListState.toMutableList()

            // Проверка 1: Если элемент не сдвинулся или список слишком мал
            if (from.index == to.index || currentList.size <= 1) {
                return@rememberReorderableLazyListState
            }

            // Проверка 2: Валидность индекса from для удаления
            if (from.index < 0 || from.index >= currentList.size) {
                Log.e(
                    "ReorderFix",
                    "Invalid from.index: ${from.index} for list size: ${currentList.size}"
                )
                return@rememberReorderableLazyListState
            }
            val item = currentList.removeAt(from.index)

            // Проверка 3: Валидность индекса to для добавления
            // to.index может быть равен currentList.size (добавление в конец нового списка)
            // to.index может быть -1, если тянем первый элемент вверх за пределы (библиотека может так вернуть)
            val targetInsertIndex = when {
                to.index < 0 -> 0 // Вставить в начало
                to.index > currentList.size -> currentList.size // Вставить в конец
                else -> to.index
            }

            currentList.add(targetInsertIndex, item)

            val tasksWithNewOrder = currentList.mapIndexed { index, task ->
                task.copy(displayOrder = index)
            }
            viewModel.updateTaskOrder(tasksWithNewOrder)
        }
    )

    Box(modifier = Modifier.fillMaxSize()) {
        val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)

        ModalNavigationDrawer(
            drawerState = drawerState,
            drawerContent = {
                ModalDrawerSheet(modifier = Modifier.width(280.dp)) {
                    NavigationDrawerItem(
                        label = { Text("Главная") },
                        selected = true,
                        onClick = { scope.launch { drawerState.close() } },
                        modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                    )
                    NavigationDrawerItem(
                        label = { Text("Статистика") },
                        selected = false,
                        onClick = { scope.launch { drawerState.close() }; navController.navigate("statistics") },
                        modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                    )
                    NavigationDrawerItem(
                        label = { Text("Шаблон задачи") },
                        selected = false,
                        onClick = { scope.launch { drawerState.close() }; navController.navigate("task_template") },
                        modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                    )
                    NavigationDrawerItem(
                        label = { Text("Шаблон дней/пользовательские") },
                        selected = false,
                        onClick = { scope.launch { drawerState.close() }; navController.navigate("manage_templates") },
                        modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                    )
                    NavigationDrawerItem(
                        label = { Text("Бэкап задач") },
                        selected = false,
                        onClick = { scope.launch { drawerState.close() }; navController.navigate("backup") },
                        modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                    )
                    NavigationDrawerItem(
                        label = { Text("Настройки") },
                        selected = false,
                        onClick = { scope.launch { drawerState.close() }; navController.navigate("settings") },
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
                            IconButton(onClick = { scope.launch { drawerState.open() } }) {
                                Icon(
                                    Icons.Default.Menu,
                                    contentDescription = "Открыть меню"
                                )
                            }
                        },
                        actions = {
                            IconButton(onClick = {
                                showDeleteDialog = true
                            }) {
                                Icon(
                                    Icons.Default.Delete,
                                    contentDescription = "Удалить все задачи"
                                )
                            }
                            IconButton(onClick = { navController.navigate("day_templates?date=${selectedDate}") }) {
                                Icon(
                                    Icons.Default.ViewModule,
                                    contentDescription = "Шаблоны дня"
                                )
                            }
                        }
                    )
                },
                floatingActionButton = {
                    FloatingActionButton(onClick = { navController.navigate(NavRoutes.AddTask.route) }) {
                        Icon(Icons.Default.Add, contentDescription = "Добавить задачу")
                    }
                },
                bottomBar = {
                    if (!isSubtaskSheetVisible) {
                        var quickTaskTitle by remember { mutableStateOf("") }
                        val keyboardController = LocalSoftwareKeyboardController.current
                        QuickAddBottomBar(
                            text = quickTaskTitle,
                            onTextChange = { quickTaskTitle = it },
                            onAddTask = {
                                viewModel.quickAddTask(quickTaskTitle, selectedDate)
                                quickTaskTitle = ""
                                keyboardController?.hide()
                            }
                        )
                    }
                }
            ) { padding ->
                Column(modifier = Modifier.padding(padding)) {
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

                    LazyColumn(
                        state = reorderableState.listState,
                        modifier = Modifier
                            .fillMaxSize()
                            .reorderable(reorderableState)
                            .detectReorderAfterLongPress(reorderableState)
                    ) {
                        itemsIndexed(taskListState, key = { _, task -> task.id }) { index, task ->
                            ReorderableItem(reorderableState, key = task.id) { isDragging ->
                                val elevation = if (isDragging) 8.dp else 0.dp
                                TaskItem(
                                    modifier = Modifier.shadow(elevation),
                                    task = task,
                                    onCheck = { viewModel.toggleTaskDone(task) },
                                    onEdit = { navController.navigate("edit_task/${task.id}") },
                                    onDelete = { viewModel.deleteTask(task) },
                                    onUpdateTask = { updatedTask -> viewModel.updateTask(updatedTask) },
                                    onEditSubtasks = {
                                        taskToEditSubtasks = task
                                        currentSubtasksList.clear()
                                        currentSubtasksList.addAll(task.subtasks)
                                        newSubtaskTextInSheet = ""
                                        scope.launch { sheetState.show() }
                                    },
                                    onToggleSubtask = { currentTask, subtaskIndex ->
                                        viewModel.toggleSubtask(currentTask, subtaskIndex)
                                    }
                                )
                            }
                        }
                        item { Spacer(modifier = Modifier.height(80.dp)) }
                    }
                }
            }
        }

        if (taskToEditSubtasks != null) {
            ModalBottomSheet(
                onDismissRequest = {
                    scope.launch { sheetState.hide() }
                        .invokeOnCompletion { if (!sheetState.isVisible) taskToEditSubtasks = null }
                },
                sheetState = sheetState,
            ) {
                SubtaskEditorContent(
                    localSubtasks = currentSubtasksList,
                    newSubtaskText = newSubtaskTextInSheet,
                    onNewSubtaskTextChange = { newSubtaskTextInSheet = it },
                    onAddSubtask = {
                        if (newSubtaskTextInSheet.isNotBlank()) {
                            currentSubtasksList.add(Subtask(title = newSubtaskTextInSheet.trim()))
                            newSubtaskTextInSheet = ""
                        }
                    },
                    onSave = {
                        taskToEditSubtasks?.let { task ->
                            val updatedTask = task.copy(subtasks = currentSubtasksList.toList())
                            viewModel.updateTask(updatedTask)
                        }
                        scope.launch { sheetState.hide() }.invokeOnCompletion {
                            if (!sheetState.isVisible) taskToEditSubtasks = null
                        }
                    },
                    onDismiss = {
                        scope.launch { sheetState.hide() }.invokeOnCompletion {
                            if (!sheetState.isVisible) taskToEditSubtasks = null
                        }
                    }
                )
            }
        }

        if (showDeleteDialog) {
            AlertDialog(
                onDismissRequest = { showDeleteDialog = false },
                title = { Text("Подтверждение") },
                text = { Text("Вы действительно хотите удалить все задачи за $selectedDate?") },
                confirmButton = {
                    TextButton(onClick = {
                        viewModel.deleteAllTasksForDate(selectedDate); showDeleteDialog = false
                    }) { Text("Удалить") }
                },
                dismissButton = {
                    TextButton(onClick = {
                        showDeleteDialog = false
                    }) { Text("Отмена") }
                })
        }
    }
}