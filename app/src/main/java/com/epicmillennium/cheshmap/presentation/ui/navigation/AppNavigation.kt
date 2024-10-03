package com.epicmillennium.cheshmap.presentation.ui.navigation

import androidx.navigation.NavHostController
import com.epicmillennium.cheshmap.domain.marker.WaterSource
import kotlinx.serialization.Serializable

@Serializable
object FavouriteScreen

@Serializable
object LendingScreen

@Serializable
object AccountScreen

@Serializable
data class DetailsScreen(val waterSourceId: String)

/**
 * Models the navigation actions in the app.
 */
class AppNavigationActions(private val navController: NavHostController) {
    fun navigateToLending() = navController.navigate(LendingScreen) {
        popUpTo(LendingScreen) { inclusive = true }
    }

    fun navigateToWaterSourceDetails(waterSourceId: String)= navController.navigate(DetailsScreen(waterSourceId)) {
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