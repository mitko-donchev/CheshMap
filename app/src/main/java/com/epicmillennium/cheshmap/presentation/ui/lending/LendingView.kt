package com.epicmillennium.cheshmap.presentation.ui.lending


import android.Manifest
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.epicmillennium.cheshmap.presentation.ui.components.maps.GoogleMaps
import com.epicmillennium.cheshmap.utils.Constants.LOCATION_PERMISSIONS
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import kotlinx.coroutines.launch

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun LendingView() {
    val coroutineScope = rememberCoroutineScope()

    var allPermissionsProvided: Boolean by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        val multiplePermission = rememberMultiplePermissionsState(LOCATION_PERMISSIONS) {
            coroutineScope.launch {
                if (it[Manifest.permission.ACCESS_FINE_LOCATION] == true && it[Manifest.permission.ACCESS_COARSE_LOCATION] == true) {
                    allPermissionsProvided = true
                } else {
                    // Handle permission denied and show button
                }
            }
        }

        LaunchedEffect(Unit) {
            if (multiplePermission.allPermissionsGranted) {
                allPermissionsProvided = true
            } else {
                multiplePermission.launchMultiplePermissionRequest()
            }
        }

        AnimatedVisibility(visible = allPermissionsProvided) {
            GoogleMaps()
        }
    }
}
