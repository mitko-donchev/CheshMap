@file:OptIn(ExperimentalSharedTransitionApi::class)

package com.epicmillennium.cheshmap.presentation.ui.components.maps

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.NearMe
import androidx.compose.material.icons.outlined.NearMe
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.epicmillennium.cheshmap.R
import com.epicmillennium.cheshmap.core.ui.theme.LocalTheme
import com.epicmillennium.cheshmap.domain.marker.WaterSource
import com.epicmillennium.cheshmap.presentation.ui.components.WaterSourceDetailsView
import com.epicmillennium.cheshmap.presentation.ui.components.onDebounceClick
import com.epicmillennium.cheshmap.utils.Constants.mapStyleDark
import com.epicmillennium.cheshmap.utils.Constants.mapStyleLight
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.maps.android.compose.CameraMoveStartedReason
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.maps.android.compose.rememberMarkerState
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

@Composable
fun GoogleMaps(
    initialUserLocation: Location,
    latestUserLocation: Location,
    waterSourceMarkers: List<WaterSource>,
    fetchLatestUserLocation: () -> Unit,
    deleteWaterSource: (WaterSource) -> Job,
    setWaterSourceFavouriteState: (Boolean, WaterSource) -> Job,
) {
    val infoMarkerState = rememberMarkerState()
    val coroutineScope = rememberCoroutineScope()
    val cameraPositionState = rememberCameraPositionState()

    val bearing by remember { derivedStateOf { cameraPositionState.position.bearing } }
    var initialCameraPosition by remember { mutableStateOf(CameraPosition()) }

    var isMapLoaded by remember { mutableStateOf(false) }
    var isUserCentered by remember { mutableStateOf(true) }

    var waterSourceForDetails by remember { mutableStateOf<WaterSource?>(null) }

    // Map UI settings
    val uiSettings by remember {
        mutableStateOf(
            MapUiSettings(
                compassEnabled = false,
                mapToolbarEnabled = false,
                zoomControlsEnabled = false,
                myLocationButtonEnabled = false,
            )
        )
    }

    // Map Properties
    val mapStyle = if (LocalTheme.current.isDark) mapStyleDark() else mapStyleLight()
    var properties by remember {
        mutableStateOf(
            MapProperties(
                isMyLocationEnabled = true,  // always show the dot
                minZoomPreference = 7f,
                maxZoomPreference = 17f,
                mapStyleOptions = MapStyleOptions(
                    mapStyle
                )
            )
        )
    }

    suspend fun pointMapToNorth() {
        // Get the current camera position
        val currentPosition = cameraPositionState.position

        // Create a new CameraPosition with the updated bearing
        val newCameraPosition = com.google.android.gms.maps.model.CameraPosition.Builder()
            .target(currentPosition.target) // Keep the current target (lat/lng)
            .zoom(currentPosition.zoom) // Keep the current zoom level
            .tilt(currentPosition.tilt) // Keep the current tilt
            .bearing(0f) // Set the bearing to 0f a.k.a north
            .build()

        cameraPositionState.animate(
            CameraUpdateFactory.newCameraPosition(newCameraPosition), 500
        )
    }

    // Handle Light/Dark mode
    LaunchedEffect(LocalTheme.current.isDark) {
        properties = properties.copy(
            mapStyleOptions = MapStyleOptions(mapStyle)
        )
    }

    LaunchedEffect(Unit) {
        if (initialUserLocation.isLocationValid()) {
            initialCameraPosition = CameraPosition(
                target = LatLong(
                    initialUserLocation.latitude,
                    initialUserLocation.longitude
                ),
                zoom = 14f  // note: forced zoom level
            )
        }

        initialCameraPosition.let { cameraPosition ->
            cameraPositionState.move(
                CameraUpdateFactory.newLatLngZoom(
                    LatLng(
                        cameraPosition.target.latitude,
                        cameraPosition.target.longitude
                    ),
                    cameraPosition.zoom
                )
            )
        }
    }

    // Animate camera to user location on each location update
    LaunchedEffect(latestUserLocation) {
        if (latestUserLocation.isLocationValid()) {
            initialCameraPosition = CameraPosition(
                target = LatLong(
                    latestUserLocation.latitude,
                    latestUserLocation.longitude
                ),
                zoom = 14f  // note: forced zoom level
            )
        }

        initialCameraPosition.let { cameraPosition ->
            cameraPositionState.animate(
                CameraUpdateFactory.newLatLngZoom(
                    LatLng(
                        cameraPosition.target.latitude,
                        cameraPosition.target.longitude
                    ),
                    cameraPosition.zoom
                ),
                500
            )
        }
        isUserCentered = true
    }

    // Track camera position changes
    LaunchedEffect(cameraPositionState.isMoving) {
        if (cameraPositionState.isMoving && cameraPositionState.cameraMoveStartedReason == CameraMoveStartedReason.GESTURE) {
            isUserCentered = false
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        GoogleMap(
            cameraPositionState = cameraPositionState,
            modifier = Modifier.background(
                MaterialTheme.colorScheme.background,
                RectangleShape
            ),
            uiSettings = uiSettings,
            properties = properties,
            onMapLoaded = {
                isMapLoaded = true
            },
            onMapClick = {
                infoMarkerState.hideInfoWindow()
            },
        ) {
            waterSourceMarkers.forEach { marker ->
                WaterSourceMarker(
                    marker,
                    onMarkerInfoWindowsClicked = {
                        waterSourceForDetails = it
                    }
                )
            }
        }

        if (!isMapLoaded) {
            AnimatedVisibility(
                modifier = Modifier
                    .matchParentSize(),
                visible = !isMapLoaded,
                enter = EnterTransition.None,
                exit = fadeOut()
            ) {
                CircularProgressIndicator(
                    modifier = Modifier
                        .background(MaterialTheme.colorScheme.background)
                        .wrapContentSize()
                )
            }
        }

        AnimatedVisibility(
            visible = bearing != 0f,
            modifier = Modifier.align(Alignment.TopEnd),
            enter = fadeIn(),
            exit = fadeOut(animationSpec = tween(durationMillis = 500, delayMillis = 1500))
        ) {
            // Custom Compass overlay
            Compass(
                bearing = bearing,
                onCompassClick = {
                    coroutineScope.launch {
                        pointMapToNorth()
                    }
                }
            )
        }

        MapButton(
            onClick = { if (!isUserCentered) fetchLatestUserLocation() },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(14.dp)
                .size(50.dp)
        ) {
            Icon(
                imageVector = if (isUserCentered) Icons.Default.NearMe else Icons.Outlined.NearMe,
                contentDescription = stringResource(R.string.center_on_user_location)
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

@Composable
private fun MapButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: @Composable () -> Unit
) {
    FilledIconButton(
        modifier = modifier,
        shape = CircleShape,
        onClick = onDebounceClick { onClick() }
    ) {
        icon.invoke()
    }
}