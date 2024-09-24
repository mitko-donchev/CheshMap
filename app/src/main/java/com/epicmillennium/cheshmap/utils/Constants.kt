package com.epicmillennium.cheshmap.utils

import android.Manifest
import androidx.datastore.preferences.core.intPreferencesKey

object Constants {

    // Permissions
    val CAMERAX_PERMISSIONS = listOf(
        Manifest.permission.CAMERA,
        Manifest.permission.RECORD_AUDIO,
    )

    // DataStore
    val GLOBAL_THEME_MODE_KEY = intPreferencesKey("global_theme_mode")

}