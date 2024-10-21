package com.epicmillennium.cheshmap.presentation.ui.navigation

import androidx.navigation.NavController
import kotlinx.serialization.Serializable

@Serializable
object SplashScreen

@Serializable
data class Lending(val waterSourceId: String, val isPickingLocationForNewWaterSource: Boolean)

@Serializable
data class Login(val waterSourceId: String, val isPickingLocationForNewWaterSource: Boolean)

/**
 * Models the navigation actions in the app.
 */
class AppNavigationActions(private val navController: NavController) {
    fun navigateToLendingFromSplashScreen() = navController.navigate(Lending) {
        popUpTo(SplashScreen) { inclusive = true }
        popUpTo(Lending) { inclusive = true }
    }

    fun navigateToLending(
        waterSourceId: String = "",
        isPickingLocationForNewWaterSource: Boolean = false
    ) = navController.navigate(Lending(waterSourceId, isPickingLocationForNewWaterSource)) {
        popUpTo(Lending(waterSourceId, isPickingLocationForNewWaterSource)) { inclusive = true }
    }

    fun navigateToLogin(
        waterSourceId: String = "",
        isPickingLocationForNewWaterSource: Boolean = false
    ) = navController.navigate(Login(waterSourceId, isPickingLocationForNewWaterSource))
}