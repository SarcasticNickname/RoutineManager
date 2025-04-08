package com.myprojects.routinemanager.ui.navigation

import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.myprojects.routinemanager.data.model.DayTemplateWithTasks
import com.myprojects.routinemanager.ui.screens.AddTaskRootScreen
import com.myprojects.routinemanager.ui.screens.AddTaskScreen
import com.myprojects.routinemanager.ui.screens.ConcentrationModeScreen
import com.myprojects.routinemanager.ui.screens.DayTemplatesScreen
import com.myprojects.routinemanager.ui.screens.EditDayTemplateScreen
import com.myprojects.routinemanager.ui.screens.SettingsScreen
import com.myprojects.routinemanager.ui.screens.TaskDetailScreen
import com.myprojects.routinemanager.ui.screens.TaskListScreen
import com.myprojects.routinemanager.ui.screens.TemplateDetailScreen
import com.myprojects.routinemanager.ui.viewmodel.DayTemplateViewModel
import com.myprojects.routinemanager.ui.viewmodel.SettingsViewModel
import com.myprojects.routinemanager.ui.viewmodel.TaskViewModel
import java.time.LocalDate

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

        composable("edit_task/{taskId}") { backStackEntry ->
            val taskId = backStackEntry.arguments?.getString("taskId")
            if (taskId != null) {
                AddTaskScreen(
                    viewModel = taskViewModel,
                    onTaskAdded = { navController.popBackStack() },
                    taskId = taskId
                )
            }
        }

        composable("${NavRoutes.TaskDetail.route}/{taskId}") { backStackEntry ->
            val taskId = backStackEntry.arguments?.getString("taskId") ?: return@composable
            val task = taskViewModel.getTaskById(taskId)
            if (task != null) {
                TaskDetailScreen(
                    task = task,
                    onBack = { navController.popBackStack() },
                    onToggleTaskDone = { t -> taskViewModel.toggleTaskDone(t) },
                    onSubtaskToggle = { t, index -> taskViewModel.toggleSubtask(t, index) },
                    onConcentrationMode = {
                        navController.navigate(NavRoutes.ConcentrationMode.route)
                    }
                )
            }
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
                onOpenDetails = { template ->
                    navController.navigate("template_detail/${template.template.id}")
                },
                onCreateCustomTemplate = { navController.navigate("edit_template") },
                onDeleteTemplate = { template: DayTemplateWithTasks ->
                    dayTemplateViewModel.deleteTemplate(template.template)
                },
                navController = navController
            )
        }

        composable("template_detail/{templateId}") { backStackEntry ->
            val templateId = backStackEntry.arguments?.getString("templateId")
            if (templateId != null) {
                TemplateDetailScreen(
                    templateId = templateId,
                    viewModel = dayTemplateViewModel,
                    onBack = { navController.popBackStack() },
                    navController = navController
                )
            }
        }

        composable("add_task_to_template/{templateId}") { backStackEntry ->
            val templateId = backStackEntry.arguments?.getString("templateId")
            AddTaskRootScreen(
                viewModel = taskViewModel,
                templateId = templateId,
                onTaskAdded = { navController.popBackStack() }
            )
        }

        composable("edit_template") {
            EditDayTemplateScreen(
                viewModel = dayTemplateViewModel,
                onBack = { navController.popBackStack() }
            )
        }

        composable(NavRoutes.Settings.route) {
            val settingsViewModel: SettingsViewModel = hiltViewModel()
            SettingsScreen(
                settingsViewModel = settingsViewModel,
                onBack = { navController.popBackStack() }
            )
        }

        // Новый маршрут для режима концентрации
        composable(NavRoutes.ConcentrationMode.route) {
            ConcentrationModeScreen(
                onDisable = { navController.popBackStack() }
            )
        }
    }
}
