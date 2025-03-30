package com.myprojects.routinemanager.ui.navigation

/**
 * Список «роутов» (экранов) для навигации.
 * Можно использовать sealed class, enum или константы.
 */
sealed class NavRoutes(val route: String) {
    object TaskList : NavRoutes("task_list")
    object AddTask : NavRoutes("add_task")
    object TaskDetail : NavRoutes("task_detail")
}
