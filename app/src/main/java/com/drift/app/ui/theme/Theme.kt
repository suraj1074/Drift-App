package com.drift.app.ui.theme

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DriftColorScheme = lightColorScheme(
    primary = Color(0xFF6C63FF),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFF0EEFF),
    onPrimaryContainer = Color(0xFF1A1A2E),
    secondary = Color(0xFF5A52D5),
    background = Color(0xFFFAFAFA),
    surface = Color.White,
    onBackground = Color(0xFF1A1A2E),
    onSurface = Color(0xFF1A1A2E),
    outline = Color(0xFFE0E0E0),
)

@Composable
fun DriftTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = DriftColorScheme,
        typography = Typography(),
        content = content
    )
}
