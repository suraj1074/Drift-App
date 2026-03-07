package com.drift.app.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.drift.app.data.DriftDatabase
import com.drift.app.data.DriftItem
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyStuffScreen(navController: NavController) {
    val context = LocalContext.current
    val db = remember { DriftDatabase.get(context) }
    val scope = rememberCoroutineScope()

    var items by remember { mutableStateOf<List<DriftItem>>(emptyList()) }

    LaunchedEffect(Unit) {
        items = db.driftDao().getActiveTasks()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Stuff") },
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
        if (items.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("📭", fontSize = 32.sp)
                    Spacer(Modifier.height(12.dp))
                    Text(
                        "Nothing here yet.",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "Hit Dump to add what's on your mind.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(items) { item ->
                    val daysSinceTouched = daysSince(item.lastTouchedAt)
                    val isDrifting = daysSinceTouched >= 5

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isDrifting)
                                MaterialTheme.colorScheme.errorContainer
                            else MaterialTheme.colorScheme.surface
                        ),
                        elevation = CardDefaults.cardElevation(
                            defaultElevation = if (isDrifting) 0.dp else 1.dp
                        )
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                item.text,
                                style = MaterialTheme.typography.titleMedium
                            )

                            Spacer(modifier = Modifier.height(6.dp))

                            // Drift status
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = if (isDrifting)
                                    MaterialTheme.colorScheme.error.copy(alpha = 0.1f)
                                else MaterialTheme.colorScheme.surfaceVariant
                            ) {
                                Text(
                                    text = when {
                                        daysSinceTouched == 0L -> "✓ Touched today"
                                        isDrifting -> "⚠️ Drifting — $daysSinceTouched days"
                                        else -> "$daysSinceTouched days ago"
                                    },
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = if (isDrifting)
                                            MaterialTheme.colorScheme.error
                                        else MaterialTheme.colorScheme.onSurfaceVariant,
                                        fontWeight = if (isDrifting) FontWeight.Medium else FontWeight.Normal
                                    )
                                )
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            // Actions row
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                ActionChip("👆 Touch", onClick = {
                                    scope.launch {
                                        db.driftDao().touch(item.id)
                                        items = db.driftDao().getActiveTasks()
                                    }
                                })
                                ActionChip("✓ Done", onClick = {
                                    scope.launch {
                                        db.driftDao().updateStatus(item.id, "done")
                                        items = db.driftDao().getActiveTasks()
                                    }
                                })
                                ActionChip("💤 Pause", onClick = {
                                    scope.launch {
                                        db.driftDao().updateStatus(item.id, "paused")
                                        items = db.driftDao().getActiveTasks()
                                    }
                                })
                                ActionChip("👋 Let go", onClick = {
                                    scope.launch {
                                        db.driftDao().updateStatus(item.id, "let_go")
                                        items = db.driftDao().getActiveTasks()
                                    }
                                })
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ActionChip(label: String, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.surfaceVariant
    ) {
        Text(
            label,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            style = MaterialTheme.typography.labelSmall.copy(
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 12.sp
            )
        )
    }
}

private fun daysSince(dateStr: String): Long {
    return try {
        val then = LocalDateTime.parse(dateStr, DateTimeFormatter.ISO_LOCAL_DATE_TIME)
        ChronoUnit.DAYS.between(then, LocalDateTime.now())
    } catch (e: Exception) {
        0L
    }
}
