package com.clicks.omnihub.ui.theme

import android.os.Build
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

val M3PrimaryBlue = Color(0xFFA8C7FA)
val M3OnPrimaryBlue = Color(0xFF062E6F)
val M3PrimaryContainer = Color(0xFF0842A0)
val M3OnPrimaryContainer = Color(0xFFD3E3FD)

val M3SecondaryTeal = Color(0xFF7FCFFF)
val M3SurfaceBackground = Color(0xFF0E1117)
val M3SurfaceContainer = Color(0xFF171B24)
val M3SurfaceContainerHigh = Color(0xFF1F2430)
val M3OutlineVariant = Color(0xFF2E3545)
val M3TextPrimary = Color(0xFFE3E8F0)
val M3TextSecondary = Color(0xFF9AA5B8)

private val DefaultM3DarkScheme = darkColorScheme(
    primary = M3PrimaryBlue,
    onPrimary = M3OnPrimaryBlue,
    primaryContainer = M3PrimaryContainer,
    onPrimaryContainer = M3OnPrimaryContainer,
    secondary = M3SecondaryTeal,
    background = M3SurfaceBackground,
    surface = M3SurfaceBackground,
    surfaceVariant = M3SurfaceContainer,
    onBackground = M3TextPrimary,
    onSurface = M3TextPrimary,
    onSurfaceVariant = M3TextSecondary,
    outline = M3OutlineVariant,
    outlineVariant = M3OutlineVariant
)

@Composable
fun OmniHubTheme(content: @Composable () -> Unit) {
    val context = LocalContext.current
    val colorScheme = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        dynamicDarkColorScheme(context).copy(
            background = M3SurfaceBackground,
            surface = M3SurfaceBackground,
            surfaceVariant = M3SurfaceContainer
        )
    } else {
        DefaultM3DarkScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}
