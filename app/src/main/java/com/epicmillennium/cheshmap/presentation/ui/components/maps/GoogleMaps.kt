@file:OptIn(ExperimentalSharedTransitionApi::class)

package com.epicmillennium.cheshmap.presentation.ui.components.maps

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Directions
import androidx.compose.material.icons.filled.NearMe
import androidx.compose.material.icons.outlined.NearMe
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.epicmillennium.cheshmap.R
import com.epicmillennium.cheshmap.core.ui.theme.LocalTheme
import com.epicmillennium.cheshmap.domain.marker.WaterSource
import com.epicmillennium.cheshmap.domain.marker.WaterSourceStatus
import com.epicmillennium.cheshmap.domain.marker.WaterSourceType
import com.epicmillennium.cheshmap.presentation.ui.components.onDebounceClick
import com.epicmillennium.cheshmap.presentation.ui.navigation.AppNavigationActions
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
import kotlinx.coroutines.launch

@Composable
fun GoogleMaps(
    navigationActions: AppNavigationActions,
    initialUserLocation: Location,
    latestUserLocation: Location,
    waterSourceMarkers: List<WaterSource>,
    fetchLatestUserLocation: () -> Unit
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
                onCloseClick = { waterSourceForDetails = null }
            )
        }
    }
}

@Composable
private fun WaterSourceDetailsView(
    waterSource: WaterSource?,
    onCloseClick: () -> Unit
) {
    waterSource ?: return

    val context = LocalContext.current

    val waterSourceStatus = stringResource(
        when (waterSource.status) {
            WaterSourceStatus.WORKING -> R.string.working
            WaterSourceStatus.UNDER_CONSTRUCTION -> R.string.under_construction
            WaterSourceStatus.OUT_OF_ORDER -> R.string.out_of_order
            WaterSourceStatus.FOR_REVIEW -> R.string.for_review
        }
    )

    val waterSourceType = stringResource(
        when (waterSource.type) {
            WaterSourceType.ESTABLISHMENT -> R.string.establishment
            WaterSourceType.URBAN_WATER -> R.string.urban_water_source
            WaterSourceType.MINERAL_WATER -> R.string.mineral_water
            WaterSourceType.HOT_MINERAL_WATER -> R.string.hot_mineral_water
            WaterSourceType.SPRING_WATER -> R.string.spring_water
        }
    )

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            DetailsTopBar(waterSource, onCloseClick = onCloseClick)
        },
        bottomBar = {
            DetailsBottomBar(waterSource, context)
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Image of the water source
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(if (waterSource.photos.isEmpty()) "" else waterSource.photos[0].imageUrl)
                        .build(),
                    placeholder = painterResource(id = R.drawable.no_image),
                    error = painterResource(id = R.drawable.no_image),
                    contentDescription = stringResource(R.string.water_source_image),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp)
                        .clip(RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Fit
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            HorizontalDivider(thickness = 2.dp)

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp)
            ) {
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                }

                item {
                    Text(
                        text = stringResource(R.string.type),
                        style = MaterialTheme.typography.titleMedium,
                    )
                    HorizontalDivider(
                        thickness = 2.dp,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                    Text(
                        text = waterSourceType,
                        style = MaterialTheme.typography.bodyMedium,
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = stringResource(R.string.status),
                        style = MaterialTheme.typography.titleMedium,
                    )
                    HorizontalDivider(
                        thickness = 2.dp,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                    Text(
                        text = waterSourceStatus,
                        style = MaterialTheme.typography.bodyMedium,
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = stringResource(R.string.details),
                        style = MaterialTheme.typography.titleMedium,
                    )
                    HorizontalDivider(
                        thickness = 2.dp,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                    Text(
                        text = waterSource.details,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }

                item {
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun DetailsTopBar(
    waterSource: WaterSource,
    onCloseClick: () -> Unit
) {
    CenterAlignedTopAppBar(
        title = {
            Text(
                text = waterSource.name,
                modifier = Modifier.wrapContentWidth()
            )
        },
        actions = {
            IconButton(onClick = onCloseClick) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Close details"
                )
            }
        },
        windowInsets = WindowInsets(
            top = 0.dp,
            bottom = 0.dp
        )
    )
}

@Composable
private fun DetailsBottomBar(
    waterSource: WaterSource,
    context: Context
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .padding(8.dp),
    ) {
        Button(
            modifier = Modifier
                .wrapContentWidth()
                .align(Alignment.CenterEnd),
            onClick = {
                // open Google Maps
                val gmmIntentUri =
                    "http://maps.google.com/maps?q=loc:" + waterSource.latitude + "," + waterSource.longitude
                val mapIntent =
                    Intent(Intent.ACTION_VIEW, Uri.parse(gmmIntentUri))
                context.startActivity(mapIntent)
            },
        ) {
            Icon(
                imageVector = Icons.Default.Directions,
                contentDescription = "Directions button icon",
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = stringResource(R.string.take_me_there))
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