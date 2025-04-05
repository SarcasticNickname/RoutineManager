package com.myprojects.routinemanager

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.navigation.compose.rememberNavController
import com.myprojects.routinemanager.ui.navigation.AppNavGraph
import com.myprojects.routinemanager.ui.theme.RoutineManagerTheme
import com.myprojects.routinemanager.ui.viewmodel.TaskViewModel
import com.myprojects.routinemanager.ui.viewmodel.DayTemplateViewModel
import dagger.hilt.android.AndroidEntryPoint

/**
 * Главная activity приложения.
 * Использует Hilt для предоставления ViewModel и запускает граф навигации.
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val taskViewModel: TaskViewModel by viewModels()
    private val dayTemplateViewModel: DayTemplateViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            RoutineManagerTheme {
                val navController = rememberNavController()

                AppNavGraph(
                    navController = navController,
                    taskViewModel = taskViewModel,
                    dayTemplateViewModel = dayTemplateViewModel
                )
            }
        }
    }
}
