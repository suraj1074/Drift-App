package com.drift.app.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
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
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
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
                text = "What would make this $selectedHorizon a win?",
                style = MaterialTheme.typography.headlineMedium
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Horizon selector
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("week" to "🗓️ Week", "month" to "📅 Month", "quarter" to "🎯 Quarter").forEach { (value, label) ->
                    FilterChip(
                        selected = selectedHorizon == value,
                        onClick = { selectedHorizon = value },
                        label = { Text(label, fontSize = 13.sp) },
                        shape = RoundedCornerShape(10.dp),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Input area
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier.fillMaxWidth()
            ) {
                TextField(
                    value = goalText,
                    onValueChange = { goalText = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = {
                        Text(
                            "e.g. File my taxes and run 3 times",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                            )
                        )
                    },
                    singleLine = true,
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                        focusedIndicatorColor = androidx.compose.ui.graphics.Color.Transparent,
                        unfocusedIndicatorColor = androidx.compose.ui.graphics.Color.Transparent,
                    ),
                    textStyle = MaterialTheme.typography.bodyMedium.copy(
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 15.sp
                    )
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

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
                enabled = goalText.isNotBlank(),
                shape = RoundedCornerShape(12.dp),
                contentPadding = PaddingValues(vertical = 14.dp)
            ) {
                Text("Set Goal 🌱", fontSize = 15.sp)
            }

            Spacer(modifier = Modifier.height(28.dp))

            if (goals.isNotEmpty()) {
                Text(
                    "ACTIVE GOALS",
                    style = MaterialTheme.typography.titleSmall.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )
                Spacer(modifier = Modifier.height(10.dp))

                LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(goals) { goal ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer
                            ),
                            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        goal.text,
                                        style = MaterialTheme.typography.titleMedium
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        "This ${goal.goalHorizon ?: "week"}",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    )
                                }
                                TextButton(onClick = {
                                    scope.launch {
                                        db.driftDao().updateStatus(goal.id, "done")
                                        goals = db.driftDao().getActiveGoals()
                                    }
                                }) {
                                    Text("Done ✓")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
