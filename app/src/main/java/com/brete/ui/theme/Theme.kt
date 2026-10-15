package com.brete.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

val Typography = Typography(
    bodyLarge = androidx.compose.ui.text.TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp
    )
)

// Definición del color amarillo principal extraído de tu logo
val YellowPrimary = Color(0xFFFFC107)

private val DarkColorScheme = darkColorScheme(
    primary = YellowPrimary,                  // Cambiado a amarillo para el modo oscuro
    background = Color(0xFF121212),
    surface = Color(0xFF1E1E1E),
    onPrimary = Color.Black,                  // Texto negro sobre botones amarillos para legibilidad
    onBackground = Color.White,
    onSurface = Color.White,
    onSurfaceVariant = Color(0xFFB0B0B0),
    outlineVariant = Color(0xFF2C2C2C)
)

private val LightColorScheme = lightColorScheme(
    primary = YellowPrimary,                  // Cambiado a amarillo para el modo claro
    background = Background,
    surface = White,
    onPrimary = Color.Black,                  // Texto negro sobre botones amarillos para legibilidad
    onBackground = TextPrimary,
    onSurface = TextPrimary,
    onSurfaceVariant = TextSecondary,
    outlineVariant = Border
)

@Composable
fun BreteTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}