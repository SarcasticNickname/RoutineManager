package com.myprojects.routinemanager.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.myprojects.routinemanager.ui.viewmodel.TaskViewModel

@Composable
fun AddTaskFromTemplateScreen(
    viewModel: TaskViewModel,
    onTaskAdded: () -> Unit
) {
//    val templates = viewModel.getAllTemplates()
//
//    Column(Modifier.padding(16.dp)) {
//        Text("Выберите шаблон", style = MaterialTheme.typography.titleLarge)
//
//        Spacer(modifier = Modifier.height(12.dp))
//
//        templates.forEach { template ->
//            Button(
//                onClick = {
//                    viewModel.addTaskFromTemplate(template)
//                    onTaskAdded()
//                },
//                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
//            ) {
//                Text(template.name)
//            }
//        }
//    }
}
