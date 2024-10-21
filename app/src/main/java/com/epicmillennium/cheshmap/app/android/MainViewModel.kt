package com.epicmillennium.cheshmap.app.android

import android.util.Log
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.epicmillennium.cheshmap.domain.auth.FirestoreUser
import com.epicmillennium.cheshmap.domain.auth.User
import com.epicmillennium.cheshmap.domain.serverdata.FirestoreServerData
import com.epicmillennium.cheshmap.domain.usecase.auth.GetCurrentUserUseCase
import com.epicmillennium.cheshmap.domain.usecase.user.InsertUserUseCase
import com.epicmillennium.cheshmap.presentation.theme.AppThemeMode
import com.epicmillennium.cheshmap.utils.Constants.DID_FETCH_DATA_ONCE
import com.epicmillennium.cheshmap.utils.Constants.FETCH_ON_SERVER_TRUE
import com.epicmillennium.cheshmap.utils.Constants.FIRESTORE_COLLECTION_SERVER_DATA
import com.epicmillennium.cheshmap.utils.Constants.FIRESTORE_COLLECTION_USERS
import com.epicmillennium.cheshmap.utils.Constants.GLOBAL_THEME_MODE_KEY
import com.epicmillennium.cheshmap.utils.WaterSourcesRetrieverWorkerUtils
import com.epicmillennium.cheshmap.utils.preferences.UserPreferencesRepository
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val insertUserUseCase: InsertUserUseCase,
    private val currentUserUseCase: GetCurrentUserUseCase,
    private val userPreferencesRepository: UserPreferencesRepository,
    private val waterSourcesRetrieverWorkerUtils: WaterSourcesRetrieverWorkerUtils,
) : ViewModel() {

    private val _globalThemeState = MutableStateFlow(AppThemeMode.MODE_AUTO)
    val globalThemeState: StateFlow<AppThemeMode> = _globalThemeState

    init {
        fetchGlobalThemeMode()
        fetchServerData()
        fetchCurrentUser()
    }

    private fun fetchServerData() = viewModelScope.launch(Dispatchers.IO) {
        if (shouldFetchDataForFirstTime()) {
            Log.d("MainViewModel", "Fetching water sources")
            waterSourcesRetrieverWorkerUtils.retrieveWaterSources()
            userPreferencesRepository.dataStore.edit { it[DID_FETCH_DATA_ONCE] = true }
        } else {
            val data = firestore.collection(FIRESTORE_COLLECTION_SERVER_DATA)
                .get()
                .await()

            val serverData = data.documents.mapNotNull { documentSnapshot ->
                documentSnapshot.toObject(FirestoreServerData::class.java)
            }

            if (serverData.isNotEmpty() && serverData[0].shouldFetchLatestData && !didFetchDataAfterServerTrue()) {
                userPreferencesRepository.dataStore.edit {
                    it[FETCH_ON_SERVER_TRUE] = true
                }
                Log.d("MainViewModel", "Fetching water sources")
                waterSourcesRetrieverWorkerUtils.retrieveWaterSources()
            } else {
                if (didFetchDataAfterServerTrue()) {
                    userPreferencesRepository.dataStore.edit {
                        it[FETCH_ON_SERVER_TRUE] = false
                    }
                }
            }
        }
    }

    private fun fetchCurrentUser() = viewModelScope.launch(Dispatchers.IO) {
        currentUserUseCase.invoke().fold({ user ->
            user.collectLatest { currentUser ->
                if (currentUser.uid.isEmpty()) return@collectLatest

                val data = firestore.collection(FIRESTORE_COLLECTION_USERS)
                    .whereEqualTo("uid", currentUser.uid)
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

                if (convertedUsers.isEmpty()) return@collectLatest

                val convertedUser = convertedUsers.first()

                convertedUser?.let {
                    insertUserUseCase.invoke(it).fold({
                        Log.d("MainViewModel", "User inserted")
                    }, {
                        Log.d("MainViewModel", "Error inserting user")
                    })
                }
            }
        }, {
            Log.d("MainViewModel", "Error fetching current user")
        })
    }

    private fun fetchGlobalThemeMode() = viewModelScope.launch {
        userPreferencesRepository.dataStore.data.map { preferences: Preferences ->
            AppThemeMode.fromOrdinal(
                preferences[GLOBAL_THEME_MODE_KEY] ?: AppThemeMode.MODE_NIGHT.ordinal
            )
        }.collect { themeMode ->
            _globalThemeState.value = themeMode
        }
    }


    private suspend fun shouldFetchDataForFirstTime() =
        !userPreferencesRepository.dataStore.data.map { it[DID_FETCH_DATA_ONCE] ?: false }
            .first()

    private suspend fun didFetchDataAfterServerTrue() =
        userPreferencesRepository.dataStore.data.map { it[FETCH_ON_SERVER_TRUE] ?: false }
            .first()
}