package com.myprojects.routinemanager.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.myprojects.routinemanager.ui.screens.TaskDetailScreen
import com.myprojects.routinemanager.ui.screens.TaskListScreen
import com.myprojects.routinemanager.ui.screens.AddTaskScreen
import com.myprojects.routinemanager.ui.viewmodel.TaskViewModel

/**
 * Файл, в котором задаём навигационную структуру приложения.
 */
@Composable
fun AppNavGraph(
    navController: NavHostController,
    viewModel: TaskViewModel
) {
    NavHost(
        navController = navController,
        startDestination = NavRoutes.TaskList.route
    ) {
        // Экран списка задач
        composable(NavRoutes.TaskList.route) {
            TaskListScreen(
                viewModel = viewModel,
                onAddTaskClick = { navController.navigate(NavRoutes.AddTask.route) },
                onTaskClick = { taskId ->
                    // Переходим на экран деталей задачи, передаём ID как аргумент
                    navController.navigate("${NavRoutes.TaskDetail.route}/$taskId")
                }
            )
        }

        // Экран добавления задачи
        composable(NavRoutes.AddTask.route) {
            AddTaskScreen(
                viewModel = viewModel,
                onTaskAdded = { navController.popBackStack() }
            )
        }

        // Экран деталей задачи (ID задачи может передаваться через путь)
        composable("${NavRoutes.TaskDetail.route}/{taskId}") { backStackEntry ->
            val taskId = backStackEntry.arguments?.getString("taskId") ?: return@composable
            TaskDetailScreen(
                viewModel = viewModel,
                taskId = taskId,
                onBack = { navController.popBackStack() }
            )
        }
    }
}
