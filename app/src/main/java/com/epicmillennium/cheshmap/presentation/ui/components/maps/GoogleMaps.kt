package com.epicmillennium.cheshmap.presentation.ui.components.maps


import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.fadeOut
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.epicmillennium.cheshmap.R
import com.epicmillennium.cheshmap.core.ui.theme.LocalTheme
import com.epicmillennium.cheshmap.utils.Constants.mapStyleDark
import com.epicmillennium.cheshmap.utils.Constants.mapStyleLight
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMapOptions
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.android.gms.maps.model.Marker
import com.google.maps.android.compose.CameraMoveStartedReason
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.maps.android.compose.rememberMarkerState

@Composable
fun GoogleMaps() {

    val infoMarkerState = rememberMarkerState()
    val cameraPositionState = rememberCameraPositionState()

    var userLocation = remember { Location(0.0, 0.0) }
    var shouldSetInitialCameraPosition = remember { CameraPosition() }

    // Information marker - visible after user clicks "find marker" button in details panel
    var infoMarker by remember { mutableStateOf<Marker?>(null) }
    var infoMarkerInfoWindowOpenPhase by remember { mutableIntStateOf(0) }

    var isMapLoaded by remember { mutableStateOf(false) }
    var isUserCentered by remember { mutableStateOf(true) }

    // Map UI settings
    val uiSettings by remember {
        mutableStateOf(
            MapUiSettings(
                myLocationButtonEnabled = false,
                zoomControlsEnabled = false,
            )
        )
    }

    // Map Properties
    val mapStyle = if (LocalTheme.current.isDark) mapStyleDark() else mapStyleLight()
    var properties by remember {
        mutableStateOf(
            MapProperties(
                isMyLocationEnabled = true,  // always show the dot
                minZoomPreference = 1f,
                maxZoomPreference = 25f,
                mapStyleOptions = MapStyleOptions(
                    mapStyle
                )
            )
        )
    }

    // Handle Light/Dark mode
    LaunchedEffect(LocalTheme.current.isDark) {
        properties = MapProperties(
            isMyLocationEnabled = true,  // always show the dot
            minZoomPreference = 7f,
            maxZoomPreference = 17f,
            mapStyleOptions = MapStyleOptions(
                mapStyle
            )
        )
    }

    // 1) Update user GPS location - will be moved from here
    LaunchedEffect(Unit) {
        userLocation = Location(latitude = 43.1421722, longitude = 24.7320678)
//        while (true) {
//            val commonGpsLocationService = GPSService()
//            commonGpsLocationService.onUpdatedGPSLocation(
//                errorCallback = { errorMessage ->
//                    Log.e(this.toString(), "Error: $errorMessage")
//                },
//                locationCallback = { updatedLocation ->
//                    updatedLocation?.let { location ->
//                        userLocation = location
//                    } ?: run {
//                        Log.e(this.toString(), "Unable to get current location - 1")
//                    }
//                })
//            delay(2.seconds)
//        }
    }

    // Usually used to setup the initial camera position (doesn't support tracking due to forcing zoom level)
    LaunchedEffect(Unit) {
        if (userLocation.isLocationValid()) {
            shouldSetInitialCameraPosition = CameraPosition(
                target = LatLong(
                    userLocation.latitude,
                    userLocation.longitude
                ),
                zoom = 4f  // note: forced zoom level
            )
        }

        shouldSetInitialCameraPosition.let { cameraPosition ->
            userLocation = Location(cameraPosition.target.latitude, cameraPosition.target.longitude)

            cameraPositionState.move(
                CameraUpdateFactory.newLatLngZoom(
                    LatLng(
                        cameraPosition.target.latitude,
                        cameraPosition.target.longitude
                    ),
                    cameraPosition.zoom
                    // cameraPositionState.position.zoom // allows users to zoom in and out while maintaining the same center, why does this work?
                )
            )
        }
    }

    // Set Camera to User Location (ie: Tracking) (Allows user to control zoom level)
    LaunchedEffect(userLocation) {
        if (userLocation.isLocationValid()) {
            shouldSetInitialCameraPosition = CameraPosition(
                target = LatLong(
                    userLocation.latitude,
                    userLocation.longitude
                ),
                zoom = 14f  // note: forced zoom level
            )
        }

        shouldSetInitialCameraPosition.let { cameraPosition ->
            cameraPositionState.move(
                CameraUpdateFactory.newLatLngZoom(
                    LatLng(
                        cameraPosition.target.latitude,
                        cameraPosition.target.longitude
                    ),
                    cameraPosition.zoom
                    // cameraPositionState.position.zoom // allows users to zoom in and out while maintaining the same center, why does this work?
                )
            )
        }
    }

    // Tack camera position changes
    LaunchedEffect(cameraPositionState.isMoving) {
        if (cameraPositionState.isMoving && cameraPositionState.cameraMoveStartedReason == CameraMoveStartedReason.GESTURE) {
            isUserCentered = false
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        GoogleMap(
            cameraPositionState = cameraPositionState,
            modifier = Modifier.background(MaterialTheme.colorScheme.background, RectangleShape),
            uiSettings = uiSettings,
            properties = properties,
            onMapLoaded = {
                isMapLoaded = true
            },
            onMapClick = {
                infoMarkerState.hideInfoWindow()
                infoMarker = null
            },
            googleMapOptionsFactory = {
                GoogleMapOptions().apply {
                    this.backgroundColor(0x000000)
                }
            }
        )

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

        MapButton(
            stringResource(R.string.center_on_user_location),
            onClick = {
                shouldSetInitialCameraPosition.let { cameraPosition ->
                    cameraPositionState.move(
                        CameraUpdateFactory.newLatLngZoom(
                            LatLng(
                                cameraPosition.target.latitude,
                                cameraPosition.target.longitude
                            ),
                            cameraPosition.zoom
                            // cameraPositionState.position.zoom // allows users to zoom in and out while maintaining the same center, why does this work?
                        )
                    )
                }
                isUserCentered = true
            },
            isUserCentered,
            modifier = Modifier.align(Alignment.BottomEnd)
        )
    }
}

@Composable
private fun MapButton(
    text: String,
    onClick: () -> Unit,
    isUserCentered: Boolean,
    modifier: Modifier = Modifier
) {
    FilledIconButton(
        modifier = modifier
            .padding(16.dp)
            .size(50.dp),
        shape = CircleShape,
        onClick = onClick
    ) {
        Icon(
            imageVector = if (isUserCentered) Icons.Default.NearMe else Icons.Outlined.NearMe,
            contentDescription = text
        )
    }
}