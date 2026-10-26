package com.virasaya.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val Orange = Color(0xFFFF8542)
val Ink = Color(0xFF11150F)
val Panel = Color(0xFF23291F)
val Muted = Color(0xFFB9BDB0)

private val VirasayaColors = darkColorScheme(
    primary = Orange, onPrimary = Ink,
    background = Ink, surface = Panel, surfaceVariant = Color(0xFF32382E),
    onBackground = Color(0xFFF7F6F1), onSurface = Color(0xFFF7F6F1),
    onSurfaceVariant = Muted, outline = Color(0xFF51574B), secondary = Color(0xFFCED9B5),
    secondaryContainer = Color(0xFF414A38), onSecondaryContainer = Color(0xFFF7F6F1),
    outlineVariant = Color(0xFF41483B)
)

private val AppType = Typography(
    headlineLarge = TextStyle(fontSize = 34.sp, lineHeight = 39.sp, fontWeight = FontWeight.SemiBold, letterSpacing = (-0.8).sp),
    headlineMedium = TextStyle(fontSize = 28.sp, lineHeight = 33.sp, fontWeight = FontWeight.SemiBold, letterSpacing = (-0.6).sp),
    titleLarge = TextStyle(fontSize = 21.sp, lineHeight = 27.sp, fontWeight = FontWeight.Medium),
    titleMedium = TextStyle(fontSize = 16.sp, lineHeight = 22.sp, fontWeight = FontWeight.Medium),
    bodyLarge = TextStyle(fontSize = 15.sp, lineHeight = 24.sp),
    bodyMedium = TextStyle(fontSize = 14.sp, lineHeight = 21.sp),
    bodySmall = TextStyle(fontSize = 12.sp, lineHeight = 18.sp),
    labelLarge = TextStyle(fontSize = 14.sp, lineHeight = 20.sp, fontWeight = FontWeight.Medium)
)

@Composable
fun VirasayaTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = VirasayaColors, typography = AppType, content = content)
}
