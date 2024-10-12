package com.epicmillennium.cheshmap.presentation.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.epicmillennium.cheshmap.app.android.MainViewModel
import com.epicmillennium.cheshmap.core.ui.theme.CheshMapTheme
import com.epicmillennium.cheshmap.core.ui.theme.DarkTheme
import com.epicmillennium.cheshmap.core.ui.theme.LocalTheme
import com.epicmillennium.cheshmap.presentation.ui.lending.LendingScreen
import com.epicmillennium.cheshmap.utils.retrieveDarkThemeFromState

@Composable
fun CheshMapApp() {

    val mainViewModel = hiltViewModel<MainViewModel>()

    val globalThemeState by mainViewModel.globalThemeState.collectAsStateWithLifecycle()

    CompositionLocalProvider(value = LocalTheme provides DarkTheme(globalThemeState.retrieveDarkThemeFromState().isDark)) {
        CheshMapTheme(darkTheme = LocalTheme.current.isDark) {
            Scaffold(
                modifier = Modifier.fillMaxSize(),
            ) { paddingValues ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = paddingValues.calculateTopPadding())
                ) {
                    LendingScreen()
                }
            }
        }
    }
}