package com.epicmillennium.cheshmap.presentation.ui.favourite

import android.util.Log
import androidx.compose.runtime.Immutable
import androidx.datastore.preferences.core.edit
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.epicmillennium.cheshmap.domain.marker.WaterSource
import com.epicmillennium.cheshmap.domain.usecase.AddWaterSourceUseCase
import com.epicmillennium.cheshmap.domain.usecase.GetAllWaterSourcesUseCase
import com.epicmillennium.cheshmap.utils.Constants.FAVOURITE_SOURCES
import com.epicmillennium.cheshmap.utils.preferences.UserPreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FavouriteViewModel @Inject constructor(
    private val addWaterSourceUseCase: AddWaterSourceUseCase,
    private val getAllWaterSourcesUseCase: GetAllWaterSourcesUseCase,
    private val userPreferencesRepository: UserPreferencesRepository,
) : ViewModel() {

    private val _favouriteUiState =
        MutableStateFlow(FavouriteViewState(FavouriteViewContentState.Loading))
    val favouriteUiState = _favouriteUiState
        .onStart { loadFavouriteWaterSources() }
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(3000L),
            FavouriteViewState(FavouriteViewContentState.Loading)
        )

    fun setWaterSourceFavouriteState(
        isFavourite: Boolean,
        waterSource: WaterSource
    ) = viewModelScope.launch {
        userPreferencesRepository.dataStore.edit {
            val listOfFavourites = (it[FAVOURITE_SOURCES] ?: emptySet()).toMutableList()

            listOfFavourites.remove(waterSource.id)

            it[FAVOURITE_SOURCES] = listOfFavourites.toSet()
        }

        addWaterSourceUseCase.invoke(waterSource.copy(isFavourite = isFavourite))
    }

    private fun loadFavouriteWaterSources() = viewModelScope.launch(Dispatchers.IO) {
        _favouriteUiState.emit(FavouriteViewState(FavouriteViewContentState.Loading))

        Log.v("Heavy methods logs", "Fetching favourite water sources")
        getAllWaterSourcesUseCase().fold({ waterSources ->
            waterSources.collectLatest {
                val favWaterSources =
                    it.filter { waterSource -> getFavouriteWaterSources().contains(waterSource.id) }
                _favouriteUiState.update {
                    FavouriteViewState(
                        FavouriteViewContentState.Success(
                            FavouriteState(favWaterSources)
                        )
                    )
                }
            }
        }, { error ->
            Log.e("Heavy methods logs", "Failed to get fav water sources: ${error.message}")
            // Handle error and retry
        })
    }

    private suspend fun getFavouriteWaterSources(): Set<String> =
        userPreferencesRepository.dataStore.data.map { it[FAVOURITE_SOURCES] ?: emptySet() }.first()
}

@Immutable
data class FavouriteViewState(
    val contentState: FavouriteViewContentState
)

@Immutable
data class FavouriteState(val favouriteWaterSources: List<WaterSource>)

@Immutable
sealed class FavouriteViewContentState {
    data object Loading : FavouriteViewContentState()
    data class Success(val favouriteState: FavouriteState) : FavouriteViewContentState()
    data class Error(val message: String) : FavouriteViewContentState()

}