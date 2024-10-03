package com.epicmillennium.cheshmap.presentation.ui.lending

import android.util.Log
import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.epicmillennium.cheshmap.domain.marker.FirestoreWaterSource
import com.epicmillennium.cheshmap.domain.marker.WaterSource
import com.epicmillennium.cheshmap.presentation.ui.components.maps.GPSService
import com.epicmillennium.cheshmap.presentation.ui.components.maps.Location
import com.epicmillennium.cheshmap.utils.Constants.FIRESTORE_COLLECTION_WATER_SOURCES
import com.epicmillennium.cheshmap.utils.preferences.UserPreferencesRepository
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.dataObjects
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LendingViewModel @Inject constructor(
    private val gpsService: GPSService,
    private val firebaseFirestore: FirebaseFirestore,
    private val userPreferencesRepository: UserPreferencesRepository
) : ViewModel() {

    private val _lendingUiState =
        MutableStateFlow(LendingViewState(LendingViewContentState.Loading))
    val lendingUiState = _lendingUiState
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(3000L),
            LendingViewState(LendingViewContentState.Loading)
        )

    private val _userLocation = MutableStateFlow(Location(0.0, 0.0))
    val userLocation = _userLocation.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(3000L),
        Location(0.0, 0.0)
    )

    private val _waterSourceMarkers = MutableStateFlow(emptyList<WaterSource>())
    val waterSourceMarkers = _waterSourceMarkers
        .onStart { loadWaterSourceMarkers() }
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(3000L),
            emptyList()
        )

    fun fetchUserLocation() = viewModelScope.launch(Dispatchers.IO) {
        Log.v("Heavy methods logs", "Refreshing latest user location")

        _userLocation.tryEmit(Location(0.0, 0.0))
        try {
            gpsService.getCurrentGPSLocationOneTime().also { location ->
                _userLocation.tryEmit(location)
            }
        } catch (e: Exception) {
            Log.e("Heavy methods logs", "Failed to get GPS location: ${e.message}")
            // Handle location retrieve error.
        }
    }

    fun fetchLatestUserData() = viewModelScope.launch(Dispatchers.IO) {
        Log.v("Heavy methods logs", "Refreshing latest user data")

        _lendingUiState.update { it.copy(contentState = LendingViewContentState.Loading) }

        gpsService.getCurrentGPSLocationOneTime().also {
            _lendingUiState.update { lendingViewState ->
                lendingViewState.copy(
                    contentState = LendingViewContentState.Success(
                        userState = UserState(it)
                    )
                )
            }
        }
    }

    private fun loadWaterSourceMarkers() = viewModelScope.launch(Dispatchers.IO) {
        Log.v("Heavy methods logs", "Fetching latest water sources")

        firebaseFirestore.collection(FIRESTORE_COLLECTION_WATER_SOURCES)
            .dataObjects<FirestoreWaterSource>()
            .collectLatest {
                Log.v("Heavy methods logs", "Water sources fetched")

                val waterSources = it.map { firestoreWaterSource ->
                    WaterSource.fromFirestoreWaterSource(firestoreWaterSource)
                }

                _waterSourceMarkers.tryEmit(waterSources)
            }
    }
}

@Immutable
data class LendingViewState(
    val contentState: LendingViewContentState
)

@Immutable
data class UserState(val lastKnownLocation: Location)

@Immutable
sealed class LendingViewContentState {
    data object Loading : LendingViewContentState()
    data class Success(val userState: UserState) : LendingViewContentState()
    data class Error(val message: String) : LendingViewContentState()
}