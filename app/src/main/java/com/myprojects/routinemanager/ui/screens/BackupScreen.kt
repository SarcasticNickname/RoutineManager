package com.myprojects.routinemanager.ui.screens

import android.app.Activity
import android.content.Intent
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.FirebaseAuthUIActivityResultContract
import com.firebase.ui.auth.data.model.FirebaseAuthUIAuthenticationResult
import com.google.firebase.auth.FirebaseAuth
import com.myprojects.routinemanager.ui.viewmodel.TaskViewModel
import com.myprojects.routinemanager.util.backupAllData
import com.myprojects.routinemanager.util.restoreAllData
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BackupScreen(
    navController: NavController,
    taskViewModel: TaskViewModel
) {
    val context = LocalContext.current
    var backupStatus by remember { mutableStateOf(value = "") }
    var isSuccess by remember { mutableStateOf<Boolean?>(value = null) }
    var isLoading by remember { mutableStateOf(value = false) }

    val firebaseAuth = FirebaseAuth.getInstance()
    val account = firebaseAuth.currentUser // Исходная логика

    val signInLauncher = rememberLauncherForActivityResult(
        contract = FirebaseAuthUIActivityResultContract()
    ) { result: FirebaseAuthUIAuthenticationResult ->
        if (result.resultCode == Activity.RESULT_OK) {
            backupStatus = "Вход выполнен! Перезайдите на экран для обновления." // Уточнение для пользователя
            isSuccess = true
            // UI не обновится сам, т.к. account не state
        } else {
            val response = result.idpResponse
            if (response == null) {
                backupStatus = "Вход отменён"
                isSuccess = null
            } else {
                backupStatus = "Ошибка входа: ${response.error?.message ?: "Неизвестная ошибка"}"
                isSuccess = false
            }
        }
    }

    val providers = arrayListOf(
        AuthUI.IdpConfig.GoogleBuilder().build()
    )

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Резервное копирование") })
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 24.dp, vertical = 16.dp), // Улучшенный padding
            verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically), // Улучшенное расположение
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (account == null) {
                Text(
                    text = "Войдите для резервного копирования и восстановления ваших данных.",
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                Button(onClick = {
                    val signInIntent = AuthUI.getInstance()
                        .createSignInIntentBuilder()
                        .setAvailableProviders(providers)
                        .setIsSmartLockEnabled(false) // Отключаем Smart Lock для простоты
                        .build()
                    signInLauncher.launch(signInIntent)
                }) {
                    Text("Войти через Google")
                }
            } else {
                // Улучшенное приветствие
                Text(
                    text = "Здравствуйте!",
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = "${account.email}",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.primary // Выделяем email
                )

                Spacer(modifier = Modifier.height(32.dp)) // Больший отступ

                Button(
                    onClick = {
                        isLoading = true
                        backupStatus = "Создаётся бэкап..."
                        isSuccess = null
                        (context as? ComponentActivity)?.lifecycleScope?.launch {
                            backupAllData(
                                taskRepository = taskViewModel.taskRepository,
                                dayTemplateRepository = taskViewModel.dayTemplateRepository
                            ) { success, message ->
                                backupStatus = message
                                isSuccess = success
                                isLoading = false
                            }
                        }
                    },
                    enabled = !isLoading // Отключаем во время загрузки
                ) {
                    Text("Создать бэкап")
                }

                Button(
                    onClick = {
                        isLoading = true
                        backupStatus = "Восстановление..."
                        isSuccess = null
                        (context as? ComponentActivity)?.lifecycleScope?.launch {
                            restoreAllData(
                                taskRepository = taskViewModel.taskRepository,
                                dayTemplateRepository = taskViewModel.dayTemplateRepository
                            ) { success, message ->
                                backupStatus = message
                                isSuccess = success
                                isLoading = false
                            }
                        }
                    },
                    enabled = !isLoading // Отключаем во время загрузки
                ) {
                    Text("Восстановить бэкап")
                }

                Spacer(modifier = Modifier.height(16.dp)) // Отступ перед выходом

                TextButton(
                    onClick = {
                        FirebaseAuth.getInstance().signOut()
                        backupStatus = "Вы вышли из аккаунта. Перезайдите на экран для обновления." // Уточнение
                        isSuccess = null
                        // UI не обновится сам
                    },
                    enabled = !isLoading // Отключаем во время загрузки
                ) {
                    Text("Выйти из аккаунта")
                }
            }

            // Улучшенный блок статуса
            AnimatedVisibility(
                visible = backupStatus.isNotBlank(),
                enter = fadeIn(animationSpec = tween(durationMillis = 300)),
                exit = fadeOut(animationSpec = tween(durationMillis = 300))
            ) {
                val color = when (isSuccess) {
                    true -> MaterialTheme.colorScheme.primary
                    false -> MaterialTheme.colorScheme.error
                    null -> MaterialTheme.colorScheme.onSurfaceVariant
                }
                val containerColor = when (isSuccess) {
                    true -> MaterialTheme.colorScheme.primaryContainer
                    false -> MaterialTheme.colorScheme.errorContainer
                    null -> MaterialTheme.colorScheme.surfaceVariant
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp) // Отступ для блока статуса
                        .background(containerColor, shape = MaterialTheme.shapes.medium)
                        .padding(12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = backupStatus,
                        color = color,
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.bodySmall, // Меньший шрифт
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }


            if (isLoading) {
                Spacer(modifier = Modifier.height(16.dp))
                CircularProgressIndicator()
            }

            Spacer(modifier = Modifier.weight(1f)) // Отодвигает кнопку "Назад" вниз

            // Кнопка "Назад" шире
            Button(
                onClick = { navController.popBackStack() },
                modifier = Modifier.fillMaxWidth(0.6f)
            ) {
                Text("Назад")
            }
        }
    }
}