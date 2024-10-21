package com.epicmillennium.cheshmap.presentation.ui.lending

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import com.epicmillennium.cheshmap.domain.auth.User
import com.epicmillennium.cheshmap.domain.marker.WaterSource
import com.epicmillennium.cheshmap.presentation.theme.AppThemeMode
import com.epicmillennium.cheshmap.presentation.theme.CheshMapTheme
import com.epicmillennium.cheshmap.presentation.theme.DarkTheme
import com.epicmillennium.cheshmap.presentation.theme.LocalTheme
import com.epicmillennium.cheshmap.presentation.ui.add.AddNewSourceView
import com.epicmillennium.cheshmap.presentation.ui.components.ViewExpandableFloatingButton
import com.epicmillennium.cheshmap.presentation.ui.components.WaterSourceDetailsView
import com.epicmillennium.cheshmap.presentation.ui.components.maps.GoogleMaps
import com.epicmillennium.cheshmap.presentation.ui.components.maps.Location
import com.epicmillennium.cheshmap.presentation.ui.components.maps.isLocationValid
import com.epicmillennium.cheshmap.presentation.ui.favourite.FavouriteView
import com.epicmillennium.cheshmap.presentation.ui.navigation.AppNavigationActions
import com.epicmillennium.cheshmap.presentation.ui.settings.SettingsView
import com.epicmillennium.cheshmap.utils.Constants.LOCATION_PERMISSIONS
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.Job

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun LendingView(
    waterSourceId: String,
    pickingLocationForNewWaterSource: Boolean,
    navigationActions: AppNavigationActions,
    uiState: LendingViewState,
    hasUser: Boolean,
    currentUser: User?,
    latestUserLocation: Location,
    waterSourceMarkers: List<WaterSource>,
    waterSourceInfo: WaterSource?,
    globalThemeState: AppThemeMode,
    isUserLocationTrackingEnabled: Boolean,
    setGlobalThemeMode: (AppThemeMode) -> Job,
    setUserLocationTrackingEnabled: (Boolean) -> Job,
    fetchUserLocation: () -> Job,
    fetchLatestUserData: () -> Job,
    fetchWaterSourceInfo: (String) -> Job,
    likeOrDislikeWaterSource: (Boolean, Boolean) -> Job,
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

                    var shouldLoadWaterSourceInfo by remember { mutableStateOf(false) }

                    var pickedLatLng by remember { mutableStateOf<LatLng?>(null) }
                    var isPickingLocationForNewWaterSource by remember { mutableStateOf(pickingLocationForNewWaterSource) }

                    LaunchedEffect(Unit) {
                        if (waterSourceId.isNotEmpty()) {
                            shouldLoadWaterSourceInfo = true
                            fetchWaterSourceInfo.invoke(waterSourceId)
                        }
                    }

                    CompositionLocalProvider(value = LocalTheme provides DarkTheme(LocalTheme.current.isDark)) {
                        CheshMapTheme(darkTheme = LocalTheme.current.isDark) {
                            Scaffold(modifier = Modifier.fillMaxSize()) {
                                Box(modifier = Modifier.fillMaxSize()) {
                                    GoogleMaps(
                                        uiState.contentState.userState.lastKnownLocation,
                                        latestUserLocation,
                                        waterSourceMarkers,
                                        isUserLocationTrackingEnabled,
                                        isPickingLocationForNewWaterSource,
                                        showWaterSourceDetails = {
                                            shouldLoadWaterSourceInfo = true
                                            fetchWaterSourceInfo.invoke(it.id)
                                        },
                                        fetchLatestUserLocation = { fetchUserLocation() },
                                        confirmPickedLocation = {
                                            isPickingLocationForNewWaterSource = false
                                            pickedLatLng = it
                                            currentScreen = Screen.ADD
                                        },
                                        cancelPickingLocationForNewWaterSource = {
                                            isPickingLocationForNewWaterSource = false
                                        }
                                    )

                                    AnimatedVisibility(
                                        visible = !isPickingLocationForNewWaterSource,
                                        enter = fadeIn(animationSpec = tween(durationMillis = 500)),
                                        exit = fadeOut(animationSpec = tween(durationMillis = 500))
                                    ) {
                                        ViewExpandableFloatingButton(
                                            openScreenFromFab = {
                                                if (it != Screen.ADD) {
                                                    currentScreen = it
                                                } else {
                                                    if (hasUser) {
                                                        isPickingLocationForNewWaterSource = true
                                                    } else {
                                                        navigationActions.navigateToLogin()
                                                    }
                                                }
                                            }
                                        )
                                    }

                                    AnimatedVisibility(
                                        visible = currentScreen == Screen.ADD,
                                        enter = scaleIn(animationSpec = tween(durationMillis = 500)),
                                        exit = scaleOut()
                                    ) {
                                        AddNewSourceView(
                                            pickedLatLng,
                                            onNavigateBack = { currentScreen = Screen.LENDING },
                                        )
                                    }

                                    AnimatedVisibility(
                                        visible = currentScreen == Screen.FAVOURITE,
                                        enter = scaleIn(animationSpec = tween(durationMillis = 500)),
                                        exit = scaleOut()
                                    ) {
                                        FavouriteView(
                                            navigationActions,
                                            hasUser,
                                            currentUser,
                                            favouriteWaterSources = waterSourceMarkers.filter { it.isFavourite },
                                            onNavigateBack = { currentScreen = Screen.LENDING },
                                            setWaterSourceFavouriteState = setWaterSourceFavouriteState
                                        )
                                    }

                                    AnimatedVisibility(
                                        visible = currentScreen == Screen.SETTINGS,
                                        enter = scaleIn(animationSpec = tween(durationMillis = 500)),
                                        exit = scaleOut()
                                    ) {
                                        SettingsView(
                                            globalThemeState,
                                            isUserLocationTrackingEnabled,
                                            setGlobalThemeMode = { setGlobalThemeMode(it) },
                                            setUserLocationTrackingEnabled = {
                                                setUserLocationTrackingEnabled(
                                                    it
                                                )
                                            },
                                            onNavigateBack = { currentScreen = Screen.LENDING },
                                        )
                                    }

                                    AnimatedVisibility(
                                        visible = shouldLoadWaterSourceInfo,
                                        enter = scaleIn(animationSpec = tween(durationMillis = 500)),
                                        exit = scaleOut()
                                    ) {
                                        WaterSourceDetailsView(
                                            navigationActions,
                                            hasUser,
                                            currentUser,
                                            waterSourceInfo,
                                            likeOrDislikeWaterSource = { shouldLike, shouldReset ->
                                                likeOrDislikeWaterSource.invoke(shouldLike, shouldReset)
                                            },
                                            onCloseClick = {
                                                shouldLoadWaterSourceInfo = false
                                            },
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