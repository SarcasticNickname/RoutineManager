package com.myprojects.routinemanager.ui.navigation

/**
 * Список «роутов» (экранов) для навигации.
 */
sealed class NavRoutes(val route: String) {
    object TaskList : NavRoutes("task_list")
    object AddTask : NavRoutes("add_task")
    object Settings : NavRoutes("settings")
    object ConcentrationMode : NavRoutes("concentration_mode")
}
