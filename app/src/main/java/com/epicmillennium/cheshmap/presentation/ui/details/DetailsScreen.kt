package com.epicmillennium.cheshmap.presentation.ui.details


import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun DetailsScreen(waterSourceId: String) {

    val detailsScreenViewModel =
        hiltViewModel<DetailsScreenViewModel, DetailsScreenViewModel.DetailsScreenViewModelFactory> { factory ->
            factory.create(waterSourceId)
        }

    val detailsUiState by detailsScreenViewModel.detailsUiState.collectAsStateWithLifecycle()

    DetailsView(
        detailsUiState = detailsUiState
    )
}
