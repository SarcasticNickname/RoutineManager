package com.myprojects.routinemanager.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.myprojects.routinemanager.ui.screens.*
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
    // Получаем текущую выбранную дату из ViewModel для передачи на экран создания
    val tasksState by taskViewModel.tasks.collectAsState()
    val selectedDate = tasksState.firstOrNull()?.date ?: LocalDate.now()

    NavHost(
        navController = navController,
        startDestination = NavRoutes.TaskList.route
    ) {
        // --- Главный экран ---
        composable(NavRoutes.TaskList.route) {
            TaskListScreen(
                viewModel = taskViewModel,
                navController = navController,
            )
        }

        // --- Экран-контейнер для добавления задачи (с вкладками) ---
        composable(NavRoutes.AddTask.route) {
            AddTaskRootScreen(
                navController = navController,
                taskViewModel = taskViewModel,
                dayTemplateViewModel = dayTemplateViewModel,
                selectedDate = selectedDate
            )
        }

        // --- Редактирование существующей Задачи ---
        // Этот маршрут остается, так как на него есть прямой переход из TaskItem
        composable(
            route = "edit_task/{taskId}",
            arguments = listOf(navArgument("taskId") { type = NavType.StringType })
        ) { backStackEntry ->
            val taskId = backStackEntry.arguments?.getString("taskId")
            if (taskId != null) {
                EditTaskScreen(
                    viewModel = taskViewModel,
                    taskId = taskId,
                    onNavigateBack = { navController.popBackStack() }
                )
            }
        }

        // --- Управление шаблонами ДНЯ (список) ---
        composable("manage_templates") {
            DayTemplatesScreen(
                taskViewModel = taskViewModel,
                dayTemplateViewModel = dayTemplateViewModel,
                navController = navController,
                selectedDateForApply = null, // Не в режиме применения
                onOpenDetails = { template -> navController.navigate("template_detail/${template.template.id}") },
                onCreateCustomTemplate = { navController.navigate("create_day_template") },
                showApplyButton = false,
                onApplyTemplate = { },
                onDeleteTemplate = { template -> dayTemplateViewModel.deleteTemplate(template.template) }
            )
        }

        // --- Создание нового шаблона ДНЯ ---
        composable("create_day_template") {
            EditDayTemplateScreen(
                viewModel = dayTemplateViewModel,
                onBack = { navController.popBackStack() }
            )
        }

        // --- Детали шаблона ДНЯ (со списком его задач) ---
        composable(
            route = "template_detail/{templateId}",
            arguments = listOf(navArgument("templateId") { type = NavType.StringType })
        ) { backStackEntry ->
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

        // --- Создание новой Задачи ВНУТРИ шаблона ДНЯ ---
        composable(
            route = "add_task_to_template/{templateId}",
            arguments = listOf(navArgument("templateId") { type = NavType.StringType })
        ) { backStackEntry ->
            val templateId = backStackEntry.arguments?.getString("templateId")
            if (templateId != null) {
                AddTaskToTemplateScreen(
                    viewModel = dayTemplateViewModel,
                    dayTemplateId = templateId,
                    onNavigateBack = { navController.popBackStack() }
                )
            }
        }

        // --- Управление шаблонами ЗАДАЧ (список одиночных) ---
        composable("manage_task_templates") {
            TaskTemplatesScreen(
                dayTemplateViewModel = dayTemplateViewModel,
                navController = navController
            )
        }

        // --- Создание нового шаблона ЗАДАЧИ ---
        composable("create_task_template") {
            CreateTaskTemplateScreen(
                viewModel = dayTemplateViewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // --- Экран применения шаблона ДНЯ к дате ---
        composable("day_templates?date={date}") { backStackEntry ->
            val dateArg = backStackEntry.arguments?.getString("date")
            val date = dateArg?.let { LocalDate.parse(it) } ?: LocalDate.now()
            DayTemplatesScreen(
                taskViewModel = taskViewModel,
                dayTemplateViewModel = dayTemplateViewModel,
                navController = navController,
                selectedDateForApply = date,
                onOpenDetails = { template -> navController.navigate("template_detail/${template.template.id}") },
                onCreateCustomTemplate = { navController.navigate("create_day_template") },
                showApplyButton = true,
                onApplyTemplate = { template ->
                    dayTemplateViewModel.applyTemplate(template, date)
                    navController.popBackStack()
                },
                onDeleteTemplate = { template -> dayTemplateViewModel.deleteTemplate(template.template) }
            )
        }

        composable(NavRoutes.Settings.route) {
            val settingsViewModel: SettingsViewModel = hiltViewModel()
            SettingsScreen(
                settingsViewModel = settingsViewModel,
                onBack = { navController.popBackStack() }
            )
        }

        composable(NavRoutes.ConcentrationMode.route) {
            ConcentrationModeScreen(onDisable = { navController.popBackStack() })
        }

        composable("backup") {
            BackupScreen(
                navController = navController,
                taskViewModel = taskViewModel,
                dayTemplateViewModel = dayTemplateViewModel
            )
        }

        composable("statistics") {
            StatisticsScreen(navController = navController, taskViewModel = taskViewModel)
        }
    }
}