package com.myprojects.routinemanager.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.myprojects.routinemanager.ui.screens.*
import com.myprojects.routinemanager.ui.viewmodel.DayTemplateViewModel
import com.myprojects.routinemanager.ui.viewmodel.TaskViewModel
import java.time.LocalDate
import com.myprojects.routinemanager.data.model.DayTemplateWithTasks

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
                onOpenDetails = { template ->
                    navController.navigate("template_detail/${template.template.id}")
                },
                onCreateCustomTemplate = {
                    // Создание нового пользовательского шаблона
                    navController.navigate("edit_template")
                },
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
    }
}
