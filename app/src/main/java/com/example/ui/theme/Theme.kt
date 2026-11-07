package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val VectorRageDarkColors = darkColorScheme(
    primary = NeonCyan,
    onPrimary = CyberVoid,
    secondary = NeonRed,
    onSecondary = TextLight,
    tertiary = NeonYellow,
    background = CyberVoid,
    onBackground = TextLight,
    surface = CyberDark,
    onSurface = TextLight,
    surfaceVariant = CyberSurface,
    onSurfaceVariant = TextMuted
)

@Composable
fun MyApplicationTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = VectorRageDarkColors,
        typography = Typography,
        content = content
    )
}
