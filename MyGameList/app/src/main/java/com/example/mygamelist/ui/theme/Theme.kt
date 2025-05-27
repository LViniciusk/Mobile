package com.example.mygamelist.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.example.mygamelist.viewmodel.ThemeViewModel

enum class ThemePreference {
    LIGHT, DARK, SYSTEM
}

@Composable
fun MyGameListTheme(
    themeViewModel: ThemeViewModel,
    content: @Composable () -> Unit
) {
    val currentPreference by themeViewModel.themePreference.collectAsState()
    val systemIsDark = isSystemInDarkTheme()

    val useDarkTheme = when (currentPreference) {
        ThemePreference.LIGHT -> false
        ThemePreference.DARK -> true
        ThemePreference.SYSTEM -> systemIsDark
    }

    val colors = if (useDarkTheme) {
        DarkColorScheme
    } else {
        LightColorScheme
    }

    MaterialTheme(
        colorScheme = colors,
        typography = Typography(),
        content = content
    )
}