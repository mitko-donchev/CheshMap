package com.epicmillennium.cheshmap.presentation.ui.lending


import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.epicmillennium.cheshmap.presentation.ui.components.maps.GoogleMaps
import com.epicmillennium.cheshmap.presentation.ui.components.maps.Location
import com.epicmillennium.cheshmap.presentation.ui.components.maps.isLocationValid
import com.epicmillennium.cheshmap.utils.Constants.LOCATION_PERMISSIONS
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import kotlinx.coroutines.Job

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun LendingView(
    uiState: LendingViewState,
    latestUserLocation: Location,
    fetchLatestUserData: () -> Job,
    fetchUserLocation: () -> Job
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
                    GoogleMaps(
                        uiState.contentState.userState.lastKnownLocation,
                        latestUserLocation,
                        fetchLatestUserLocation = { fetchUserLocation() }
                    )
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