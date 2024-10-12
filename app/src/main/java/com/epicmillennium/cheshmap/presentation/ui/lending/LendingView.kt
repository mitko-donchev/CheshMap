package com.epicmillennium.cheshmap.presentation.ui.lending

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.epicmillennium.cheshmap.core.ui.theme.CheshMapTheme
import com.epicmillennium.cheshmap.core.ui.theme.DarkTheme
import com.epicmillennium.cheshmap.core.ui.theme.LocalTheme
import com.epicmillennium.cheshmap.domain.marker.WaterSource
import com.epicmillennium.cheshmap.presentation.ui.components.ViewExpandableFloatingButton
import com.epicmillennium.cheshmap.presentation.ui.components.WaterSourceDetailsView
import com.epicmillennium.cheshmap.presentation.ui.components.maps.GoogleMaps
import com.epicmillennium.cheshmap.presentation.ui.components.maps.Location
import com.epicmillennium.cheshmap.presentation.ui.components.maps.isLocationValid
import com.epicmillennium.cheshmap.presentation.ui.favourite.FavouriteView
import com.epicmillennium.cheshmap.utils.Constants.LOCATION_PERMISSIONS
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import kotlinx.coroutines.Job

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun LendingView(
    uiState: LendingViewState,
    latestUserLocation: Location,
    waterSourceMarkers: List<WaterSource>,
    fetchUserLocation: () -> Job,
    fetchLatestUserData: () -> Job,
    deleteWaterSource: (WaterSource) -> Job,
    setWaterSourceFavouriteState: (Boolean, WaterSource) -> Job
) {
    val snackbarHostState = remember { SnackbarHostState() }

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        val multiplePermission = rememberMultiplePermissionsState(LOCATION_PERMISSIONS)

        LaunchedEffect(multiplePermission.allPermissionsGranted) {
            if (multiplePermission.allPermissionsGranted) {
                if (!latestUserLocation.isLocationValid()) fetchLatestUserData()
            } else {
                multiplePermission.launchMultiplePermissionRequest()
            }
        }

        AnimatedVisibility(visible = multiplePermission.allPermissionsGranted) {
            when (uiState.contentState) {
                is LendingViewContentState.Loading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Box(modifier = Modifier.size(48.dp)) {
                            CircularProgressIndicator()
                        }
                    }
                }

                is LendingViewContentState.Success -> {

                    var currentScreen by remember { mutableStateOf(Screen.LENDING) }

                    var waterSourceForDetails by remember { mutableStateOf<WaterSource?>(null) }

                    CompositionLocalProvider(value = LocalTheme provides DarkTheme(LocalTheme.current.isDark)) {
                        CheshMapTheme(darkTheme = LocalTheme.current.isDark) {
                            Scaffold(modifier = Modifier.fillMaxSize()) {
                                Box(modifier = Modifier.fillMaxSize()) {
                                    GoogleMaps(
                                        uiState.contentState.userState.lastKnownLocation,
                                        latestUserLocation,
                                        waterSourceMarkers,
                                        showWaterSourceDetails = { waterSourceForDetails = it },
                                        fetchLatestUserLocation = { fetchUserLocation() },
                                    )

                                    ViewExpandableFloatingButton(
                                        openScreenFromFab = {
                                            currentScreen = it
                                        }
                                    )

                                    AnimatedVisibility(
                                        visible = currentScreen == Screen.FAVOURITE,
                                        enter = scaleIn(animationSpec = tween(durationMillis = 500)),
                                        exit = scaleOut()
                                    ) {
                                        FavouriteView(
                                            favouriteWaterSources = waterSourceMarkers.filter { it.isFavourite },
                                            onNavigateBack = { currentScreen = Screen.LENDING },
                                            setWaterSourceFavouriteState = setWaterSourceFavouriteState
                                        )
                                    }

                                    AnimatedVisibility(
                                        visible = waterSourceForDetails != null,
                                        enter = scaleIn(animationSpec = tween(durationMillis = 500)),
                                        exit = scaleOut()
                                    ) {
                                        WaterSourceDetailsView(
                                            waterSourceForDetails,
                                            onCloseClick = { waterSourceForDetails = null },
                                            onFavouriteIconClick = { isFavourite, waterSource ->
                                                setWaterSourceFavouriteState.invoke(
                                                    isFavourite,
                                                    waterSource
                                                )
                                            },
                                            deleteWaterSource = { waterSource ->
                                                deleteWaterSource.invoke(waterSource)
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                is LendingViewContentState.Error -> {
                    LaunchedEffect(uiState.contentState.message) {
                        snackbarHostState.showSnackbar(
                            message = uiState.contentState.message,
                            duration = SnackbarDuration.Short
                        )
                    }
                }
            }
        }
    }
}

enum class Screen {
    LENDING, ADD, FAVOURITE, SETTINGS
}