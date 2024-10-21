package com.epicmillennium.cheshmap.presentation.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.rememberNavController
import com.epicmillennium.cheshmap.app.android.MainViewModel
import com.epicmillennium.cheshmap.presentation.theme.CheshMapTheme
import com.epicmillennium.cheshmap.presentation.theme.DarkTheme
import com.epicmillennium.cheshmap.presentation.theme.LocalTheme
import com.epicmillennium.cheshmap.presentation.ui.navigation.AppNavGraph
import com.epicmillennium.cheshmap.presentation.ui.navigation.AppNavigationActions
import com.epicmillennium.cheshmap.utils.retrieveDarkThemeFromState

@Composable
fun CheshMapApp() {

    val mainViewModel = hiltViewModel<MainViewModel>()

    val globalThemeState by mainViewModel.globalThemeState.collectAsStateWithLifecycle()

    CompositionLocalProvider(value = LocalTheme provides DarkTheme(globalThemeState.retrieveDarkThemeFromState().isDark)) {
        CheshMapTheme(darkTheme = LocalTheme.current.isDark) {
            val navController = rememberNavController()
            val navigationActions = remember(navController) {
                AppNavigationActions(navController)
            }

            Scaffold(
                modifier = Modifier.fillMaxSize(),
            ) { paddingValues ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = paddingValues.calculateTopPadding())
                ) {
                    AppNavGraph(
                        navController = navController,
                        navigationActions = navigationActions,
                    )
                }
            }
        }
    }
}