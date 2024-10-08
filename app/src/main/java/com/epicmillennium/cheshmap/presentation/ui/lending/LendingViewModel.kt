package com.epicmillennium.cheshmap.presentation.ui.lending

import android.util.Log
import androidx.compose.runtime.Immutable
import androidx.datastore.preferences.core.edit
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.epicmillennium.cheshmap.domain.marker.WaterSource
import com.epicmillennium.cheshmap.domain.usecase.AddWaterSourceUseCase
import com.epicmillennium.cheshmap.domain.usecase.DeleteWaterSourceByIdUseCase
import com.epicmillennium.cheshmap.domain.usecase.GetAllWaterSourcesUseCase
import com.epicmillennium.cheshmap.presentation.ui.components.maps.GPSService
import com.epicmillennium.cheshmap.presentation.ui.components.maps.Location
import com.epicmillennium.cheshmap.utils.Constants.FAVOURITE_SOURCES
import com.epicmillennium.cheshmap.utils.Constants.FIRESTORE_COLLECTION_WATER_SOURCES
import com.epicmillennium.cheshmap.utils.preferences.UserPreferencesRepository
import com.google.firebase.firestore.FirebaseFirestore
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
    private val firestore: FirebaseFirestore,
    private val addWaterSourceUseCase: AddWaterSourceUseCase,
    private val getAllWaterSourcesUseCase: GetAllWaterSourcesUseCase,
    private val userPreferencesRepository: UserPreferencesRepository,
    private val deleteWaterSourceByIdUseCase: DeleteWaterSourceByIdUseCase
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

    fun setWaterSourceFavouriteState(
        isFavourite: Boolean,
        waterSource: WaterSource
    ) = viewModelScope.launch {
        userPreferencesRepository.dataStore.edit {
            val listOfFavourites = (it[FAVOURITE_SOURCES] ?: emptySet()).toMutableList()

            if (isFavourite) {
                listOfFavourites.add(waterSource.id)
            } else {
                listOfFavourites.remove(waterSource.id)
            }

            it[FAVOURITE_SOURCES] = listOfFavourites.toSet()
        }

        addWaterSourceUseCase.invoke(waterSource.copy(isFavourite = isFavourite))
    }

    fun deleteWaterSource(
        waterSource: WaterSource
    ) = viewModelScope.launch(Dispatchers.IO) {
        userPreferencesRepository.dataStore.edit {
            val listOfFavourites = (it[FAVOURITE_SOURCES] ?: emptySet()).toMutableList()
            listOfFavourites.remove(waterSource.id)

            it[FAVOURITE_SOURCES] = listOfFavourites.toSet()
        }

        deleteWaterSourceByIdUseCase.invoke(waterSource.id).fold(
            onSuccess = {
                firestore.collection(FIRESTORE_COLLECTION_WATER_SOURCES)
                    .document(waterSource.id)
                    .delete()
                    .addOnSuccessListener {
                        Log.d(
                            "LendingViewModel",
                            "Document deleted successfully ${waterSource.id}!"
                        )
                    }
                    .addOnFailureListener { e ->
                        Log.e(
                            "LendingViewModel",
                            "Error deleting document",
                            e
                        )
                    }
            },
            onFailure = {
                Log.e(
                    "LendingViewModel",
                    "Error deleting document",
                    it
                )
            }
        )
    }

    private fun loadWaterSourceMarkers() = viewModelScope.launch(Dispatchers.IO) {
        Log.v("Heavy methods logs", "Fetching latest water sources")

        // TODO - filter in review sources
        getAllWaterSourcesUseCase().fold({ waterSources ->
            waterSources.collectLatest {
                _waterSourceMarkers.emit(it)
            }
        }, { error ->
            Log.e("Heavy methods logs", "Failed to get water sources: ${error.message}")
            // Handle error and retry
        })
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