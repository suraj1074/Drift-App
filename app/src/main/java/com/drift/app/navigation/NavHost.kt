package com.drift.app.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.drift.app.screens.*

@Composable
fun DriftNavHost() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "today") {
        composable("today") { TodayScreen(navController) }
        composable("dump") { BrainDumpScreen(navController) }
        composable("goals") { GoalsScreen(navController) }
        composable("stuff") { MyStuffScreen(navController) }
    }
}
