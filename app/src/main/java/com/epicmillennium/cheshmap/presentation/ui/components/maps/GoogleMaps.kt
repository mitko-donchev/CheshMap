package com.epicmillennium.cheshmap.presentation.ui.components.maps

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.epicmillennium.cheshmap.R
import com.epicmillennium.cheshmap.presentation.theme.LocalTheme
import com.epicmillennium.cheshmap.domain.marker.WaterSource
import com.epicmillennium.cheshmap.domain.marker.WaterSourceType
import com.epicmillennium.cheshmap.presentation.ui.components.onDebounceClick
import com.epicmillennium.cheshmap.utils.Constants.mapStyleDark
import com.epicmillennium.cheshmap.utils.Constants.mapStyleLight
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.maps.android.clustering.algo.NonHierarchicalViewBasedAlgorithm
import com.google.maps.android.compose.CameraMoveStartedReason
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.MapsComposeExperimentalApi
import com.google.maps.android.compose.clustering.Clustering
import com.google.maps.android.compose.clustering.rememberClusterManager
import com.google.maps.android.compose.clustering.rememberClusterRenderer
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.maps.android.compose.rememberMarkerState
import kotlinx.coroutines.launch

@Composable
fun GoogleMaps(
    initialUserLocation: Location,
    latestUserLocation: Location,
    waterSourceMarkers: List<WaterSource>,
    isUserLocationTrackingEnabled: Boolean,
    isPickingLocationForNewWaterSource: Boolean,
    showWaterSourceDetails: (WaterSource) -> Unit,
    fetchLatestUserLocation: () -> Unit,
    confirmPickedLocation: (LatLng) -> Unit,
    cancelPickingLocationForNewWaterSource: () -> Unit,
) {
    val infoMarkerState = rememberMarkerState()
    val coroutineScope = rememberCoroutineScope()
    val cameraPositionState = rememberCameraPositionState()

    val bearing by remember { derivedStateOf { cameraPositionState.position.bearing } }
    var initialCameraPosition by remember { mutableStateOf(CameraPosition()) }

    var isMapLoaded by remember { mutableStateOf(false) }
    var isUserCentered by remember { mutableStateOf(true) }

    // Map UI settings
    val uiSettings by remember {
        mutableStateOf(
            MapUiSettings(
                compassEnabled = false, // it is handled by a custom button
                mapToolbarEnabled = false,
                zoomControlsEnabled = false,
                myLocationButtonEnabled = false, // same as the compass
                rotationGesturesEnabled = !isPickingLocationForNewWaterSource
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
            CustomRendererClustering(
                waterSourceMarkers,
                onMarkerClicked = {
                    showWaterSourceDetails.invoke(it)
                }
            )
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

        Column(modifier = Modifier.align(Alignment.TopEnd)) {
            AnimatedVisibility(
                visible = !isUserCentered && isUserLocationTrackingEnabled,
                enter = fadeIn(),
                exit = fadeOut(animationSpec = tween(durationMillis = 500))
            ) {
                CenterOnUserLocationButton {
                    fetchLatestUserLocation()
                }
            }

            AnimatedVisibility(
                visible = bearing != 0f,
                enter = fadeIn(),
                exit = fadeOut(animationSpec = tween(durationMillis = 500, delayMillis = 1500))
            ) {
                CompassButton(
                    bearing = bearing,
                    onCompassClick = {
                        coroutineScope.launch {
                            pointMapToNorth()
                        }
                    }
                )
            }
        }

        AnimatedVisibility(
            visible = isPickingLocationForNewWaterSource,
            enter = scaleIn(animationSpec = tween(durationMillis = 500)),
            exit = scaleOut(animationSpec = tween(durationMillis = 500)),
            modifier = Modifier.align(Alignment.Center)
        ) {
            Icon(
                imageVector = Icons.Default.WaterDrop,
                contentDescription = stringResource(R.string.pick_location_for_new_water_source),
                modifier = Modifier
                    .size(48.dp)
                    .rotate(180f)
                    .offset(y = (20).dp)
            )
        }

        AnimatedVisibility(
            visible = isPickingLocationForNewWaterSource,
            enter = fadeIn(animationSpec = tween(durationMillis = 500)),
            exit = fadeOut(animationSpec = tween(durationMillis = 500)),
            modifier = Modifier.align(Alignment.BottomEnd)
        ) {
            Column(
                modifier = Modifier
                    .padding(14.dp)
                    .wrapContentHeight(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                FilledIconButton(
                    shape = MaterialTheme.shapes.medium,
                    colors = IconButtonDefaults.iconButtonColors().copy(containerColor = Color.Red),
                    onClick = onDebounceClick { cancelPickingLocationForNewWaterSource.invoke() }
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = stringResource(R.string.cancel_picking_location)
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                FilledIconButton(
                    modifier = Modifier.size(56.dp),
                    shape = MaterialTheme.shapes.large,
                    onClick = onDebounceClick { confirmPickedLocation.invoke(cameraPositionState.position.target) }
                ) {
                    Icon(
                        imageVector = Icons.Default.Done,
                        contentDescription = stringResource(R.string.confirm_picked_location)
                    )
                }
            }
        }
    }
}

@OptIn(MapsComposeExperimentalApi::class)
@Composable
fun CustomRendererClustering(
    items: List<WaterSource>,
    onMarkerClicked: (WaterSource) -> Unit
) {
    val configuration = LocalConfiguration.current
    val screenHeight = configuration.screenHeightDp.dp
    val screenWidth = configuration.screenWidthDp.dp
    val clusterManager = rememberClusterManager<WaterSource>()

    clusterManager?.setAlgorithm(
        NonHierarchicalViewBasedAlgorithm(
            screenWidth.value.toInt(),
            screenHeight.value.toInt()
        )
    )

    val renderer = rememberClusterRenderer(
        clusterContent = { cluster ->
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .background(color = Color.Blue, shape = CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(text = cluster.size.toString(), color = Color.White)
            }
        },
        clusterItemContent = { clusterItem ->
            Icon(
                imageVector = Icons.Filled.LocationOn,
                contentDescription = null,
                tint = when (clusterItem.type) {
                    WaterSourceType.ESTABLISHMENT -> Color.Yellow
                    WaterSourceType.URBAN_WATER -> MaterialTheme.colorScheme.primary
                    WaterSourceType.MINERAL_WATER -> Color.Green
                    WaterSourceType.HOT_MINERAL_WATER -> Color.Red
                    WaterSourceType.SPRING_WATER -> Color.Gray
                    WaterSourceType.NONE -> Color.Gray
                },
                modifier = Modifier.size(32.dp)
            )
        },
        clusterManager = clusterManager,
    )

    SideEffect {
        clusterManager ?: return@SideEffect
        clusterManager.setOnClusterItemInfoWindowClickListener { clusterItem ->
            onMarkerClicked.invoke(clusterItem)
        }
    }

    SideEffect {
        if (clusterManager?.renderer != renderer) {
            clusterManager?.renderer = renderer ?: return@SideEffect
        }
    }

    if (clusterManager != null) {
        Clustering(
            items = items,
            clusterManager = clusterManager,
        )
    }
}