package com.virasaya.ui

import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.ui.unit.dp

@Composable
fun AppIcon(name: String, description: String? = null, modifier: Modifier = Modifier, color: Color = Color.White) {
    val image = remember(name) {
        val path = when (name) {
            "back" -> "M15 18l-6-6 6-6"
            "arrow" -> "M5 12h14M13 6l6 6-6 6"
            "search" -> "M21 21l-5-5M18 10a8 8 0 1 1-16 0 8 8 0 0 1 16 0"
            "pin" -> "M20 10c0 6-8 12-8 12S4 16 4 10a8 8 0 0 1 16 0M15 10a3 3 0 1 1-6 0 3 3 0 0 1 6 0"
            "map" -> "M9 3L2 6v15l7-3 6 3 7-3V3l-7 3-6-3M9 3v15M15 6v15"
            "bookmark" -> "M6 21V5a2 2 0 0 1 2-2h8a2 2 0 0 1 2 2v16l-6-4-6 4"
            "home" -> "M3 10l9-7 9 7v10a1 1 0 0 1-1 1h-5v-7H9v7H4a1 1 0 0 1-1-1V10"
            "swap" -> "M7 3v18M3 7l4-4 4 4M17 21V3M13 17l4 4 4-4"
            "close" -> "M6 6l12 12M6 18L18 6"
            "walk" -> "M14 4a1.5 1.5 0 1 1-3 0 1.5 1.5 0 0 1 3 0M7 21l4-8M16 21l-2-7-3-3 1-4M5 12l3-4 4-1 4 5h4"
            "source" -> "M14 3h7v7M10 14L21 3M10 3H5a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-5"
            else -> "M12 3v18M3 12h18"
        }
        ImageVector.Builder(name, 24.dp, 24.dp, 24f, 24f).addPath(
            pathData = PathParser().parsePathString(path).toNodes(),
            stroke = SolidColor(Color.White), strokeLineWidth = 1.7f,
            strokeLineCap = StrokeCap.Round, strokeLineJoin = StrokeJoin.Round
        ).build()
    }
    Icon(image, description, modifier, tint = color)
}
