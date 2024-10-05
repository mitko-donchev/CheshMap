package com.epicmillennium.cheshmap.presentation.ui.favourite

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun FavouriteScreen() {

    val favouriteViewModel: FavouriteViewModel = hiltViewModel()

    val favouriteUiState by favouriteViewModel.favouriteUiState.collectAsStateWithLifecycle()

    FavouriteView(
        favouriteUiState = favouriteUiState,
        favouriteViewModel::setWaterSourceFavouriteState
    )
}