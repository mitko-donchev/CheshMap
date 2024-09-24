package com.epicmillennium.cheshmap.presentation.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.rememberNavController
import com.epicmillennium.cheshmap.app.android.MainViewModel
import com.epicmillennium.cheshmap.core.ui.theme.CheshMapTheme
import com.epicmillennium.cheshmap.core.ui.theme.DarkTheme
import com.epicmillennium.cheshmap.core.ui.theme.LocalTheme
import com.epicmillennium.cheshmap.presentation.ui.navigation.AppBottomNavigation
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

            Surface {
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    bottomBar = { AppBottomNavigation(navController = navController) }
                ) { paddingValues ->
                    AppNavGraph(
                        modifier = Modifier.padding(paddingValues),
                        navController = navController,
                        navigationActions = navigationActions
                    )
                }
            }
        }
    }
}