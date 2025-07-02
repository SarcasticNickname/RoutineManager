package com.myprojects.routinemanager.ui.screens

import android.app.Activity
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import com.myprojects.routinemanager.ui.viewmodel.DayTemplateViewModel
import com.myprojects.routinemanager.ui.viewmodel.TaskViewModel
import com.myprojects.routinemanager.util.backupAllData
import com.myprojects.routinemanager.util.restoreAllData
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BackupScreen(
    navController: NavController,
    taskViewModel: TaskViewModel,
    dayTemplateViewModel: DayTemplateViewModel
) {
    val context = LocalContext.current
    var backupStatus by remember { mutableStateOf(value = "") }
    var isSuccess by remember { mutableStateOf<Boolean?>(value = null) }
    var isLoading by remember { mutableStateOf(value = false) }

    val firebaseAuth = FirebaseAuth.getInstance()
    val account = firebaseAuth.currentUser

    val signInLauncher = rememberLauncherForActivityResult(
        contract = FirebaseAuthUIActivityResultContract()
    ) { result: FirebaseAuthUIAuthenticationResult ->
        if (result.resultCode == Activity.RESULT_OK) {
            backupStatus = "Вход выполнен! Перезайдите на экран для обновления."
            isSuccess = true
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
                .padding(horizontal = 24.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
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
                        .setIsSmartLockEnabled(false)
                        .build()
                    signInLauncher.launch(signInIntent)
                }) {
                    Text("Войти через Google")
                }
            } else {
                Text(
                    text = "Здравствуйте!",
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = "${account.email}",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.height(32.dp))

                Button(
                    onClick = {
                        isLoading = true
                        backupStatus = "Создаётся бэкап..."
                        isSuccess = null
                        (context as? ComponentActivity)?.lifecycleScope?.launch {
                            backupAllData(
                                taskRepository = taskViewModel.taskRepository,
                                dayTemplateRepository = dayTemplateViewModel.repository // Используем репозиторий из DayTemplateViewModel
                            ) { success, message ->
                                backupStatus = message
                                isSuccess = success
                                isLoading = false
                            }
                        }
                    },
                    enabled = !isLoading
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
                                dayTemplateRepository = dayTemplateViewModel.repository // Используем репозиторий из DayTemplateViewModel
                            ) { success, message ->
                                backupStatus = message
                                isSuccess = success
                                isLoading = false
                            }
                        }
                    },
                    enabled = !isLoading
                ) {
                    Text("Восстановить бэкап")
                }

                Spacer(modifier = Modifier.height(16.dp))

                TextButton(
                    onClick = {
                        FirebaseAuth.getInstance().signOut()
                        backupStatus = "Вы вышли из аккаунта. Перезайдите на экран для обновления."
                        isSuccess = null
                    },
                    enabled = !isLoading
                ) {
                    Text("Выйти из аккаунта")
                }
            }

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
                        .padding(vertical = 16.dp)
                        .background(containerColor, shape = MaterialTheme.shapes.medium)
                        .padding(12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = backupStatus,
                        color = color,
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }


            if (isLoading) {
                Spacer(modifier = Modifier.height(16.dp))
                CircularProgressIndicator()
            }

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = { navController.popBackStack() },
                modifier = Modifier.fillMaxWidth(0.6f)
            ) {
                Text("Назад")
            }
        }
    }
}