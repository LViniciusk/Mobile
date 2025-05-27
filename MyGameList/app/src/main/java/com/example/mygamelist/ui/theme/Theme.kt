package com.example.mygamelist.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

enum class ThemePreference {
    LIGHT, DARK, SYSTEM
}


@Composable
fun MyGameListTheme(content: @Composable () -> Unit) {

    val systemIsDark = isSystemInDarkTheme()

    //val useDarkTheme = when (currentPreference) {
    //    ThemePreference.LIGHT -> false
    //    ThemePreference.DARK -> true
    //    ThemePreference.SYSTEM -> systemIsDark
    //}

    val colors = DarkColorScheme




    MaterialTheme(
        colorScheme = colors,
        typography = androidx.compose.material3.Typography(),
        content = content
    )
}
