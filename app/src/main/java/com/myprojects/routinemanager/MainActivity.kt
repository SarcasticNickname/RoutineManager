package com.myprojects.routinemanager

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.room.Room
import androidx.navigation.compose.rememberNavController
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.myprojects.routinemanager.ui.navigation.AppNavGraph
import com.myprojects.routinemanager.data.repository.TaskRepository
import com.myprojects.routinemanager.data.room.RoutineManagerDatabase
import com.myprojects.routinemanager.ui.theme.RoutineManagerTheme
import com.myprojects.routinemanager.ui.viewmodel.TaskViewModel

/**
 * Главная activity приложения.
 * Создаёт базу данных, репозиторий, ViewModel и настраивает NavHost.
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Инициализация базы данных Room
        val db = Room.databaseBuilder(
            applicationContext,
            RoutineManagerDatabase::class.java,
            "routine_manager_db"
        ).build()

        // Создаём репозиторий
        val repository = TaskRepository(db.taskDao())

        // Создаём ViewModel через фабрику
        val vmFactory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                return TaskViewModel(repository) as T
            }
        }

        setContent {
            RoutineManagerTheme {
                val navController = rememberNavController()
                val viewModel: TaskViewModel = viewModel(factory = vmFactory)

                // Запускаем граф навигации
                AppNavGraph(navController, viewModel)
            }
        }
    }
}
