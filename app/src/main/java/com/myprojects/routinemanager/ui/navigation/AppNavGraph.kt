package com.myprojects.routinemanager.ui.navigation

import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.myprojects.routinemanager.data.model.DayTemplateWithTasks // Keep if used elsewhere or for clarity
import com.myprojects.routinemanager.ui.screens.*
import com.myprojects.routinemanager.ui.viewmodel.DayTemplateViewModel
import com.myprojects.routinemanager.ui.viewmodel.SettingsViewModel
import com.myprojects.routinemanager.ui.viewmodel.TaskViewModel
import java.time.LocalDate

@Composable
fun AppNavGraph(
    navController: NavHostController,
    taskViewModel: TaskViewModel,
    dayTemplateViewModel: DayTemplateViewModel // Passed from MainActivity
) {
    NavHost(
        navController = navController,
        startDestination = NavRoutes.TaskList.route
    ) {
        // Main task list screen
        composable(NavRoutes.TaskList.route) {
            TaskListScreen(
                viewModel = taskViewModel,
                navController = navController,
                onAddTaskClick = { navController.navigate(NavRoutes.AddTask.route) }, // Navigate to AddTaskRoot
            )
        }

        // Root screen for adding tasks (choosing New or Template from FAB)
        composable(NavRoutes.AddTask.route) {
            AddTaskRootScreen(
                taskViewModel = taskViewModel,
                dayTemplateViewModel = dayTemplateViewModel, // Pass DayTemplateViewModel
                onTaskAdded = { navController.popBackStack() },
                templateId = null // Not adding to a specific DayTemplate here
            )
        }

        // Route for AddTaskScreen (used for editing Task and creating standalone TaskTemplate)
        composable(
            route = "add_task/{taskId}", // Accepts "new_template" or an actual task ID
            arguments = listOf(navArgument("taskId") { type = NavType.StringType })
        ) { backStackEntry ->
            val taskIdArg = backStackEntry.arguments?.getString("taskId")
            val isCreatingTemplate = taskIdArg == "new_template" // Check marker for template creation
            val actualTaskId = if (isCreatingTemplate) null else taskIdArg // Determine actual task ID

            AddTaskScreen(
                viewModel = taskViewModel, // TaskViewModel handles saving logic
                onTaskAdded = { navController.popBackStack() },
                taskId = actualTaskId,
                templateId = null, // Not adding to a specific DayTemplate in this mode
                isCreatingTemplate = isCreatingTemplate // Pass the flag
            )
        }

        // Route for editing an existing task (explicitly goes to AddTaskScreen)
        composable("edit_task/{taskId}") { backStackEntry ->
            val taskId = backStackEntry.arguments?.getString("taskId")
            if (taskId != null) {
                AddTaskScreen(
                    viewModel = taskViewModel,
                    onTaskAdded = { navController.popBackStack() },
                    taskId = taskId,
                    templateId = null,
                    isCreatingTemplate = false // Explicitly not creating a template
                )
            }
        }

        // Task details screen
        composable("${NavRoutes.TaskDetail.route}/{taskId}") { backStackEntry ->
            val taskId = backStackEntry.arguments?.getString("taskId") ?: return@composable
            val task = taskViewModel.getTaskById(taskId) // Fetch task data
            if (task != null) {
                TaskDetailScreen(
                    task = task,
                    onBack = { navController.popBackStack() },
                    onToggleTaskDone = { t -> taskViewModel.toggleTaskDone(t) },
                    onSubtaskToggle = { t, index -> taskViewModel.toggleSubtask(t, index) },
                    onConcentrationMode = { navController.navigate(NavRoutes.ConcentrationMode.route) }
                )
            } else {
                navController.popBackStack() // Go back if task is somehow not found
            }
        }

        // Route for adding a task TO a specific DayTemplate (uses AddTaskRootScreen)
        composable("add_task_to_template/{templateId}") { backStackEntry ->
            val templateId = backStackEntry.arguments?.getString("templateId")
            AddTaskRootScreen( // Goes through AddTaskRootScreen first
                taskViewModel = taskViewModel,
                dayTemplateViewModel = dayTemplateViewModel, // Pass DayTemplateViewModel
                templateId = templateId, // Pass the DayTemplate ID
                onTaskAdded = { navController.popBackStack() }
            )
        }

        // Day templates screen (for applying to a date) - Now passes TaskViewModel
        composable("day_templates?date={date}") { backStackEntry ->
            val dateArg = backStackEntry.arguments?.getString("date")
            val selectedDate = dateArg?.let { LocalDate.parse(it) } ?: LocalDate.now()
            DayTemplatesScreen(
                taskViewModel = taskViewModel, // Pass TaskViewModel for task checking
                dayTemplateViewModel = dayTemplateViewModel,
                navController = navController,
                selectedDateForApply = selectedDate, // Pass the date to apply to
                onApplyTemplate = { /* Logic is now internal to DayTemplatesScreen */ },
                onOpenDetails = { template -> navController.navigate("template_detail/${template.template.id}") },
                onCreateCustomTemplate = { navController.navigate("edit_template") },
                onDeleteTemplate = { template -> dayTemplateViewModel.deleteTemplate(template.template) },
                showApplyButton = true // Show the apply button
            )
        }

        // Day template details screen
        composable("template_detail/{templateId}") { backStackEntry ->
            val templateId = backStackEntry.arguments?.getString("templateId")
            if (templateId != null) {
                TemplateDetailScreen(
                    templateId = templateId,
                    viewModel = dayTemplateViewModel,
                    navController = navController,
                    onBack = { navController.popBackStack() }
                )
            }
        }

        // Screen for creating a new DAY template
        composable("edit_template") {
            EditDayTemplateScreen(
                viewModel = dayTemplateViewModel,
                onBack = { navController.popBackStack() }
            )
        }

        // Screen for managing DAY templates (no apply button) - Now passes TaskViewModel
        composable("manage_templates") {
            DayTemplatesScreen(
                taskViewModel = taskViewModel, // Pass TaskViewModel
                dayTemplateViewModel = dayTemplateViewModel,
                navController = navController,
                selectedDateForApply = null, // No date context for applying
                onApplyTemplate = { /* No action */ },
                onOpenDetails = { template -> navController.navigate("template_detail/${template.template.id}") },
                onCreateCustomTemplate = { navController.navigate("edit_template") },
                onDeleteTemplate = { template -> dayTemplateViewModel.deleteTemplate(template.template) },
                showApplyButton = false // Hide apply button
            )
        }

        // Screen for managing standalone TASK templates
        composable("task_template") {
            TaskTemplatesScreen(
                dayTemplateViewModel = dayTemplateViewModel, // Provides the templates
                navController = navController
            )
        }

        // Settings screen
        composable(NavRoutes.Settings.route) {
            val settingsViewModel: SettingsViewModel = hiltViewModel()
            SettingsScreen(
                settingsViewModel = settingsViewModel,
                onBack = { navController.popBackStack() }
            )
        }

        // Concentration mode screen
        composable(NavRoutes.ConcentrationMode.route) {
            ConcentrationModeScreen(
                onDisable = { navController.popBackStack() }
            )
        }

        // Backup screen
        composable("backup") {
            BackupScreen(navController = navController, taskViewModel = taskViewModel)
        }

        // Statistics screen
        composable("statistics") {
            StatisticsScreen(
                navController = navController,
                taskViewModel = taskViewModel // Use existing injected instance
            )
        }
    }
}