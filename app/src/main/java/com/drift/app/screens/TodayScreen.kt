package com.drift.app.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.drift.app.data.*
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TodayScreen(navController: NavController) {
    val context = LocalContext.current
    val db = remember { DriftDatabase.get(context) }
    val ai = remember { AiService.getInstance() }
    val scope = rememberCoroutineScope()

    var focus by remember { mutableStateOf<DailyFocus?>(null) }
    var isLoading by remember { mutableStateOf(true) }

    fun fetchFocus() {
        scope.launch {
            isLoading = true
            val items = db.driftDao().getActiveTasks()
            val goals = db.driftDao().getActiveGoals()
            val result = ai.getDailyFocus(items, goals)
            focus = result

            // Cache it
            db.driftDao().saveFocusCache(
                FocusCache(
                    focusText = serializeFocus(result),
                    date = LocalDate.now().toString(),
                    itemCount = items.size,
                    goalCount = goals.size
                )
            )
            isLoading = false
        }
    }

    LaunchedEffect(Unit) {
        val items = db.driftDao().getActiveTasks()
        val goals = db.driftDao().getActiveGoals()
        val today = LocalDate.now().toString()
        val cache = db.driftDao().getFocusCache()

        if (cache != null && cache.date == today
            && cache.itemCount == items.size && cache.goalCount == goals.size
        ) {
            val cached = deserializeFocus(cache.focusText)
            if (cached != null) {
                focus = cached
                isLoading = false
            } else {
                fetchFocus()
            }
        } else {
            fetchFocus()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Drift") },
                actions = {
                    IconButton(onClick = { fetchFocus() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = true, onClick = { },
                    icon = { Icon(Icons.Default.Star, contentDescription = "Today") },
                    label = { Text("Today") }
                )
                NavigationBarItem(
                    selected = false, onClick = { navController.navigate("dump") },
                    icon = { Icon(Icons.Default.Add, contentDescription = "Dump") },
                    label = { Text("Dump") }
                )
                NavigationBarItem(
                    selected = false, onClick = { navController.navigate("stuff") },
                    icon = { Icon(Icons.Default.List, contentDescription = "My Stuff") },
                    label = { Text("My Stuff") }
                )
            }
        }
    ) { padding ->
        if (isLoading) {
            Box(
                Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                    Spacer(Modifier.height(12.dp))
                    Text("Thinking about your day...", color = MaterialTheme.colorScheme.outline)
                }
            }
        } else {
            Column(
                Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp)
            ) {
                // Greeting
                Text(
                    text = focus?.greeting ?: "Hey!",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onBackground
                )

                Spacer(Modifier.height(20.dp))

                // Focus actions
                Text(
                    "Focus today",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.primary,
                    letterSpacing = 1.sp
                )
                Spacer(Modifier.height(8.dp))

                focus?.actions?.forEach { action ->
                    Card(
                        Modifier.fillMaxWidth().padding(bottom = 12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    ) {
                        Column(Modifier.padding(16.dp)) {
                            Text(
                                action.text,
                                fontSize = 17.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                action.why,
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                            )
                            if (action.goal != null) {
                                Spacer(Modifier.height(6.dp))
                                Text(
                                    "🎯 ${action.goal}",
                                    fontSize = 13.sp,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }

                // Parked items
                val parked = focus?.parked ?: emptyList()
                if (parked.isNotEmpty()) {
                    Spacer(Modifier.height(16.dp))
                    Text(
                        "Parked for now",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.outline,
                        letterSpacing = 1.sp
                    )
                    Spacer(Modifier.height(8.dp))

                    parked.forEach { item ->
                        Card(
                            Modifier.fillMaxWidth().padding(bottom = 8.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        ) {
                            Text(
                                item,
                                Modifier.padding(14.dp),
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                Spacer(Modifier.height(20.dp))

                OutlinedButton(
                    onClick = { navController.navigate("goals") },
                    Modifier.align(Alignment.CenterHorizontally)
                ) {
                    Text("My Goals")
                }
            }
        }
    }
}

// --- Serialization for cache ---

private fun serializeFocus(f: DailyFocus): String {
    val json = JSONObject()
    json.put("greeting", f.greeting)
    json.put("actions", org.json.JSONArray().apply {
        f.actions.forEach { a ->
            put(JSONObject().apply {
                put("text", a.text)
                put("why", a.why)
                put("goal", a.goal ?: JSONObject.NULL)
            })
        }
    })
    json.put("parked", org.json.JSONArray().apply { f.parked.forEach { put(it) } })
    return json.toString()
}

private fun deserializeFocus(s: String): DailyFocus? {
    return try {
        val json = JSONObject(s)
        val actions = json.getJSONArray("actions")
        val parked = json.optJSONArray("parked") ?: org.json.JSONArray()
        DailyFocus(
            greeting = json.getString("greeting"),
            actions = (0 until actions.length()).map { i ->
                val a = actions.getJSONObject(i)
                FocusAction(a.getString("text"), a.getString("why"),
                    if (a.isNull("goal")) null else a.optString("goal"))
            },
            parked = (0 until parked.length()).map { parked.getString(it) }
        )
    } catch (e: Exception) {
        null // Old format or corrupt — force a fresh fetch
    }
}
