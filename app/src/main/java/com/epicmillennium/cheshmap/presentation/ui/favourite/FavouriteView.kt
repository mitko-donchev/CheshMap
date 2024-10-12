package com.epicmillennium.cheshmap.presentation.ui.favourite

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.epicmillennium.cheshmap.core.ui.theme.CheshMapTheme
import com.epicmillennium.cheshmap.core.ui.theme.DarkTheme
import com.epicmillennium.cheshmap.core.ui.theme.LocalTheme
import com.epicmillennium.cheshmap.domain.marker.WaterSource
import com.epicmillennium.cheshmap.presentation.ui.components.FavouriteWaterSourceItem
import com.epicmillennium.cheshmap.presentation.ui.components.WaterSourceDetailsView
import kotlinx.coroutines.Job

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavouriteView(
    favouriteWaterSources: List<WaterSource>,
    onNavigateBack: () -> Unit,
    setWaterSourceFavouriteState: (Boolean, WaterSource) -> Job,
) {

    val lazyListState = rememberLazyListState()

    var waterSourceForFavStateAlter by remember { mutableStateOf<WaterSource?>(null) }

    CompositionLocalProvider(value = LocalTheme provides DarkTheme(LocalTheme.current.isDark)) {
        CheshMapTheme(darkTheme = LocalTheme.current.isDark) {
            var waterSourceForDetails by remember { mutableStateOf<WaterSource?>(null) }

            Scaffold(modifier = Modifier.fillMaxSize(),
                topBar = {
                    CenterAlignedTopAppBar(
                        title = { Text(text = "Favourites") },
                        navigationIcon = {
                            IconButton(onClick = onNavigateBack) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "Navigate back"
                                )
                            }
                        },
                        windowInsets = WindowInsets(0, 0, 0, 0)
                    )
                }
            ) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(it),
                    state = lazyListState,
                ) {
                    items(
                        favouriteWaterSources,
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
                            },
                            onFavItemClicked = {
                                waterSourceForDetails = it
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
                                false,
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

            AnimatedVisibility(
                visible = waterSourceForDetails != null,
                enter = scaleIn(animationSpec = tween(durationMillis = 500)),
                exit = scaleOut()
            ) {
                WaterSourceDetailsView(
                    waterSourceForDetails,
                    onCloseClick = { waterSourceForDetails = null },
                    onFavouriteIconClick = { isFavourite, waterSource ->
                        setWaterSourceFavouriteState.invoke(
                            isFavourite,
                            waterSource
                        )
                    },
                    deleteWaterSource = {
                    }
                )
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