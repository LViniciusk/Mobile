package com.example.mygamelist.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF1F1F1F),
    onPrimary = Color.White,
    secondary = Color(0xFFBB86FC),
    onSecondary = Color.Black,
    background = Color(0xFF121212),
    surface = Color(0xFF1F1F1F),
    onSurface = Color.White
)

@Composable
fun MyGameListTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = androidx.compose.material3.Typography(),
        content = content
    )
}
