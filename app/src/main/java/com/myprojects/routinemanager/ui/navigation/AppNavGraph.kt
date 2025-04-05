package com.myprojects.routinemanager.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.myprojects.routinemanager.ui.screens.AddTaskRootScreen
import com.myprojects.routinemanager.ui.screens.TaskDetailScreen
import com.myprojects.routinemanager.ui.screens.TaskListScreen
import com.myprojects.routinemanager.ui.screens.AddTaskScreen
import com.myprojects.routinemanager.ui.screens.DayTemplatesScreen
import com.myprojects.routinemanager.ui.viewmodel.DayTemplateViewModel
import com.myprojects.routinemanager.ui.viewmodel.TaskViewModel
import java.time.LocalDate
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun AppNavGraph(
    navController: NavHostController,
    taskViewModel: TaskViewModel,
    dayTemplateViewModel: DayTemplateViewModel
) {
    NavHost(
        navController = navController,
        startDestination = NavRoutes.TaskList.route
    ) {
        composable(NavRoutes.TaskList.route) {
            TaskListScreen(
                viewModel = taskViewModel,
                onAddTaskClick = { navController.navigate(NavRoutes.AddTask.route) },
                onTaskClick = { taskId ->
                    navController.navigate("${NavRoutes.TaskDetail.route}/$taskId")
                },
                navController = navController
            )
        }

        composable(NavRoutes.AddTask.route) {
            AddTaskScreen(
                viewModel = taskViewModel,
                onTaskAdded = { navController.popBackStack() }
            )
        }

        composable("${NavRoutes.TaskDetail.route}/{taskId}") { backStackEntry ->
            val taskId = backStackEntry.arguments?.getString("taskId") ?: return@composable
            TaskDetailScreen(
                viewModel = taskViewModel,
                taskId = taskId,
                onBack = { navController.popBackStack() }
            )
        }

        composable("add_root") {
            AddTaskRootScreen(
                viewModel = taskViewModel,
                onTaskAdded = { navController.popBackStack() }
            )
        }

        composable("day_templates?date={date}") { backStackEntry ->
            val dateArg = backStackEntry.arguments?.getString("date")
            val selectedDate = dateArg?.let { LocalDate.parse(it) } ?: LocalDate.now()

            DayTemplatesScreen(
                viewModel = dayTemplateViewModel,
                onApplyTemplate = { template ->
                    dayTemplateViewModel.applyTemplate(template, selectedDate)
                    taskViewModel.loadTasksFor(selectedDate)
                    navController.popBackStack()
                },
                onEditTemplate = { template ->
                    // заглушка
                }
            )
        }
    }
}
