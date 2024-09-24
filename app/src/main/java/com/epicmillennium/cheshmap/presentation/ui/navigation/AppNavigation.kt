package com.epicmillennium.cheshmap.presentation.ui.navigation

import androidx.navigation.NavHostController
import kotlinx.serialization.Serializable

@Serializable
object FavouriteScreen

@Serializable
object LendingScreen

@Serializable
object AccountScreen

/**
 * Models the navigation actions in the app.
 */
class AppNavigationActions(private val navController: NavHostController) {
    fun navigateToLending() = navController.navigate(LendingScreen) {
        popUpTo(LendingScreen) { inclusive = true }
    }
}

@Serializable
sealed class BottomNavigationScreens<T>(val name: String, val route: T) {
    @Serializable
    data object Favourite : BottomNavigationScreens<FavouriteScreen>(
        name = "Favourite",
        route = FavouriteScreen
    )

    @Serializable
    data object Lending : BottomNavigationScreens<LendingScreen>(
        name = "Map",
        route = LendingScreen
    )

    @Serializable
    data object Account : BottomNavigationScreens<AccountScreen>(
        name = "Account",
        route = AccountScreen
    )
}