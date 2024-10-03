package com.epicmillennium.cheshmap.presentation.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.epicmillennium.cheshmap.presentation.ui.account.AccountScreen
import com.epicmillennium.cheshmap.presentation.ui.details.DetailsScreen
import com.epicmillennium.cheshmap.presentation.ui.favourite.FavouriteScreen
import com.epicmillennium.cheshmap.presentation.ui.lending.LendingScreen

@Composable
fun AppNavGraph(
    modifier: Modifier,
    navController: NavHostController = rememberNavController(),
    navigationActions: AppNavigationActions
) {
    NavHost(modifier = modifier, navController = navController, startDestination = LendingScreen) {
        composable<FavouriteScreen> {
            FavouriteScreen()
        }
        composable<LendingScreen> {
            LendingScreen(navigationActions)
        }
        composable<AccountScreen> {
            AccountScreen()
        }
        composable<DetailsScreen> {
            val args = it.toRoute<DetailsScreen>()
            DetailsScreen(args.waterSourceId)
        }
    }
}