package com.myprojects.routinemanager.ui.screens

import android.app.Activity
import android.content.Intent
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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
    var backupStatus by remember { mutableStateOf("") }
    var isSuccess by remember { mutableStateOf<Boolean?>(null) }
    var isLoading by remember { mutableStateOf(false) }

    val firebaseAuth = FirebaseAuth.getInstance()

    val signInLauncher = rememberLauncherForActivityResult(
        contract = FirebaseAuthUIActivityResultContract()
    ) { result: FirebaseAuthUIAuthenticationResult ->
        if (result.resultCode == Activity.RESULT_OK) {
            backupStatus = "Вход выполнен!"
            isSuccess = true
        } else {
            backupStatus = "Ошибка входа"
            isSuccess = false
        }
    }

    val account = firebaseAuth.currentUser

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
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (account == null) {
                Button(onClick = {
                    val signInIntent = AuthUI.getInstance()
                        .createSignInIntentBuilder()
                        .setAvailableProviders(providers)
                        .build()
                    signInLauncher.launch(signInIntent)
                }) {
                    Text("Войти через Google")
                }
            } else {
                Text("Здравствуйте,\n${account.email}")
                Spacer(modifier = Modifier.height(24.dp))

                Button(onClick = {
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
                }) {
                    Text("Создать бэкап")
                }

                Spacer(modifier = Modifier.height(12.dp))

                Button(onClick = {
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
                }) {
                    Text("Восстановить бэкап")
                }

                Spacer(modifier = Modifier.height(16.dp))

                TextButton(onClick = {
                    FirebaseAuth.getInstance().signOut()
                    backupStatus = "Вы вышли из аккаунта"
                    isSuccess = null
                }) {
                    Text("Выйти из аккаунта")
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            if (backupStatus.isNotBlank()) {
                val color = when (isSuccess) {
                    true -> Color(0xFF4CAF50)
                    false -> Color(0xFFFF5252)
                    null -> Color.Gray
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(color.copy(alpha = 0.1f), shape = MaterialTheme.shapes.medium)
                        .padding(12.dp)
                ) {
                    Text(
                        text = backupStatus,
                        color = color,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            if (isLoading) {
                Spacer(modifier = Modifier.height(16.dp))
                CircularProgressIndicator()
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(onClick = { navController.popBackStack() }) {
                Text("Назад")
            }
        }
    }
}
