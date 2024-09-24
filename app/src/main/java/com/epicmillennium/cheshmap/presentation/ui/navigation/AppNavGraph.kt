package com.epicmillennium.cheshmap.presentation.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.epicmillennium.cheshmap.presentation.ui.account.AccountScreen
import com.epicmillennium.cheshmap.presentation.ui.favourite.FavouriteScreen
import com.epicmillennium.cheshmap.presentation.ui.lending.LendingScreen

@Composable
fun AppNavGraph(
    navController: NavHostController = rememberNavController(),
    navigationActions: AppNavigationActions
) {
    NavHost(navController = navController, startDestination = LendingScreen) {
        composable<FavouriteScreen> {
            FavouriteScreen()
        }
        composable<LendingScreen> {
            LendingScreen()
        }
        composable<AccountScreen> {
            AccountScreen()
        }
    }
}