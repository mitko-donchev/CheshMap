package com.epicmillennium.cheshmap.utils

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.core.app.ActivityCompat
import com.epicmillennium.cheshmap.core.ui.theme.AppThemeMode
import com.epicmillennium.cheshmap.core.ui.theme.DarkTheme

// Context
fun Context.checkLocationPermissions(): Boolean {
    return ActivityCompat.checkSelfPermission(
        this,
        Manifest.permission.ACCESS_FINE_LOCATION
    ) == PackageManager.PERMISSION_GRANTED
            && ActivityCompat.checkSelfPermission(
        this,
        Manifest.permission.ACCESS_COARSE_LOCATION
    ) == PackageManager.PERMISSION_GRANTED
}

// Compose states
@Composable
fun AppThemeMode.retrieveDarkThemeFromState(): DarkTheme = when (this) {
    AppThemeMode.MODE_AUTO -> DarkTheme(isSystemInDarkTheme())
    AppThemeMode.MODE_DAY -> DarkTheme(false)
    AppThemeMode.MODE_NIGHT -> DarkTheme(true)
}