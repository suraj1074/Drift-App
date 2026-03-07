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
                }
            )
        }
    ) { padding ->
        if (items.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(24.dp),
                contentAlignment = androidx.compose.ui.Alignment.Center
            ) {
                Text(
                    "Nothing here yet. Hit Dump to add what's on your mind.",
                    color = MaterialTheme.colorScheme.outline,
                    fontSize = 16.sp
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(items) { item ->
                    val daysSinceTouched = daysSince(item.lastTouchedAt)
                    val isDrifting = daysSinceTouched >= 5

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isDrifting)
                                MaterialTheme.colorScheme.errorContainer
                            else MaterialTheme.colorScheme.surface
                        )
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(item.text, fontSize = 16.sp)

                            Spacer(modifier = Modifier.height(4.dp))

                            Text(
                                text = if (daysSinceTouched == 0L) "Touched today"
                                else if (isDrifting) "⚠️ Drifting — $daysSinceTouched days untouched"
                                else "$daysSinceTouched days ago",
                                fontSize = 13.sp,
                                color = if (isDrifting) MaterialTheme.colorScheme.error
                                else MaterialTheme.colorScheme.outline
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                TextButton(onClick = {
                                    scope.launch {
                                        db.driftDao().touch(item.id)
                                        items = db.driftDao().getActiveTasks()
                                    }
                                }) { Text("Touched it") }

                                TextButton(onClick = {
                                    scope.launch {
                                        db.driftDao().updateStatus(item.id, "done")
                                        items = db.driftDao().getActiveTasks()
                                    }
                                }) { Text("Done") }

                                TextButton(onClick = {
                                    scope.launch {
                                        db.driftDao().updateStatus(item.id, "let_go")
                                        items = db.driftDao().getActiveTasks()
                                    }
                                }) { Text("Let go") }

                                TextButton(onClick = {
                                    scope.launch {
                                        db.driftDao().updateStatus(item.id, "paused")
                                        items = db.driftDao().getActiveTasks()
                                    }
                                }) { Text("Pause") }
                            }
                        }
                    }
                }
            }
        }
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
