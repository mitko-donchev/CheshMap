package com.epicmillennium.cheshmap.presentation.ui.details

import android.util.Log
import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.epicmillennium.cheshmap.domain.marker.WaterSource
import com.epicmillennium.cheshmap.domain.usecase.GetWaterSourceByIdUseCase
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel(assistedFactory = DetailsScreenViewModel.DetailsScreenViewModelFactory::class)
class DetailsScreenViewModel @AssistedInject constructor(
    @Assisted val waterSourceId: String,
    private val getWaterSourceByIdUseCase: GetWaterSourceByIdUseCase,
) : ViewModel() {

    private val _detailsUiState =
        MutableStateFlow(DetailsViewState(DetailsViewContentState.Loading))
    val detailsUiState = _detailsUiState
        .onStart { loadWaterSourceDetails() }
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(3000L),
            DetailsViewState(DetailsViewContentState.Loading)
        )

    @AssistedFactory
    interface DetailsScreenViewModelFactory {
        fun create(waterSourceId: String): DetailsScreenViewModel
    }

    private fun loadWaterSourceDetails() = viewModelScope.launch(Dispatchers.IO) {
        _detailsUiState.update { it.copy(contentState = DetailsViewContentState.Loading) }

        getWaterSourceByIdUseCase(waterSourceId).fold({ waterSource ->
            waterSource.collectLatest {
                if (it == null) {
                    _detailsUiState.update { homeViewState ->
                        homeViewState.copy(
                            contentState = DetailsViewContentState.Error("Could not retrieve water source details")
                        )
                    }
                } else {
                    _detailsUiState.update { homeViewState ->
                        homeViewState.copy(
                            contentState = DetailsViewContentState.Success(
                                waterSourceState = WaterSourceState(it)
                            )
                        )
                    }
                }
            }
        }, { error ->
            Log.e(
                this.toString(),
                error.message ?: "Something went wrong while retrieving water source details!"
            )

            _detailsUiState.update {
                it.copy(
                    contentState = DetailsViewContentState.Error("Could not retrieve water source details")
                )
            }
        })
    }
}

@Immutable
data class DetailsViewState(
    val contentState: DetailsViewContentState
)

@Immutable
data class WaterSourceState(val waterSource: WaterSource)

@Immutable
sealed class DetailsViewContentState {
    data object Loading : DetailsViewContentState()
    data class Success(val waterSourceState: WaterSourceState) : DetailsViewContentState()
    data class Error(val message: String) : DetailsViewContentState()
}