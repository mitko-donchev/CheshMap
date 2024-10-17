package com.epicmillennium.cheshmap.presentation.ui.lending

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun LendingScreen() {

    val lendingViewModel: LendingViewModel = hiltViewModel()

    val lendingUiState by lendingViewModel.lendingUiState.collectAsStateWithLifecycle()
    val latestUserLocation by lendingViewModel.userLocation.collectAsStateWithLifecycle()
    val waterSourceMarkers by lendingViewModel.waterSourceMarkers.collectAsStateWithLifecycle()
    val globalThemeState by lendingViewModel.globalThemeState.collectAsStateWithLifecycle()
    val isUserLocationTrackingEnabled by lendingViewModel.isUserLocationTrackingEnabled.collectAsStateWithLifecycle()
    val waterSourceInfo by lendingViewModel.waterSourceInfo.collectAsStateWithLifecycle()

    LendingView(
        uiState = lendingUiState,
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