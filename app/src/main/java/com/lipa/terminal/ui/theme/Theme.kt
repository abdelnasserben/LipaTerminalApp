package com.lipa.terminal.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LipaColorScheme = lightColorScheme(
    primary = LipaColors.Green,
    onPrimary = Color.White,
    background = LipaColors.Bg,
    onBackground = LipaColors.Ink,
    surface = LipaColors.Card,
    onSurface = LipaColors.Ink,
    error = LipaColors.Error,
    onError = Color.White,
)

@Composable
fun LipaTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = LipaColorScheme,
        content = content,
    )
}
