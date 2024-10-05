package com.epicmillennium.cheshmap.presentation.ui.favourite

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.epicmillennium.cheshmap.core.ui.theme.CheshMapTheme
import com.epicmillennium.cheshmap.core.ui.theme.DarkTheme
import com.epicmillennium.cheshmap.core.ui.theme.LocalTheme
import com.epicmillennium.cheshmap.domain.marker.WaterSource
import com.epicmillennium.cheshmap.presentation.ui.components.FavouriteWaterSourceItem
import kotlinx.coroutines.Job

@Composable
fun FavouriteView(
    favouriteUiState: FavouriteViewState,
    setWaterSourceFavouriteState: (WaterSource) -> Job
) {
    val snackbarHostState = remember { SnackbarHostState() }

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        when (favouriteUiState.contentState) {
            is FavouriteViewContentState.Loading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Box(modifier = Modifier.size(48.dp)) {
                        CircularProgressIndicator()
                    }
                }
            }

            is FavouriteViewContentState.Success -> {
                val lazyListState = rememberLazyListState()

                var waterSourceForFavStateAlter by remember { mutableStateOf<WaterSource?>(null) }

                CompositionLocalProvider(value = LocalTheme provides DarkTheme(LocalTheme.current.isDark)) {
                    CheshMapTheme(darkTheme = LocalTheme.current.isDark) {
                        Scaffold(modifier = Modifier.fillMaxSize()) {
                            LazyColumn(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(it),
                                state = lazyListState,
                            ) {
                                items(
                                    favouriteUiState.contentState.favouriteState.favouriteWaterSources,
                                    key = { waterSource -> waterSource.id }
                                ) { waterSource ->
                                    FavouriteWaterSourceItem(
                                        modifier = Modifier.animateItem(
                                            fadeInSpec = tween(durationMillis = 250),
                                            fadeOutSpec = tween(durationMillis = 100),
                                            placementSpec = spring(
                                                stiffness = Spring.StiffnessLow,
                                                dampingRatio = Spring.DampingRatioMediumBouncy
                                            )
                                        ),
                                        waterSource,
                                        onFavouriteIconClick = {
                                            waterSourceForFavStateAlter = waterSource
                                        }
                                    )
                                }
                            }

                            AnimatedVisibility(
                                visible = waterSourceForFavStateAlter != null
                            ) {
                                FavouriteAlertDialog(
                                    waterSourceForFavStateAlter,
                                    onConfirmClick = {
                                        setWaterSourceFavouriteState.invoke(
                                            waterSourceForFavStateAlter!!
                                        )
                                        waterSourceForFavStateAlter = null
                                    },
                                    dismiss = {
                                        waterSourceForFavStateAlter = null
                                    }
                                )
                            }
                        }
                    }
                }
            }

            is FavouriteViewContentState.Error -> {
                LaunchedEffect(favouriteUiState.contentState.message) {
                    snackbarHostState.showSnackbar(
                        message = favouriteUiState.contentState.message,
                        duration = SnackbarDuration.Short
                    )
                }
            }
        }
    }
}

@Composable
fun FavouriteAlertDialog(
    waterSource: WaterSource?,
    onConfirmClick: () -> Unit,
    dismiss: () -> Unit
) {
    waterSource ?: return

    AlertDialog(
        title = { Text(text = "Remove from favourites") },
        text = { Text(text = "Are you sure you want to remove ${waterSource.name} from favourites?") },
        confirmButton = {
            TextButton(onClick = onConfirmClick) {
                Text(text = "Yes")
            }
        },
        dismissButton = {
            TextButton(onClick = dismiss) {
                Text(text = "No")
            }
        },
        onDismissRequest = dismiss,
    )
}