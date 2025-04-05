package com.myprojects.routinemanager.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.myprojects.routinemanager.data.model.Task
import com.myprojects.routinemanager.data.model.getCategoryColor
import com.myprojects.routinemanager.ui.viewmodel.TaskViewModel
import java.time.LocalDate

import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskListScreen(
    viewModel: TaskViewModel,
    navController: NavController,
    onAddTaskClick: () -> Unit,
    onTaskClick: (String) -> Unit
) {
    var selectedDate by remember { mutableStateOf(LocalDate.now()) }
    val taskList by viewModel.tasks.collectAsState()
    val context = LocalContext.current

    // Загружаем задачи на выбранную дату
    LaunchedEffect(selectedDate) {
        viewModel.loadTasksFor(selectedDate)
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Менеджер рутины") })
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { navController.navigate("add_root") }) {
                Text("+")
            }
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {

            // --- Блок выбора даты ---
            val isToday = selectedDate == LocalDate.now()
            val locale = Locale("ru")
            val formatter = DateTimeFormatter.ofPattern("EEEE, d MMMM", locale)
            val formattedDate = selectedDate.format(formatter).replaceFirstChar { it.uppercase() }

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = formattedDate,
                        style = MaterialTheme.typography.bodyMedium
                    )

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
                            android.app.DatePickerDialog(
                                context,
                                { _, year, month, dayOfMonth ->
                                    selectedDate = LocalDate.of(year, month + 1, dayOfMonth)
                                },
                                selectedDate.year,
                                selectedDate.monthValue - 1,
                                selectedDate.dayOfMonth
                            ).show()
                        }) {
                            Icon(Icons.Default.CalendarToday, contentDescription = "Выбрать дату")
                        }
                    }
                }
            }

            // --- Список задач ---
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(taskList) { task ->
                    TaskItem(
                        task = task,
                        onCheck = { viewModel.toggleTaskDone(task) },
                        onClick = { onTaskClick(task.id) }
                    )
                }
            }
        }
    }
}

@Composable
fun TaskItem(
    task: Task,
    onCheck: () -> Unit,
    onClick: () -> Unit
) {
    val categoryColor = getCategoryColor(task.category)
    var expanded by remember { mutableStateOf(false) }
    val subtasks = remember { task.subtasks.toMutableStateList() }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clickable { expanded = !expanded },
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column {
            Row(modifier = Modifier.height(IntrinsicSize.Min)) {
                // Цветовая полоска категории
                Box(
                    modifier = Modifier
                        .width(6.dp)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(topStart = 12.dp, bottomStart = 12.dp))
                        .background(categoryColor)
                )

                Column(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth()
                ) {
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = task.title,
                                style = MaterialTheme.typography.titleMedium
                            )
                            if (!task.description.isNullOrEmpty()) {
                                Text(
                                    text = task.description,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }

                            // ВРЕМЯ (под заголовком)
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(top = 4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Schedule,
                                    contentDescription = "Время",
                                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = formatTimeRange(task.startTime, task.endTime),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                )
                            }
                        }

                        Row(
                            verticalAlignment = Alignment.Top,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            var menuExpanded by remember { mutableStateOf(false) }

                            IconButton(onClick = { menuExpanded = true }) {
                                Icon(
                                    imageVector = Icons.Default.MoreVert,
                                    contentDescription = "Меню"
                                )
                            }

                            DropdownMenu(
                                expanded = menuExpanded,
                                onDismissRequest = { menuExpanded = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text("Редактировать") },
                                    onClick = {
                                        menuExpanded = false
                                        onClick()
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Удалить") },
                                    onClick = {
                                        menuExpanded = false
                                        // вызвать onDelete() если пробросишь
                                    }
                                )
                            }

                            Checkbox(
                                checked = task.isDone,
                                onCheckedChange = { onCheck() }
                            )
                        }
                    }

                    HorizontalDivider(
                        modifier = Modifier
                            .padding(top = 8.dp, bottom = 4.dp)
                            .fillMaxWidth(),
                        thickness = 1.dp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
                    )
                }
            }

            AnimatedVisibility(visible = expanded) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f))
                        .padding(12.dp)
                ) {
                    HorizontalDivider(
                        modifier = Modifier.padding(bottom = 8.dp),
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f)
                    )

                    subtasks.forEachIndexed { index, subtask ->
                        var subtaskDone by remember { mutableStateOf(subtask.isDone) }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 2.dp)
                        ) {
                            Checkbox(
                                checked = subtaskDone,
                                onCheckedChange = {
                                    subtaskDone = it
                                    subtasks[index] = subtask.copy(isDone = it)
                                }
                            )
                            Text(
                                text = subtask.title,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
            }
        }
    }
}

fun formatTimeRange(start: LocalTime?, end: LocalTime?): String {
    return if (start != null && end != null) {
        "${start.format(DateTimeFormatter.ofPattern("HH:mm"))} - ${
            end.format(
                DateTimeFormatter.ofPattern(
                    "HH:mm"
                )
            )
        }"
    } else if (start != null) {
        "с ${start.format(DateTimeFormatter.ofPattern("HH:mm"))}"
    } else {
        "Время не указано"
    }
}
