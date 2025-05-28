package com.myprojects.routinemanager.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.myprojects.routinemanager.ui.viewmodel.TaskViewModel
import com.myprojects.routinemanager.ui.viewmodel.MonthlyStats

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatisticsScreen(
    navController: NavController,
    taskViewModel: TaskViewModel
) {
    val stats by taskViewModel.monthlyStats.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Статистика") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Назад"
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            when {
                stats.isLoading -> {
                    CircularProgressIndicator()
                }
                stats.totalTasks == 0 && !stats.isLoading -> {
                    Text(
                        "Нет данных о задачах за ${stats.monthName}.",
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center
                    )
                }
                else -> {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = "Статистика за ${stats.monthName}",
                            style = MaterialTheme.typography.titleLarge
                        )

                        DonutChart(stats = stats)

                        Column(horizontalAlignment = Alignment.Start) {
                            Text(
                                text = "Всего задач: ${stats.totalTasks}",
                                style = MaterialTheme.typography.bodyLarge
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Выполнено: ${stats.completedTasks}",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Осталось: ${stats.pendingTasks}",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DonutChart(
    stats: MonthlyStats,
    modifier: Modifier = Modifier,
    chartSize: Float = 180f,
    strokeWidth: Float = 25f
) {
    val total = stats.totalTasks
    if (total == 0) return

    val completed = stats.completedTasks
    val pending = stats.pendingTasks

    val completedAngle = (completed.toFloat() / total.toFloat()) * 360f
    val pendingAngle = (pending.toFloat() / total.toFloat()) * 360f

    val completedColor = MaterialTheme.colorScheme.primary
    val pendingColor = MaterialTheme.colorScheme.errorContainer
    val backgroundColor = MaterialTheme.colorScheme.surfaceVariant

    val strokePx = with(LocalDensity.current) { strokeWidth.dp.toPx() }
    val sizePx = with(LocalDensity.current) { chartSize.dp.toPx() }

    Box(
        modifier = modifier.size(chartSize.dp),
        contentAlignment = Alignment.Center
    ) {
        val percentage = if (total > 0) (completed.toFloat() / total.toFloat() * 100).toInt() else 0
        Text(
            text = "$percentage%",
            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.primary
        )

        Canvas(modifier = Modifier.fillMaxSize()) {
            val topLeft = Offset((size.width - sizePx) / 2, (size.height - sizePx) / 2)

            drawArc(
                color = pendingColor,
                startAngle = -90f + completedAngle,
                sweepAngle = pendingAngle,
                useCenter = false,
                topLeft = topLeft,
                size = Size(sizePx, sizePx),
                style = Stroke(width = strokePx, cap = StrokeCap.Butt)
            )

            drawArc(
                color = completedColor,
                startAngle = -90f,
                sweepAngle = completedAngle,
                useCenter = false,
                topLeft = topLeft,
                size = Size(sizePx, sizePx),
                style = Stroke(width = strokePx, cap = StrokeCap.Butt)
            )
        }
    }
}