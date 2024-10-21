package com.epicmillennium.cheshmap.presentation.ui.lending

import android.util.Log
import androidx.compose.runtime.Immutable
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.epicmillennium.cheshmap.domain.auth.FirestoreUser
import com.epicmillennium.cheshmap.domain.auth.User
import com.epicmillennium.cheshmap.domain.marker.FirestoreWaterSource
import com.epicmillennium.cheshmap.domain.marker.WaterSource
import com.epicmillennium.cheshmap.domain.usecase.auth.HasUserUseCase
import com.epicmillennium.cheshmap.domain.usecase.user.GetUserUseCase
import com.epicmillennium.cheshmap.domain.usecase.user.InsertUserUseCase
import com.epicmillennium.cheshmap.domain.usecase.watersource.AddWaterSourceUseCase
import com.epicmillennium.cheshmap.domain.usecase.watersource.DeleteWaterSourceByIdUseCase
import com.epicmillennium.cheshmap.domain.usecase.watersource.GetAllWaterSourcesUseCase
import com.epicmillennium.cheshmap.domain.usecase.watersource.GetWaterSourceByIdUseCase
import com.epicmillennium.cheshmap.presentation.theme.AppThemeMode
import com.epicmillennium.cheshmap.presentation.ui.components.maps.GPSService
import com.epicmillennium.cheshmap.presentation.ui.components.maps.Location
import com.epicmillennium.cheshmap.utils.Constants.FAVOURITE_SOURCES
import com.epicmillennium.cheshmap.utils.Constants.FIRESTORE_COLLECTION_USERS
import com.epicmillennium.cheshmap.utils.Constants.FIRESTORE_COLLECTION_WATER_SOURCES
import com.epicmillennium.cheshmap.utils.Constants.GLOBAL_THEME_MODE_KEY
import com.epicmillennium.cheshmap.utils.Constants.USER_LOCATION_TRACKING_ENABLED_KEY
import com.epicmillennium.cheshmap.utils.preferences.UserPreferencesRepository
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

