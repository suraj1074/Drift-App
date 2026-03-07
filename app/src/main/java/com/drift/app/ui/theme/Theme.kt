package com.drift.app.ui.theme

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// Warm, calm palette — not corporate purple, more like a journal
private val DriftColorScheme = lightColorScheme(
    primary = Color(0xFF5B5BD6),           // Softer indigo
    onPrimary = Color.White,
    primaryContainer = Color(0xFFEEEDFF),   // Light lavender for focus cards
    onPrimaryContainer = Color(0xFF2D2B55),
    secondary = Color(0xFFF5A623),          // Warm amber for accents
    secondaryContainer = Color(0xFFFFF3E0), // Light amber
    onSecondaryContainer = Color(0xFF4E3B1F),
    background = Color(0xFFFCFBF8),         // Warm off-white, like paper
    surface = Color(0xFFFFFFFF),
    surfaceVariant = Color(0xFFF5F3EE),     // Warm grey for parked cards
    onBackground = Color(0xFF2C2C2C),       // Soft black, not harsh
    onSurface = Color(0xFF2C2C2C),
    onSurfaceVariant = Color(0xFF6B6B6B),
    outline = Color(0xFF9E9E9E),            // Readable secondary text
    outlineVariant = Color(0xFFE0DDD6),     // Subtle dividers
    error = Color(0xFFD4634B),              // Warm red for drift warnings
    errorContainer = Color(0xFFFFF0ED),     // Light warm red
    onError = Color.White,
    onErrorContainer = Color(0xFF5C1D0F),
)

private val DriftTypography = Typography(
    // Greeting / big headings
    headlineMedium = TextStyle(
        fontSize = 24.sp,
        fontWeight = FontWeight.SemiBold,
        lineHeight = 32.sp,
        letterSpacing = (-0.3).sp,
        color = Color(0xFF2C2C2C)
    ),
    // Section labels like "Focus today"
    titleSmall = TextStyle(
        fontSize = 13.sp,
        fontWeight = FontWeight.SemiBold,
        lineHeight = 18.sp,
        letterSpacing = 0.8.sp,
        color = Color(0xFF5B5BD6)
    ),
    // Card titles
    titleMedium = TextStyle(
        fontSize = 16.sp,
        fontWeight = FontWeight.Medium,
        lineHeight = 22.sp,
        color = Color(0xFF2C2C2C)
    ),
    // Body text / card descriptions
    bodyMedium = TextStyle(
        fontSize = 14.sp,
        fontWeight = FontWeight.Normal,
        lineHeight = 20.sp,
        color = Color(0xFF6B6B6B)
    ),
    // Small labels
    labelSmall = TextStyle(
        fontSize = 12.sp,
        fontWeight = FontWeight.Medium,
        lineHeight = 16.sp,
        color = Color(0xFF9E9E9E)
    ),
)

@Composable
fun DriftTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = DriftColorScheme,
        typography = DriftTypography,
        content = content
    )
}
