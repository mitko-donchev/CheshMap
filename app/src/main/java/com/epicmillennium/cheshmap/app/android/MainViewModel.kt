package com.epicmillennium.cheshmap.app.android

import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.epicmillennium.cheshmap.core.ui.theme.AppThemeMode
import com.epicmillennium.cheshmap.domain.serverdata.FirestoreServerData
import com.epicmillennium.cheshmap.utils.Constants.DID_FETCH_DATA_ONCE
import com.epicmillennium.cheshmap.utils.Constants.DID_FETCH_DATA_ONCE_AFTER_SERVER_TRUE
import com.epicmillennium.cheshmap.utils.Constants.FIRESTORE_COLLECTION_SERVER_DATA
import com.epicmillennium.cheshmap.utils.Constants.GLOBAL_THEME_MODE_KEY
import com.epicmillennium.cheshmap.utils.WaterSourcesRetrieverWorkerUtils
import com.epicmillennium.cheshmap.utils.preferences.UserPreferencesRepository
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val userPreferencesRepository: UserPreferencesRepository,
    private val waterSourcesRetrieverWorkerUtils: WaterSourcesRetrieverWorkerUtils,
) : ViewModel() {

    private val _globalThemeState = MutableStateFlow(AppThemeMode.MODE_NIGHT)
    val globalThemeState: StateFlow<AppThemeMode> = _globalThemeState

    init {
        fetchGlobalThemeMode()
        fetchServerData()
    }

    private fun fetchServerData() = viewModelScope.launch {
        if (shouldFetchDataForFirstTime()) {
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
                    it[DID_FETCH_DATA_ONCE_AFTER_SERVER_TRUE] = true
                }
                waterSourcesRetrieverWorkerUtils.retrieveWaterSources()
            } else {
                if (didFetchDataAfterServerTrue()) {
                    userPreferencesRepository.dataStore.edit {
                        it[DID_FETCH_DATA_ONCE_AFTER_SERVER_TRUE] = false
                    }
                }
            }
        }
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
        !userPreferencesRepository.dataStore.data.map { it[DID_FETCH_DATA_ONCE] ?: true }.first()

    private suspend fun didFetchDataAfterServerTrue() =
        userPreferencesRepository.dataStore.data.map {
            it[DID_FETCH_DATA_ONCE_AFTER_SERVER_TRUE] ?: false
        }.first()
}