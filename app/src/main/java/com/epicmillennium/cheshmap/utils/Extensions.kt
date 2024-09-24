package com.epicmillennium.cheshmap.utils

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import com.epicmillennium.cheshmap.core.ui.theme.AppThemeMode
import com.epicmillennium.cheshmap.core.ui.theme.DarkTheme

// Compose states
@Composable
fun AppThemeMode.retrieveDarkThemeFromState(): DarkTheme = when (this) {
    AppThemeMode.MODE_AUTO -> DarkTheme(isSystemInDarkTheme())
    AppThemeMode.MODE_DAY -> DarkTheme(false)
    AppThemeMode.MODE_NIGHT -> DarkTheme(true)
}