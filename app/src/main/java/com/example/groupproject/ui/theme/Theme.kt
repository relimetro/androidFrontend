package com.example.groupproject.ui.theme

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightHealthColorScheme = lightColorScheme(
    primary = BluePrimary,
    onPrimary = Color.White,
    primaryContainer = BluePrimaryContainer,
    onPrimaryContainer = BluePrimary,

    secondary = BlueSecondary,
    onSecondary = Color.White,

    tertiary = BlueTertiary,
    onTertiary = Color.White,

    background = BackgroundLight,
    onBackground = TextPrimary,

    surface = SurfaceLight,
    onSurface = TextPrimary,

    surfaceVariant = BackgroundLight,
    onSurfaceVariant = TextSecondary,

    error = ErrorRed,
    onError = Color.White,

    outline = DividerLight
)

@Composable
fun GroupProjectTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = LightHealthColorScheme,
        typography = Typography(), // default = clean & readable
        content = content
    )
}