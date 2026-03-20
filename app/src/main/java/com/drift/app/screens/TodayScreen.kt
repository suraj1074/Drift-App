package com.drift.app.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.drift.app.data.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.time.LocalDate

private val loadingQuotes = listOf(
    "Two things, not twenty.",
    "What matters most today?",
    "Clarity is a superpower.",
    "Less noise, more signal.",
    "You don't need to do it all.",
    "Small steps, big shifts.",
    "Focus is saying no to good ideas.",
    "Done is better than perfect.",
    "What would make today a win?",
    "Progress, not perfection.",
    "The best time to start is now.",
    "One thing at a time.",
    "Breathe. Then begin.",
    "Your future self will thank you.",
    "Drift less, do more."
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TodayScreen(navController: NavController) {
    val context = LocalContext.current
    val db = remember { DriftDatabase.get(context) }
    val ai = remember { AiService.getInstance() }
    val scope = rememberCoroutineScope()

    var focus by remember { mutableStateOf<DailyFocus?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var showSettings by remember { mutableStateOf(false) }

    fun fetchFocus() {
        scope.launch {
            isLoading = true
            val items = db.driftDao().getActiveTasks()
            val goals = db.driftDao().getActiveGoals()
            android.util.Log.d("Drift", "fetchFocus: calling backend with ${items.size} items, ${goals.size} goals")
            val result = ai.getDailyFocus(items, goals)
            android.util.Log.d("Drift", "fetchFocus: got result, isFallback=${result.isFallback}, greeting=${result.greeting}")
            focus = result

            // Only cache real AI responses, not fallbacks
            if (!result.isFallback) {
                android.util.Log.d("Drift", "fetchFocus: caching result")
                db.driftDao().saveFocusCache(
                    FocusCache(
                        focusText = serializeFocus(result),
                        date = LocalDate.now().toString(),
                        itemCount = items.size,
                        goalCount = goals.size
                    )
                )
            } else {
                android.util.Log.d("Drift", "fetchFocus: NOT caching (fallback)")
            }
            isLoading = false
        }
    }

    LaunchedEffect(Unit) {
        // Already have focus in memory (e.g. navigated back) — skip everything
        if (focus != null) {
            isLoading = false
            return@LaunchedEffect
        }

        try {
            val items = db.driftDao().getActiveTasks()
            val goals = db.driftDao().getActiveGoals()
            val today = LocalDate.now().toString()
            val cache = db.driftDao().getFocusCache()

            android.util.Log.d("Drift", "Cache check: cache=$cache, items=${items.size}, goals=${goals.size}, today=$today")

            if (cache != null && cache.date == today
                && cache.itemCount == items.size && cache.goalCount == goals.size
            ) {
                val cached = deserializeFocus(cache.focusText)
                if (cached != null) {
                    android.util.Log.d("Drift", "Using cached focus")
                    focus = cached
                    isLoading = false
                } else {
                    android.util.Log.d("Drift", "Cache corrupt, fetching fresh")
                    fetchFocus()
                }
            } else {
                android.util.Log.d("Drift", "Cache miss, fetching fresh")
                fetchFocus()
            }
        } catch (e: Exception) {
            android.util.Log.e("Drift", "LaunchedEffect error: ${e.message}", e)
            fetchFocus()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "drift",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                    )
                },
                actions = {
                    IconButton(onClick = { showSettings = true }) {
                        Icon(
                            Icons.Default.Settings,
                            contentDescription = "Settings",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    IconButton(onClick = { fetchFocus() }) {
                        Icon(
                            Icons.Default.Refresh,
                            contentDescription = "Refresh",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 0.dp
            ) {
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
            var quoteIndex by remember { mutableIntStateOf((0 until loadingQuotes.size).random()) }

            LaunchedEffect(Unit) {
                while (true) {
                    delay(3000)
                    quoteIndex = (quoteIndex + 1) % loadingQuotes.size
                }
            }

            Box(
                Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(
                        color = MaterialTheme.colorScheme.primary,
                        strokeWidth = 2.dp
                    )
                    Spacer(Modifier.height(24.dp))
                    AnimatedContent(
                        targetState = quoteIndex,
                        transitionSpec = {
                            fadeIn(animationSpec = androidx.compose.animation.core.tween(500)) togetherWith
                                fadeOut(animationSpec = androidx.compose.animation.core.tween(500))
                        },
                        label = "quote"
                    ) { index ->
                        Text(
                            loadingQuotes[index],
                            style = MaterialTheme.typography.bodyLarge.copy(
                                fontWeight = FontWeight.Medium
                            ),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 48.dp)
                        )
                    }
                }
            }
        } else {
            Column(
                Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp, vertical = 16.dp)
            ) {
                // Greeting — like a friend texting
                Text(
                    text = focus?.greeting ?: "Hey!",
                    style = MaterialTheme.typography.headlineMedium
                )

                Spacer(Modifier.height(28.dp))

                // Focus section
                Text(
                    "FOCUS TODAY",
                    style = MaterialTheme.typography.titleSmall
                )
                Spacer(Modifier.height(12.dp))

                focus?.actions?.forEach { action ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                    ) {
                        Column(Modifier.padding(20.dp)) {
                            Text(
                                "✦ ${action.text}",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.SemiBold
                                ),
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Spacer(Modifier.height(6.dp))
                            Text(
                                action.why,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                            )
                            if (action.goal != null) {
                                Spacer(Modifier.height(10.dp))
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                                ) {
                                    Text(
                                        "🎯 ${action.goal}",
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    )
                                }
                            }
                        }
                    }
                }

                // Parked items
                val parked = focus?.parked ?: emptyList()
                if (parked.isNotEmpty()) {
                    Spacer(Modifier.height(24.dp))
                    Text(
                        "PARKED FOR NOW",
                        style = MaterialTheme.typography.titleSmall.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                    Spacer(Modifier.height(10.dp))

                    parked.forEach { item ->
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 8.dp),
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant
                        ) {
                            Text(
                                "💤 $item",
                                modifier = Modifier.padding(16.dp),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                Spacer(Modifier.height(28.dp))

                // Goals link — feels like part of the flow, not an orphan button
                Surface(
                    onClick = { navController.navigate("goals") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.secondaryContainer
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("🌱", fontSize = 20.sp)
                        Spacer(Modifier.width(12.dp))
                        Column {
                            Text(
                                "My Goals",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                            Text(
                                "What would make this week a win?",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f)
                            )
                        }
                    }
                }

                Spacer(Modifier.height(24.dp))
            }
        }
    }

    // --- Settings Dialog ---
    if (showSettings) {
        var keyInput by remember { mutableStateOf(ai.geminiApiKey ?: "") }

        AlertDialog(
            onDismissRequest = { showSettings = false },
            title = { Text("Settings") },
            text = {
                Column {
                    Text(
                        "Bring your own Gemini API key for faster, higher-quality AI responses. Leave empty to use the free default.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(8.dp))
                    val uriHandler = androidx.compose.ui.platform.LocalUriHandler.current
                    Text(
                        "How do I get a key? →",
                        style = MaterialTheme.typography.labelMedium.copy(
                            color = MaterialTheme.colorScheme.primary
                        ),
                        modifier = Modifier.clickable {
                            uriHandler.openUri("https://suraj1074.github.io/Drift-Website/#setup")
                        }
                    )
                    Spacer(Modifier.height(16.dp))
                    OutlinedTextField(
                        value = keyInput,
                        onValueChange = { keyInput = it },
                        label = { Text("Gemini API Key") },
                        placeholder = { Text("AIza...") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    if (ai.geminiApiKey != null) {
                        Spacer(Modifier.height(8.dp))
                        Text(
                            "✓ Using your Gemini key",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    ai.setGeminiApiKey(context, keyInput.takeIf { it.isNotBlank() })
                    showSettings = false
                }) {
                    Text("Save")
                }
            },
            dismissButton = {
                if (ai.geminiApiKey != null) {
                    TextButton(onClick = {
                        ai.setGeminiApiKey(context, null)
                        keyInput = ""
                        showSettings = false
                    }) {
                        Text("Remove Key")
                    }
                } else {
                    TextButton(onClick = { showSettings = false }) {
                        Text("Cancel")
                    }
                }
            }
        )
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
        null
    }
}
