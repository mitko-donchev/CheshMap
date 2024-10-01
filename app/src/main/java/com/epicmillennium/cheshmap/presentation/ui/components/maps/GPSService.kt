package com.epicmillennium.cheshmap.presentation.ui.components.maps

interface GPSService {
    suspend fun onUpdatedGPSLocation(
        errorCallback: (String) -> Unit = {},
        locationCallback: (Location?) -> Unit
    )

    fun getLatestGPSLocation(): Location?

    suspend fun getCurrentGPSLocationOneTime(): Location

    fun allowBackgroundLocationUpdates()

    fun preventBackgroundLocationUpdates()
}