package com.epicmillennium.cheshmap.presentation.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.epicmillennium.cheshmap.presentation.ui.lending.LendingScreen
import com.epicmillennium.cheshmap.presentation.ui.login.LoginScreen

@Composable
fun AppNavGraph(
    navController: NavHostController = rememberNavController(),
    navigationActions: AppNavigationActions,
) {
    NavHost(navController = navController, startDestination = Lending("", false)) {
        composable<Lending> {
            val args = it.toRoute<Lending>()
            LendingScreen(
                args.waterSourceId,
                args.isPickingLocationForNewWaterSource,
                navigationActions
            )
        }
        composable<Login> {
            val args = it.toRoute<Login>()
            LoginScreen(
                args.waterSourceId,
                args.isPickingLocationForNewWaterSource,
                navigationActions
            )
        }
    }
}