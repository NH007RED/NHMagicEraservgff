package com.nh.magiceraser

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

object NH {
    val Bg = Color(0xFF07070F)
    val Surface = Color(0xFF12121F)
    val Surface2 = Color(0xFF1B1B2E)
    val Violet = Color(0xFF7C5CFF)
    val Cyan = Color(0xFF22D3EE)
    val Pink = Color(0xFFFF5CAA)
    val Amber = Color(0xFFFFB84D)
    val Green = Color(0xFF34D399)
    val TextHi = Color(0xFFF4F4FF)
    val TextLo = Color(0xFF9A9AB8)

    val Accent: Brush get() = Brush.horizontalGradient(listOf(Violet, Pink))
    val Cool: Brush get() = Brush.horizontalGradient(listOf(Cyan, Violet))
}

@Composable
fun NHTheme(content: @Composable () -> Unit) {
    val scheme = darkColorScheme(
        primary = NH.Violet,
        onPrimary = Color.White,
        secondary = NH.Cyan,
        tertiary = NH.Pink,
        background = NH.Bg,
        surface = NH.Surface,
        onSurface = NH.TextHi,
        onBackground = NH.TextHi
    )
    MaterialTheme(colorScheme = scheme) {
        CompositionLocalProvider(LocalContentColor provides NH.TextHi) { content() }
    }
}
