package com.drift.app.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.drift.app.data.AiService
import com.drift.app.data.DriftDatabase
import com.drift.app.data.DriftItem
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BrainDumpScreen(navController: NavController) {
    val context = LocalContext.current
    val db = remember { DriftDatabase.get(context) }
    val ai = remember { AiService.getInstance() }
    val scope = rememberCoroutineScope()

    var text by remember { mutableStateOf("") }
    var saved by remember { mutableStateOf(false) }
    var isProcessing by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Brain Dump") },
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
                text = "What's on your mind?",
                fontSize = 22.sp,
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Don't organize. Just dump everything — tasks, ideas, worries, plans.",
                fontSize = 15.sp,
                color = MaterialTheme.colorScheme.outline
            )

            Spacer(modifier = Modifier.height(20.dp))

            OutlinedTextField(
                value = text,
                onValueChange = { text = it; saved = false },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                placeholder = { Text("I need to file my taxes, finish that book, call mom, maybe start running again...") },
            )

            Spacer(modifier = Modifier.height(20.dp))

            Button(
                onClick = {
                    if (text.isNotBlank()) {
                        scope.launch {
                            isProcessing = true
                            val parsed = ai.parseDump(text)
                            parsed.forEach { item ->
                                db.driftDao().insert(
                                    DriftItem(text = item.text, category = item.category)
                                )
                            }
                            isProcessing = false
                            saved = true
                            text = ""
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = text.isNotBlank() && !isProcessing
            ) {
                if (isProcessing) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Processing...")
                } else {
                    Text("Dump it")
                }
            }

            if (saved) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Got it. Check Today's Focus to see what matters most.",
                    color = MaterialTheme.colorScheme.primary,
                    fontSize = 14.sp
                )
            }
        }
    }
}
