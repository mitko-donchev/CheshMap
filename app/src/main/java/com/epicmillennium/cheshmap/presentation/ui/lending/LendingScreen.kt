package com.epicmillennium.cheshmap.presentation.ui.lending

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.epicmillennium.cheshmap.presentation.ui.navigation.AppNavigationActions

@Composable
fun LendingScreen(
    waterSourceId: String,
    isPickingLocationForNewWaterSource: Boolean,
    navigationActions: AppNavigationActions
) {

    val lendingViewModel: LendingViewModel = hiltViewModel()

    val hasUser by lendingViewModel.hasUser.collectAsStateWithLifecycle()
    val currentUser by lendingViewModel.currentUser.collectAsStateWithLifecycle()
    val lendingUiState by lendingViewModel.lendingUiState.collectAsStateWithLifecycle()
    val latestUserLocation by lendingViewModel.userLocation.collectAsStateWithLifecycle()
    val waterSourceInfo by lendingViewModel.waterSourceInfo.collectAsStateWithLifecycle()
    val globalThemeState by lendingViewModel.globalThemeState.collectAsStateWithLifecycle()
    val waterSourceMarkers by lendingViewModel.waterSourceMarkers.collectAsStateWithLifecycle()
    val isUserLocationTrackingEnabled by lendingViewModel.isUserLocationTrackingEnabled.collectAsStateWithLifecycle()

    LendingView(
        waterSourceId,
        isPickingLocationForNewWaterSource,
        navigationActions,
        uiState = lendingUiState,
        hasUser = hasUser,
        currentUser = currentUser,
        latestUserLocation = latestUserLocation,
        waterSourceMarkers = waterSourceMarkers,
        waterSourceInfo = waterSourceInfo,
        globalThemeState = globalThemeState,
        isUserLocationTrackingEnabled = isUserLocationTrackingEnabled,
        lendingViewModel::setGlobalThemeMode,
        lendingViewModel::setUserLocationTrackingEnabled,
        lendingViewModel::fetchUserLocation,
        lendingViewModel::fetchLatestUserData,
        lendingViewModel::fetchWaterSourceInfo,
        lendingViewModel::likeOrDislikeWaterSource,
        lendingViewModel::deleteWaterSource,
        lendingViewModel::setWaterSourceFavouriteState
    )
}