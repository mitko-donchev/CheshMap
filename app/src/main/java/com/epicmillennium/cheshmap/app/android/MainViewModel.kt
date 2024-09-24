package com.epicmillennium.cheshmap.app.android

import androidx.datastore.preferences.core.Preferences
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.epicmillennium.cheshmap.core.ui.theme.AppThemeMode
import com.epicmillennium.cheshmap.utils.Constants.GLOBAL_THEME_MODE_KEY
import com.epicmillennium.cheshmap.utils.preferences.UserPreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val userPreferencesRepository: UserPreferencesRepository
) : ViewModel() {

    private val _globalThemeState = MutableStateFlow(AppThemeMode.MODE_NIGHT)
    val globalThemeState: StateFlow<AppThemeMode> = _globalThemeState

    init {
        fetchGlobalThemeMode()
    }

    private fun fetchGlobalThemeMode() = viewModelScope.launch {
        userPreferencesRepository.dataStore.data.map { preferences: Preferences ->
            AppThemeMode.fromOrdinal(
                preferences[GLOBAL_THEME_MODE_KEY] ?: AppThemeMode.MODE_NIGHT.ordinal
            )
        }.collect { themeMode ->
            _globalThemeState.value = themeMode
        }
    }
}