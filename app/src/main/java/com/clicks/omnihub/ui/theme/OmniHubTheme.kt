package com.clicks.omnihub.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val ClicksOrange = Color(0xFFF97316)
val ClicksOrangeGlow = Color(0x33F97316)
val ObsidianBackground = Color(0xFF090A0D)
val ObsidianSurface = Color(0xFF12141B)
val ObsidianCard = Color(0xFF161922)
val ObsidianCardSelected = Color(0xFF1D2230)
val ObsidianBorder = Color(0xFF262B38)
val TextPrimary = Color(0xFFF3F4F6)
val TextSecondary = Color(0xFF9CA3AF)
val TextMuted = Color(0xFF6B7280)

private val OmniHubDarkColorScheme = darkColorScheme(
    primary = ClicksOrange,
    onPrimary = Color.White,
    secondary = Color(0xFF38BDF8),
    background = ObsidianBackground,
    surface = ObsidianSurface,
    onBackground = TextPrimary,
    onSurface = TextPrimary,
    surfaceVariant = ObsidianCard,
    outline = ObsidianBorder
)

@Composable
fun OmniHubTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = OmniHubDarkColorScheme,
        content = content
    )
}
