package com.clicks.omnihub.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Airframe Design System Tokens (exact values from `tokens.json` & `tokens.css`).
 * Supports both Ivory (default editorial warm daylight) and Night themes.
 */
@Immutable
data class AirframeColors(
    val isNight: Boolean,
    val ground: Color,
    val surface: Color,
    val surfaceRaised: Color,
    val line: Color,
    val lineStrong: Color,
    val ink: Color,
    val text: Color,
    val textMuted: Color,
    val forest: Color,
    val onForest: Color,
    val onForestVeil: Color,
    val sage: Color,
    val sageSoft: Color,
    val sageWash: Color,
    val capsuleDone: Color,
    val track: Color,
    val signal: Color,
    val alert: Color,
    val alertSoft: Color
)

val AirframeIvoryPalette = AirframeColors(
    isNight = false,
    ground = Color(0xFFF7F6F2),
    surface = Color(0xFFEDECE6),
    surfaceRaised = Color(0xFFF9F8F5),
    line = Color(0xFFE3E1DB),
    lineStrong = Color(0xFFCFCDC5),
    ink = Color(0xFF17201B),
    text = Color(0xFF2B2D2A),
    textMuted = Color(0xFF676862),
    forest = Color(0xFF1E3D2B),
    onForest = Color(0xFFFFFFFF),
    onForestVeil = Color(0x24FFFFFF),
    sage = Color(0xFFDCE4CB),
    sageSoft = Color(0xFFE6E9DA),
    sageWash = Color(0xFFEEF0E4),
    capsuleDone = Color(0xFFC9D6B0),
    track = Color(0xFFE6E4DE),
    signal = Color(0xFF3F8F5A),
    alert = Color(0xFF9B2C2C),
    alertSoft = Color(0xFFF6E3DF)
)

val AirframeNightPalette = AirframeColors(
    isNight = true,
    ground = Color(0xFF111412),
    surface = Color(0xFF1C201D),
    surfaceRaised = Color(0xFF171A18),
    line = Color(0xFF2A2F2B),
    lineStrong = Color(0xFF3E443F),
    ink = Color(0xFFEEF0EA),
    text = Color(0xFFD8DBD4),
    textMuted = Color(0xFF9EA39B),
    forest = Color(0xFF8FC4A0),
    onForest = Color(0xFF0F1A13),
    onForestVeil = Color(0x290F1A13),
    sage = Color(0xFF2A3A2E),
    sageSoft = Color(0xFF223026),
    sageWash = Color(0xFF1A221C),
    capsuleDone = Color(0xFF3F5A47),
    track = Color(0xFF262B27),
    signal = Color(0xFF6CC48A),
    alert = Color(0xFFF29B8F),
    alertSoft = Color(0xFF3A1E1B)
)

/**
 * Airframe Typography Scale (Display = Serif, Text = Sans).
 */
object AirframeType {
    val numeralXl = TextStyle(
        fontFamily = FontFamily.Serif,
        fontSize = 86.sp,
        lineHeight = 72.sp,
        fontWeight = FontWeight.ExtraBold,
        letterSpacing = (-3.5).sp
    )
    val numeralUnit = TextStyle(
        fontFamily = FontFamily.Serif,
        fontSize = 30.sp,
        lineHeight = 30.sp,
        fontWeight = FontWeight.Light
    )
    val titleXl = TextStyle(
        fontFamily = FontFamily.Serif,
        fontSize = 34.sp,
        lineHeight = 36.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = (-0.7).sp
    )
    val titleLg = TextStyle(
        fontFamily = FontFamily.Serif,
        fontSize = 30.sp,
        lineHeight = 33.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = (-0.6).sp
    )
    val statValue = TextStyle(
        fontFamily = FontFamily.Serif,
        fontSize = 21.sp,
        lineHeight = 25.sp,
        fontWeight = FontWeight.Bold
    )
    val wordmark = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontSize = 16.sp,
        lineHeight = 20.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 5.2.sp
    )
    val eyebrow = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        fontWeight = FontWeight.SemiBold,
        letterSpacing = 2.8.sp
    )
    val lede = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontSize = 17.sp,
        lineHeight = 24.sp,
        fontWeight = FontWeight.Light
    )
    val button = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontSize = 17.sp,
        lineHeight = 17.sp,
        fontWeight = FontWeight.SemiBold
    )
    val body = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontSize = 15.sp,
        lineHeight = 22.sp,
        fontWeight = FontWeight.Normal
    )
    val rowTitle = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontSize = 16.sp,
        lineHeight = 21.sp,
        fontWeight = FontWeight.Normal
    )
    val rowTitleUnread = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontSize = 16.sp,
        lineHeight = 21.sp,
        fontWeight = FontWeight.SemiBold
    )
    val bodyLight = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        fontWeight = FontWeight.Light
    )
    val caption = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontSize = 13.sp,
        lineHeight = 16.sp,
        fontWeight = FontWeight.Normal
    )
    val meta = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontSize = 11.sp,
        lineHeight = 14.sp,
        fontWeight = FontWeight.SemiBold,
        letterSpacing = 1.7.sp
    )
}

/**
 * Airframe Spacing & Dimension Tokens (`tokens.json`).
 */
object AirframeSpace {
    val space1 = 4.dp
    val space2 = 8.dp
    val space3 = 12.dp
    val space4 = 16.dp
    val space5 = 20.dp
    val gutter = 26.dp
    val section = 26.dp
    val top = 26.dp

    val radiusXs = 6.dp
    val radiusSm = 10.dp
    val radiusMd = 12.dp
    val radiusLg = 16.dp
    val radiusXl = 20.dp

    val sizeTouch = 44.dp
    val sizeButton = 52.dp
    val sizeAvatar = 44.dp
    val sizeCapsuleWidth = 13.dp
    val sizeCapsuleHeight = 40.dp
    val sizeCapsuleCurrentHeight = 46.dp
    val sizeTabBar = 80.dp
}

val LocalAirframeColors = staticCompositionLocalOf { AirframeIvoryPalette }

object AirframeTheme {
    val colors: AirframeColors
        @Composable
        get() = LocalAirframeColors.current
}

@Composable
fun OmniHubTheme(
    isNightTheme: Boolean = false,
    content: @Composable () -> Unit
) {
    val palette = if (isNightTheme) AirframeNightPalette else AirframeIvoryPalette
    CompositionLocalProvider(LocalAirframeColors provides palette) {
        content()
    }
}
