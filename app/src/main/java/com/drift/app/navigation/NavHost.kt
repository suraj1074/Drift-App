package com.drift.app.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.drift.app.screens.*

@Composable
fun DriftNavHost() {
    val context = LocalContext.current
    val navController = rememberNavController()

    val isSetupComplete = context
        .getSharedPreferences("drift_prefs", android.content.Context.MODE_PRIVATE)
        .getBoolean("setup_complete", false)

    val startRoute = if (isSetupComplete) "today" else "welcome"

    NavHost(navController = navController, startDestination = startRoute) {
        composable("welcome") { WelcomeScreen(navController) }
        composable("today") { TodayScreen(navController) }
        composable("dump") { BrainDumpScreen(navController) }
        composable("goals") { GoalsScreen(navController) }
        composable("stuff") { MyStuffScreen(navController) }
    }
}
