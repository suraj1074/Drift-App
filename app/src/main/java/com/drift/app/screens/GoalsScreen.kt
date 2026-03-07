package com.drift.app.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.drift.app.data.DriftDatabase
import com.drift.app.data.DriftItem
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GoalsScreen(navController: NavController) {
    val context = LocalContext.current
    val db = remember { DriftDatabase.get(context) }
    val scope = rememberCoroutineScope()

    var goals by remember { mutableStateOf<List<DriftItem>>(emptyList()) }
    var goalText by remember { mutableStateOf("") }
    var selectedHorizon by remember { mutableStateOf("week") }

    LaunchedEffect(Unit) {
        goals = db.driftDao().getActiveGoals()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Goals") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp)
        ) {
            Text(
                text = "What would make this ${selectedHorizon} a win?",
                fontSize = 20.sp,
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Horizon selector
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("week", "month", "quarter").forEach { horizon ->
                    FilterChip(
                        selected = selectedHorizon == horizon,
                        onClick = { selectedHorizon = horizon },
                        label = { Text("This $horizon") }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = goalText,
                onValueChange = { goalText = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("e.g. File my taxes and run 3 times") },
                singleLine = true
            )

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = {
                    if (goalText.isNotBlank()) {
                        scope.launch {
                            db.driftDao().insert(
                                DriftItem(
                                    text = goalText.trim(),
                                    isGoal = true,
                                    goalHorizon = selectedHorizon
                                )
                            )
                            goalText = ""
                            goals = db.driftDao().getActiveGoals()
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = goalText.isNotBlank()
            ) {
                Text("Set Goal")
            }

            Spacer(modifier = Modifier.height(24.dp))

            if (goals.isNotEmpty()) {
                Text("Active Goals", fontSize = 14.sp, color = MaterialTheme.colorScheme.outline)
                Spacer(modifier = Modifier.height(8.dp))

                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(goals) { goal ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer
                            )
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(goal.text, fontSize = 16.sp)
                                    Text(
                                        "This ${goal.goalHorizon ?: "week"}",
                                        fontSize = 13.sp,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                                TextButton(onClick = {
                                    scope.launch {
                                        db.driftDao().updateStatus(goal.id, "done")
                                        goals = db.driftDao().getActiveGoals()
                                    }
                                }) {
                                    Text("Done")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
