package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.CognitiveLoad
import com.example.data.PlanningHorizon
import com.example.data.Task
import com.example.ui.theme.BentoBorder

@Composable
fun TaskBoardScreen(taskViewModel: TaskViewModel) {
    val weeklyTasks by taskViewModel.weeklyTasks.collectAsStateWithLifecycle()
    val dailyTasks by taskViewModel.dailyTasks.collectAsStateWithLifecycle()

    var showAddTaskDialog by remember { mutableStateOf(false) }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddTaskDialog = true },
                modifier = Modifier.testTag("add_task_fab")
            ) {
                Icon(Icons.Filled.Add, contentDescription = "Add Task")
            }
        }
    ) { paddingValues ->
        // Use a Column with vertical scroll as the main grid container
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header Row (Avatar and Title)
            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Dolphin with Adeeb", style = MaterialTheme.typography.titleLarge)
                    Text("Hello, Adeeb \uD83D\uDC4B", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(MaterialTheme.colorScheme.primaryContainer, CircleShape)
                        .border(2.dp, Color.White, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text("AD", color = MaterialTheme.colorScheme.onPrimaryContainer, style = MaterialTheme.typography.titleMedium)
                }
            }

            // Weekly Buffer Bento Box
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.secondaryContainer, RoundedCornerShape(24.dp))
                    .border(1.dp, BentoBorder, RoundedCornerShape(24.dp))
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "WEEKLY BUFFER",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Box(
                        modifier = Modifier.background(Color.White.copy(alpha = 0.5f), CircleShape).padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text("${weeklyTasks.size} Tasks", style = MaterialTheme.typography.labelSmall)
                    }
                }
                
                if (weeklyTasks.isEmpty()) {
                    Text(
                        text = "No tasks in the weekly buffer.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(weeklyTasks, key = { it.id }) { task ->
                            WeeklyTaskCard(
                                task = task,
                                onMoveToToday = { taskViewModel.moveTaskToToday(task) },
                                onComplete = { taskViewModel.toggleTaskComplete(task) }
                            )
                        }
                    }
                }
            }

            // Daily Grid Boxes
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                val deepTasks = dailyTasks.filter { it.cognitiveLoad == CognitiveLoad.DEEP_WORK }
                val shallowTasks = dailyTasks.filter { it.cognitiveLoad == CognitiveLoad.SHALLOW_WORK }

                // Daily Deep Box
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(24.dp))
                        .border(1.dp, BentoBorder, RoundedCornerShape(24.dp))
                        .padding(16.dp)
                ) {
                    Text(
                        text = "DAILY DEEP",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    if (deepTasks.isEmpty()) {
                        Text("No deep tasks.", style = MaterialTheme.typography.bodySmall)
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            deepTasks.forEach { task ->
                                DailyTaskCard(
                                    task = task,
                                    onComplete = { taskViewModel.toggleTaskComplete(task) }
                                )
                            }
                        }
                    }
                }

                // Daily Shallow Box
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(24.dp))
                        .border(1.dp, BentoBorder, RoundedCornerShape(24.dp))
                        .padding(16.dp)
                ) {
                    Text(
                        text = "DAILY SHALLOW",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    if (shallowTasks.isEmpty()) {
                        Text("No shallow tasks.", style = MaterialTheme.typography.bodySmall)
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            shallowTasks.forEach { task ->
                                DailyTaskCard(
                                    task = task,
                                    onComplete = { taskViewModel.toggleTaskComplete(task) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAddTaskDialog) {
        AddTaskDialog(
            onDismiss = { showAddTaskDialog = false },
            onSave = { title, load, horizon ->
                taskViewModel.addTask(title, load, horizon)
                showAddTaskDialog = false
            }
        )
    }
}

@Composable
fun WeeklyTaskCard(task: Task, onMoveToToday: () -> Unit, onComplete: () -> Unit) {
    Box(
        modifier = Modifier
            .width(220.dp)
            .background(Color.White, RoundedCornerShape(16.dp))
            .border(1.dp, Color(0xFFD0BCFF), RoundedCornerShape(16.dp))
            .padding(12.dp)
    ) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(bottom = 4.dp)) {
                val pointColor = if (task.cognitiveLoad == CognitiveLoad.DEEP_WORK) MaterialTheme.colorScheme.error else Color(0xFF00639B)
                Box(modifier = Modifier.size(8.dp).background(pointColor, CircleShape))
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = if (task.cognitiveLoad == CognitiveLoad.DEEP_WORK) "DEEP WORK" else "SHALLOW WORK",
                    style = MaterialTheme.typography.labelSmall.copy(color = pointColor)
                )
            }
            Text(
                text = task.title,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 2,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            Button(
                onClick = onMoveToToday,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                shape = CircleShape,
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                modifier = Modifier.height(28.dp)
            ) {
                Text("Move to Today", style = MaterialTheme.typography.labelSmall)
            }
        }
    }
}

@Composable
fun DailyTaskCard(task: Task, onComplete: () -> Unit) {
    val borderColor = if (task.cognitiveLoad == CognitiveLoad.DEEP_WORK) Color.Transparent else Color(0xFF00639B)
    val cardModifier = if (task.cognitiveLoad == CognitiveLoad.DEEP_WORK) {
        Modifier.fillMaxWidth().background(Color.White, RoundedCornerShape(12.dp)).padding(8.dp)
    } else {
        Modifier.fillMaxWidth().background(Color.White, RoundedCornerShape(12.dp)).border(2.dp, borderColor, RoundedCornerShape(12.dp)).padding(8.dp)
    }

    Row(
        modifier = cardModifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(
            checked = task.isCompleted,
            onCheckedChange = { onComplete() },
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(text = task.title, style = MaterialTheme.typography.labelMedium)
    }
}

@Composable
fun AddTaskDialog(onDismiss: () -> Unit, onSave: (String, CognitiveLoad, PlanningHorizon) -> Unit) {
    var title by remember { mutableStateOf("") }
    var load by remember { mutableStateOf(CognitiveLoad.SHALLOW_WORK) }
    var horizon by remember { mutableStateOf(PlanningHorizon.WEEKLY) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add New Task") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Task Title") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("task_title_input")
                )
                
                Text("Cognitive Load", style = MaterialTheme.typography.labelLarge)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = load == CognitiveLoad.SHALLOW_WORK,
                        onClick = { load = CognitiveLoad.SHALLOW_WORK },
                        label = { Text("Shallow") }
                    )
                    FilterChip(
                        selected = load == CognitiveLoad.DEEP_WORK,
                        onClick = { load = CognitiveLoad.DEEP_WORK },
                        label = { Text("Deep") }
                    )
                }

                Text("Planning Horizon", style = MaterialTheme.typography.labelLarge)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = horizon == PlanningHorizon.WEEKLY,
                        onClick = { horizon = PlanningHorizon.WEEKLY },
                        label = { Text("Weekly Buffer") }
                    )
                    FilterChip(
                        selected = horizon == PlanningHorizon.DAILY,
                        onClick = { horizon = PlanningHorizon.DAILY },
                        label = { Text("Daily Focus") }
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onSave(title, load, horizon) },
                enabled = title.isNotBlank(),
                modifier = Modifier.testTag("save_task_button")
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
