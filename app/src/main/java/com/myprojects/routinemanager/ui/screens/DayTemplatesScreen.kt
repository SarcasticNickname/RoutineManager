package com.myprojects.routinemanager.ui.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.myprojects.routinemanager.data.model.DayTemplate
import com.myprojects.routinemanager.ui.viewmodel.DayTemplateViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun DayTemplatesScreen(
    viewModel: DayTemplateViewModel,
    onApplyTemplate: (DayTemplate) -> Unit,
    onEditTemplate: (DayTemplate) -> Unit
) {
    val weekly by viewModel.weeklyTemplates.collectAsState()
    val custom by viewModel.customTemplates.collectAsState()

    var selectedTab by remember { mutableStateOf(0) }

    Column(modifier = Modifier.fillMaxSize()) {
        TabRow(selectedTabIndex = selectedTab) {
            Tab(selected = selectedTab == 0, onClick = { selectedTab = 0 }, text = { Text("По дням недели") })
            Tab(selected = selectedTab == 1, onClick = { selectedTab = 1 }, text = { Text("Пользовательские") })
        }

        when (selectedTab) {
            0 -> {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(weekly) { template ->
                        TemplateGridCard(template, onApplyTemplate, onEditTemplate)
                    }
                }
            }

            1 -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(custom) { template ->
                        TemplateListCard(template, onApplyTemplate, onEditTemplate)
                    }
                }
            }
        }
    }
}

@Composable
fun TemplateGridCard(
    template: DayTemplate,
    onApply: (DayTemplate) -> Unit,
    onEdit: (DayTemplate) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .clickable { onApply(template) },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Text(template.name, style = MaterialTheme.typography.titleMedium)

            // Краткий список задач
            if (template.taskTemplates.isNotEmpty()) {
                Text(
                    text = template.taskTemplates.take(2).joinToString("\n") { "- ${it.defaultTitle}" },
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(top = 4.dp)
                )
            } else {
                Text("Нет задач", style = MaterialTheme.typography.bodySmall)
            }

            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                TextButton(onClick = { onEdit(template) }) {
                    Text("Изменить")
                }
                TextButton(onClick = { onApply(template) }) {
                    Text("▶")
                }
            }
        }
    }
}

@Composable
fun TemplateListCard(
    template: DayTemplate,
    onApply: (DayTemplate) -> Unit,
    onEdit: (DayTemplate) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onApply(template) },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(template.name, style = MaterialTheme.typography.titleMedium)

            Spacer(modifier = Modifier.height(4.dp))

            if (template.taskTemplates.isNotEmpty()) {
                Text(
                    text = template.taskTemplates.joinToString(", ") { it.defaultTitle },
                    style = MaterialTheme.typography.bodySmall
                )
            } else {
                Text("Нет задач", style = MaterialTheme.typography.bodySmall)
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                TextButton(onClick = { onEdit(template) }) {
                    Text("Изменить")
                }
                TextButton(onClick = { onApply(template) }) {
                    Text("▶ Применить")
                }
            }
        }
    }
}