@HiltViewModel
class LendingViewModel @Inject constructor(
    private val gpsService: GPSService,
    private val firestore: FirebaseFirestore,
    private val insertUserUseCase: InsertUserUseCase,
    private val addWaterSourceUseCase: AddWaterSourceUseCase,
    private val getWaterSourceByIdUseCase: GetWaterSourceByIdUseCase,
    private val getAllWaterSourcesUseCase: GetAllWaterSourcesUseCase,
    private val userPreferencesRepository: UserPreferencesRepository,
    private val deleteWaterSourceByIdUseCase: DeleteWaterSourceByIdUseCase,
    private val hasUserUseCase: HasUserUseCase,
    private val getUserUseCase: GetUserUseCase
) : ViewModel() {

    private val _lendingUiState =
        MutableStateFlow(LendingViewState(LendingViewContentState.Loading))
    val lendingUiState = _lendingUiState
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(3000L),
            LendingViewState(LendingViewContentState.Loading)
        )

    private val _hasUser = MutableStateFlow(false)
    val hasUser = _hasUser
        .onStart { fetchHasUser() }
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(3000L),
            false
        )

    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser = _currentUser
        .onStart { fetchCurrentUser() }
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(3000L),
            null
        )

    private val _userLocation = MutableStateFlow(Location(0.0, 0.0))
    val userLocation = _userLocation.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(3000L),
        Location(0.0, 0.0)
    )

    private val _waterSourceInfo = MutableStateFlow<WaterSource?>(null)
    val waterSourceInfo = _waterSourceInfo.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(3000L),
        null
    )

    private val _waterSourceMarkers = MutableStateFlow(emptyList<WaterSource>())
    val waterSourceMarkers = _waterSourceMarkers
        .onStart { loadWaterSourceMarkers() }
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(3000L),
            emptyList()
        )

    private val _globalThemeState = MutableStateFlow(AppThemeMode.MODE_AUTO)
    val globalThemeState = _globalThemeState
        .onStart { fetchGlobalThemeMode() }
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(3000L),
            AppThemeMode.MODE_AUTO
        )

    private val _isUserLocationTrackingEnabled = MutableStateFlow(true)
    val isUserLocationTrackingEnabled = _isUserLocationTrackingEnabled
        .onStart { fetchUserLocationTrackingEnabled() }
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(3000L),
            true
        )

    fun setGlobalThemeMode(appThemeMode: AppThemeMode) = viewModelScope.launch {
        userPreferencesRepository.dataStore.edit {
            it[GLOBAL_THEME_MODE_KEY] = appThemeMode.ordinal
        }
    }

    fun setUserLocationTrackingEnabled(isEnabled: Boolean) = viewModelScope.launch {
        userPreferencesRepository.dataStore.edit {
            it[USER_LOCATION_TRACKING_ENABLED_KEY] = isEnabled
        }
    }

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

    fun fetchWaterSourceInfo(waterSourceId: String) = viewModelScope.launch(Dispatchers.IO) {
        updateWaterSourceInfo(waterSourceId)

        getWaterSourceByIdUseCase.invoke(waterSourceId).fold({ waterSource ->
            waterSource.collectLatest {
                _waterSourceInfo.tryEmit(it)
            }
        }, { error ->
            Log.e("LendingViewModel", "Error fetching water source info", error)
        })
    }

    fun likeOrDislikeWaterSource(shouldLike: Boolean, shouldReset: Boolean) =
        viewModelScope.launch(Dispatchers.IO) {
            Log.d(
                "LendingViewModel",
                "Should like: $shouldLike water source: ${waterSourceInfo.value}"
            )

            try {
                val currentUser = currentUser.value ?: return@launch
                val waterSource = waterSourceInfo.value ?: return@launch

                val getIfItsAlreadyLiked = currentUser.likedSourcesIds.contains(waterSource.id)
                val getIfItsAlreadyDisliked =
                    currentUser.dislikedSourcesIds.contains(waterSource.id)

                if (shouldLike && getIfItsAlreadyLiked) return@launch
                if (!shouldLike && getIfItsAlreadyDisliked) return@launch

                if (getIfItsAlreadyLiked && (waterSource.totalLikes - 1 < 0)) return@launch
                if (getIfItsAlreadyDisliked && (waterSource.totalDislikes - 1 < 0)) return@launch

                val likedSourcesIds = currentUser.likedSourcesIds.toMutableList()
                val dislikedSourcesIds = currentUser.dislikedSourcesIds.toMutableList()

                if (shouldLike) {
                    if (!shouldReset) {
                        likedSourcesIds.add(waterSource.id)
                    }
                    dislikedSourcesIds.remove(waterSource.id)
                } else {
                    if (!shouldReset) {
                        dislikedSourcesIds.add(waterSource.id)
                    }
                    likedSourcesIds.remove(waterSource.id)
                }

                val updatedUser = currentUser.copy(
                    likedSourcesIds = likedSourcesIds,
                    dislikedSourcesIds = dislikedSourcesIds
                )

                val updatedWaterSource = waterSource.copy(
                    totalLikes = getTotalLikes(
                        shouldLike,
                        shouldReset,
                        waterSource,
                        getIfItsAlreadyLiked
                    ),
                    totalDislikes = getTotalDislikes(
                        shouldLike,
                        shouldReset,
                        waterSource,
                        getIfItsAlreadyDisliked
                    )
                )

                updateWaterSourceFirestoreData(
                    updatedUser,
                    updatedWaterSource
                )
            } catch (e: Exception) {
                Log.e("LendingViewModel", "Error liking the water source", e)
            }
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

    private fun updateWaterSourceInfo(waterSourceId: String) =
        viewModelScope.launch(Dispatchers.IO) {
            val documentSnapshot = firestore.collection(FIRESTORE_COLLECTION_WATER_SOURCES)
                .document(waterSourceId)
                .get()
                .addOnFailureListener {
                    Log.e("LendingViewModel", "Error fetching water source info", it)
                }
                .await() // Use await to suspend and wait for the result

            // Convert Firestore document to FirestoreWaterSource and include the document ID
            val firestoreWaterSource = documentSnapshot.toObject(FirestoreWaterSource::class.java)

            // Use documentSnapshot.id as the Firestore ID
            val waterSourceWithId = firestoreWaterSource?.copy(id = documentSnapshot.id)

            // Convert FirestoreWaterSource to domain model WaterSource and emit
            val waterSource = WaterSource.fromFirestoreWaterSourceButCouldBeNull(waterSourceWithId)

            Log.d("LendingViewModel", "Fetched water source info: $waterSource")
            waterSource?.let { addWaterSourceUseCase.invoke(it) }
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

    private fun fetchGlobalThemeMode() = viewModelScope.launch {
        userPreferencesRepository.dataStore.data.map { preferences: Preferences ->
            AppThemeMode.fromOrdinal(
                preferences[GLOBAL_THEME_MODE_KEY] ?: AppThemeMode.MODE_NIGHT.ordinal
            )
        }.collect {
            _globalThemeState.value = it
        }
    }

    private fun fetchUserLocationTrackingEnabled() = viewModelScope.launch {
        userPreferencesRepository.dataStore.data.map { preferences: Preferences ->
            preferences[USER_LOCATION_TRACKING_ENABLED_KEY] ?: true
        }.collect {
            _isUserLocationTrackingEnabled.value = it
        }
    }

    private fun fetchHasUser() = viewModelScope.launch {
        hasUserUseCase().fold({ hasUser ->
            _hasUser.emit(hasUser)
        }, { error ->
            Log.e("LendingViewModel", "Error fetching user", error)
        })
    }

    private fun fetchCurrentUser() = viewModelScope.launch {
        getUserUseCase.invoke().fold({ user ->
            user.collectLatest {
                _currentUser.emit(it)
            }
        }, { error ->
            Log.e("LendingViewModel", "Error fetching user", error)
        })
    }

    private fun updateUserFirestoreData(currentUser: User) {
        firestore.collection(FIRESTORE_COLLECTION_USERS)
            .document(currentUser.documentId)
            .update(
                "liked", currentUser.likedSourcesIds,
                "disliked", currentUser.dislikedSourcesIds
            )
            .addOnSuccessListener {
                updateCurrentUser(currentUser.uid)
            }
            .addOnFailureListener { e ->
                Log.e(
                    "LendingViewModel",
                    "Error liking the water source",
                    e
                )
            }
    }

    private suspend fun updateWaterSourceFirestoreData(
        updatedUser: User,
        updatedWaterSource: WaterSource
    ) {
        firestore.collection(FIRESTORE_COLLECTION_WATER_SOURCES)
            .document(updatedWaterSource.id)
            .update(
                "totalLikes",
                updatedWaterSource.totalLikes,
                "totalDislikes",
                updatedWaterSource.totalDislikes
            )
            .addOnSuccessListener {
                Log.d(
                    "LendingViewModel",
                    "Adding one like to ${updatedWaterSource.id}!"
                )

                updateWaterSourceInfo(updatedWaterSource.id)

                updateUserFirestoreData(updatedUser)
            }
            .addOnFailureListener { e ->
                Log.e(
                    "LendingViewModel",
                    "Error liking the water source",
                    e
                )
            }
            .await()
    }

    private fun updateCurrentUser(uid: String) = viewModelScope.launch(Dispatchers.IO) {
        if (uid.isEmpty()) return@launch

        val data = firestore.collection(FIRESTORE_COLLECTION_USERS)
            .whereEqualTo("uid", uid)
            .get()
            .addOnFailureListener {
                Log.d("MainViewModel", "Error fetching current user")
            }
            .await() // Use await to suspend and wait for the result

        val convertedUsers = data.documents.map { documentSnapshot ->
            val firestoreUser = documentSnapshot.toObject(FirestoreUser::class.java)

            val convUser =
                User.fromFirestoreUserButCouldBeNull(firestoreUser) ?: return@map null

            convUser.copy(documentId = documentSnapshot.id)
        }

        if (convertedUsers.isEmpty()) return@launch

        val convertedUser = convertedUsers.first()

        convertedUser?.let {
            insertUserUseCase.invoke(it).fold({
                Log.d("MainViewModel", "User inserted")
            }, {
                Log.d("MainViewModel", "Error inserting user")
            })
        }
    }

    private fun getTotalLikes(
        shouldLike: Boolean,
        shouldReset: Boolean,
        waterSource: WaterSource,
        getIfItsAlreadyLiked: Boolean
    ) =
        when {
            shouldReset && !shouldLike -> waterSource.totalLikes - 1
            shouldReset && shouldLike -> waterSource.totalLikes
            shouldLike -> waterSource.totalLikes + 1
            getIfItsAlreadyLiked -> waterSource.totalLikes - 1
            else -> waterSource.totalLikes
        }

    private fun getTotalDislikes(
        shouldLike: Boolean,
        shouldReset: Boolean,
        waterSource: WaterSource,
        getIfItsAlreadyDisliked: Boolean
    ) =
        when {
            shouldReset && shouldLike -> waterSource.totalDislikes - 1
            shouldReset && !shouldLike -> waterSource.totalDislikes
            !shouldLike -> waterSource.totalDislikes + 1
            getIfItsAlreadyDisliked -> waterSource.totalDislikes - 1
            else -> waterSource.totalDislikes
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