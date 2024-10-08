package com.epicmillennium.cheshmap.presentation.ui.lending

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.epicmillennium.cheshmap.presentation.ui.navigation.AppNavigationActions

@Composable
fun LendingScreen(navigationActions: AppNavigationActions) {

    val lendingViewModel: LendingViewModel = hiltViewModel()

    val lendingUiState by lendingViewModel.lendingUiState.collectAsStateWithLifecycle()
    val latestUserLocation by lendingViewModel.userLocation.collectAsStateWithLifecycle()
    val waterSourceMarkers by lendingViewModel.waterSourceMarkers.collectAsStateWithLifecycle()

    LendingView(
        navigationActions = navigationActions,
        uiState = lendingUiState,
        latestUserLocation = latestUserLocation,
        waterSourceMarkers = waterSourceMarkers,
        lendingViewModel::fetchUserLocation,
        lendingViewModel::fetchLatestUserData,
        lendingViewModel::deleteWaterSource,
        lendingViewModel::setWaterSourceFavouriteState
    )
}