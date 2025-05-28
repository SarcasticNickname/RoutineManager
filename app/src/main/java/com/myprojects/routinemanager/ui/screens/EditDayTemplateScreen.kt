package com.myprojects.routinemanager.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.myprojects.routinemanager.data.model.DayTemplate
import com.myprojects.routinemanager.ui.viewmodel.DayTemplateViewModel
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditDayTemplateScreen(
    viewModel: DayTemplateViewModel,
    onBack: () -> Unit
) {
    var name by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Создать шаблон дня") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Название шаблона") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(modifier = Modifier.weight(1f)) // Push button to bottom

            Button(
                onClick = {
                    val newTemplate = DayTemplate(
                        id = UUID.randomUUID().toString(),
                        name = name.trim(),
                        taskTemplates = emptyList(), // TaskTemplates are added later
                        isWeekly = false,
                        weekday = null
                    )
                    // Call addTemplate without the 'tasks' parameter
                    viewModel.addTemplate(newTemplate)
                    onBack()
                },
                enabled = name.isNotBlank(),
                modifier = Modifier.align(Alignment.End) // Align button to the end
            ) {
                Text("Создать")
            }
        }
    }
}