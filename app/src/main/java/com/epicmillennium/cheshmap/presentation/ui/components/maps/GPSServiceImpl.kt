package com.epicmillennium.cheshmap.presentation.ui.components.maps

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Looper
import android.util.Log
import androidx.core.app.ActivityCompat
import com.epicmillennium.cheshmap.utils.checkLocationPermissions
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicReference
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

class GPSServiceImpl(private val appContext: Context) : GPSService {

    // Define an atomic reference to store the latest location
    private val latestLocation = AtomicReference<Location?>(null)

    // Initialize the FusedLocationProviderClient source of location data
    private val fusedLocationClient by lazy {
        LocationServices.getFusedLocationProviderClient(appContext)
    }

    private var errorCallback: ((String) -> Unit)? = null
    private var locationUpdateCallback: ((Location?) -> Unit)? = null
    private var internalLocationCallback: LocationCallback? = null

    private val locationRequest = LocationRequest.Builder(UPDATE_INTERVAL)
        .setIntervalMillis(UPDATE_INTERVAL)
        .setPriority(Priority.PRIORITY_LOW_POWER)
        .setMinUpdateDistanceMeters(1.0f)
        .setWaitForAccurateLocation(false)
        .build()

    // Gets location 1 time only.
    // WARNING: Should NOT be used for continuous location updates or in conjunction with currentLocation()
    @SuppressLint("MissingPermission") // Assuming location permission check is already handled
    override suspend fun getCurrentGPSLocationOneTime(): Location =
        suspendCoroutine { continuation ->
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                location?.let { androidOsLocation ->
                    val updatedLocation =
                        Location(androidOsLocation.latitude, androidOsLocation.longitude)
                    latestLocation.set(updatedLocation)
                    continuation.resume(updatedLocation)
                } ?: run {
                    continuation.resumeWithException(Exception("Unable to get current location - 3"))
                }
            }.addOnFailureListener { e ->
                continuation.resumeWithException(e)
            }
        }

    @SuppressLint("MissingPermission") // suppress missing permission check warning, we are checking permissions in the method.
    // suspend fun onUpdatedGPSLocation(callback: (Location?) -> Flow<Location>) {  // LEAVE FOR REFERENCE - emits a flow of locations
    override suspend fun onUpdatedGPSLocation(
        errorCallback: (errorMessage: String) -> Unit,
        locationCallback: (newLocation: Location?) -> Unit
    ) {
        startGPSLocationUpdates(
            errorCallback,
            locationCallback
        ) // keeps requesting location updates
    }

    @SuppressLint("MissingPermission") // suppress missing permission check warning, we are checking permissions in the method.
    private fun startGPSLocationUpdates(
        errorCallback: ((String) -> Unit)? = null,
        locationCallback: ((Location?) -> Unit)? = null
    ) {
        if (!appContext.checkLocationPermissions()) return
        if (locationCallback == this.locationUpdateCallback) return // already using same callback
        this.locationUpdateCallback = locationCallback
        this.errorCallback = errorCallback

        // Check if GPS and Network location is enabled
        val locationManager =
            appContext.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val isGpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
        val isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)

        if (!isGpsEnabled) {
            errorCallback?.let { errorCallback("GPS is disabled") }
            Log.d(this.toString(), "GPS is disabled")
            return
        }
        if (!isNetworkEnabled) {
            errorCallback?.let { errorCallback("Network is disabled") }
            Log.d(this.toString(), "Network is disabled")
            return
        }

        // Setup the location callback
        internalLocationCallback?.let { fusedLocationClient.removeLocationUpdates(it) }
        internalLocationCallback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                super.onLocationResult(result)

                result.locations.lastOrNull()?.let { androidOsLocation: android.location.Location ->
                    //launch {  // For flow - leave for reference
                    //    send(androidOsLocation) // emits the androidOsLocation into the flow
                    //}
                    val updatedLocation =
                        Location(androidOsLocation.latitude, androidOsLocation.longitude)
                    latestLocation.set(updatedLocation)

                    locationCallback?.let {
                        locationCallback(updatedLocation)
                    }
                }
            }
        }

        fusedLocationClient.requestLocationUpdates(
            locationRequest,
            internalLocationCallback!!,
            Looper.getMainLooper()
        )
    }

    override fun getLatestGPSLocation(): Location? = latestLocation.get()

    override fun allowBackgroundLocationUpdates() {
        CoroutineScope(Dispatchers.Main).launch {
//            _androidIntentFlow.emit(Intent(ACTION_START_BACKGROUND_UPDATES))
        }
    }

    override fun preventBackgroundLocationUpdates() {
        CoroutineScope(Dispatchers.Main).launch {
//            _androidIntentFlow.emit(Intent(ACTION_STOP_BACKGROUND_UPDATES))
        }
    }

    companion object {
        const val ACTION_START_BACKGROUND_UPDATES = "ACTION_START_BACKGROUND_UPDATES"
        const val ACTION_STOP_BACKGROUND_UPDATES = "ACTION_STOP_BACKGROUND_UPDATES"

        const val UPDATE_INTERVAL = 1000L
    }
}