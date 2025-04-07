package com.myprojects.routinemanager.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
                title = { Text("Создать шаблон") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Назад")
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
                modifier = Modifier.fillMaxWidth()
            )

            Button(
                onClick = {
                    val newTemplate = DayTemplate(
                        id = UUID.randomUUID().toString(),
                        name = name,
                        taskTemplates = emptyList(),
                        isWeekly = false,
                        weekday = null
                    )
                    viewModel.addTemplate(
                        newTemplate,
                        tasks = emptyList()
                    )
                    onBack()
                },
                enabled = name.isNotBlank(),
                modifier = Modifier.align(Alignment.End)
            ) {
                Text("Создать")
            }
        }
    }
}
