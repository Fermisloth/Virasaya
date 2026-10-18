package com.virasaya.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val VirasayaColors = lightColorScheme(
    primary = Color(0xFF171717), onPrimary = Color.White,
    background = Color(0xFFFAFAFA), surface = Color.White,
    onBackground = Color(0xFF171717), onSurface = Color(0xFF171717)
)

@Composable
fun VirasayaTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = VirasayaColors, content = content)
}
