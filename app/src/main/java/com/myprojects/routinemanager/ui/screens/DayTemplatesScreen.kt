package com.myprojects.routinemanager.ui.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.myprojects.routinemanager.data.model.DayTemplateWithTasks
import com.myprojects.routinemanager.ui.viewmodel.DayTemplateViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun DayTemplatesScreen(
    viewModel: DayTemplateViewModel,
    navController: NavController,
    onApplyTemplate: (DayTemplateWithTasks) -> Unit,
    onOpenDetails: (DayTemplateWithTasks) -> Unit,
    onCreateCustomTemplate: () -> Unit,
    onDeleteTemplate: (DayTemplateWithTasks) -> Unit
) {
    val weekly by viewModel.weeklyTemplates.collectAsState()
    val custom by viewModel.customTemplates.collectAsState()

    var selectedTab by rememberSaveable { mutableStateOf(0) }

    Scaffold(
        floatingActionButton = {
            if (selectedTab == 1) {
                FloatingActionButton(onClick = onCreateCustomTemplate) {
                    Icon(Icons.Default.Add, contentDescription = "Создать шаблон")
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            TabRow(selectedTabIndex = selectedTab) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("По дням недели") }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("Пользовательские") }
                )
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
                        items(weekly) { templateWithTasks ->
                            TemplateGridCard(
                                template = templateWithTasks,
                                onApply = onApplyTemplate,
                                onDetails = onOpenDetails
                            )
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
                        items(custom) { templateWithTasks ->
                            TemplateListCard(
                                template = templateWithTasks,
                                onApply = onApplyTemplate,
                                onOpenDetails = onOpenDetails,
                                onDelete = onDeleteTemplate
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TemplateGridCard(
    template: DayTemplateWithTasks,
    onApply: (DayTemplateWithTasks) -> Unit,
    onDetails: (DayTemplateWithTasks) -> Unit
) {
    val data = template.template
    val tasks = template.taskTemplates

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .clickable { onDetails(template) },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    data.name,
                    style = MaterialTheme.typography.titleMedium
                )

                Spacer(modifier = Modifier.height(4.dp))

                if (tasks.isNotEmpty()) {
                    Text(
                        text = tasks.take(2).joinToString("\n") { "- ${it.defaultTitle}" },
                        style = MaterialTheme.typography.bodySmall
                    )
                } else {
                    Text("Нет задач", style = MaterialTheme.typography.bodySmall)
                }
            }

            Row(
                horizontalArrangement = Arrangement.Start,
                modifier = Modifier.fillMaxWidth()
            ) {
                TextButton(onClick = { onApply(template) }) {
                    Text("▶ Применить")
                }
            }
        }
    }
}

@Composable
fun TemplateListCard(
    template: DayTemplateWithTasks,
    onApply: (DayTemplateWithTasks) -> Unit,
    onOpenDetails: (DayTemplateWithTasks) -> Unit,
    onDelete: (DayTemplateWithTasks) -> Unit
) {
    val data = template.template
    val tasks = template.taskTemplates

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onOpenDetails(template) },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(data.name, style = MaterialTheme.typography.titleMedium)
                IconButton(onClick = { onDelete(template) }) {
                    Icon(Icons.Default.Delete, contentDescription = "Удалить шаблон")
                }
            }

            if (tasks.isNotEmpty()) {
                Text(
                    text = tasks.take(2).joinToString("\n") { "- ${it.defaultTitle}" },
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(top = 4.dp)
                )
            } else {
                Text("Нет задач", style = MaterialTheme.typography.bodySmall)
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                horizontalArrangement = Arrangement.Start,
                modifier = Modifier.fillMaxWidth()
            ) {
                TextButton(onClick = { onApply(template) }) {
                    Text("Применить ▶")
                }
            }
        }
    }
}
