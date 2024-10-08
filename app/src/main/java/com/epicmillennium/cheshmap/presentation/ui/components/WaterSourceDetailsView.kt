package com.epicmillennium.cheshmap.presentation.ui.components

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Directions
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconToggleButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.epicmillennium.cheshmap.R
import com.epicmillennium.cheshmap.domain.marker.WaterSource
import com.epicmillennium.cheshmap.domain.marker.WaterSourceStatus
import com.epicmillennium.cheshmap.domain.marker.WaterSourceType

@Composable
fun WaterSourceDetailsView(
    waterSource: WaterSource?,
    onCloseClick: () -> Unit,
    deleteWaterSource: (WaterSource) -> Unit,
    onFavouriteIconClick: (Boolean, WaterSource) -> Unit,
) {
    waterSource ?: return

    val context = LocalContext.current

    val waterSourceStatus = stringResource(
        when (waterSource.status) {
            WaterSourceStatus.WORKING -> R.string.working
            WaterSourceStatus.UNDER_CONSTRUCTION -> R.string.under_construction
            WaterSourceStatus.OUT_OF_ORDER -> R.string.out_of_order
            WaterSourceStatus.FOR_REVIEW -> R.string.for_review
        }
    )

    val waterSourceType = stringResource(
        when (waterSource.type) {
            WaterSourceType.ESTABLISHMENT -> R.string.establishment
            WaterSourceType.URBAN_WATER -> R.string.urban_water_source
            WaterSourceType.MINERAL_WATER -> R.string.mineral_water
            WaterSourceType.HOT_MINERAL_WATER -> R.string.hot_mineral_water
            WaterSourceType.SPRING_WATER -> R.string.spring_water
        }
    )

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            DetailsTopBar(waterSource, onCloseClick = onCloseClick)
        },
        bottomBar = {
            DetailsBottomBar(
                context,
                waterSource,
                onFavouriteIconClick = { onFavouriteIconClick.invoke(it, waterSource) },
                onDeleteIconClick = {
                    deleteWaterSource.invoke(waterSource)
                    onCloseClick.invoke()
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Image of the water source
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(if (waterSource.photos.isEmpty()) "" else waterSource.photos[0].imageUrl)
                        .build(),
                    placeholder = painterResource(id = R.drawable.no_image),
                    error = painterResource(id = R.drawable.no_image),
                    contentDescription = stringResource(R.string.water_source_image),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .clip(RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Fit
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp)
            ) {
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                }

                item {
                    Text(
                        text = stringResource(R.string.type),
                        style = MaterialTheme.typography.titleMedium,
                    )
                    HorizontalDivider(
                        thickness = 2.dp,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                    Text(
                        text = waterSourceType,
                        style = MaterialTheme.typography.bodyMedium,
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = stringResource(R.string.status),
                        style = MaterialTheme.typography.titleMedium,
                    )
                    HorizontalDivider(
                        thickness = 2.dp,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                    Text(
                        text = waterSourceStatus,
                        style = MaterialTheme.typography.bodyMedium,
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = stringResource(R.string.details),
                        style = MaterialTheme.typography.titleMedium,
                    )
                    HorizontalDivider(
                        thickness = 2.dp,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                    Text(
                        text = waterSource.details,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }

                item {
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun DetailsTopBar(
    waterSource: WaterSource,
    onCloseClick: () -> Unit
) {
    CenterAlignedTopAppBar(
        title = {
            Text(
                text = waterSource.name,
                modifier = Modifier.wrapContentWidth()
            )
        },
        actions = {
            IconButton(onClick = onCloseClick) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Close details"
                )
            }
        },
        windowInsets = WindowInsets(
            top = 0.dp,
            bottom = 0.dp
        )
    )
}

@Composable
private fun DetailsBottomBar(
    context: Context,
    waterSource: WaterSource,
    onDeleteIconClick: () -> Unit,
    onFavouriteIconClick: (Boolean) -> Unit,
) {
    var favouriteState by remember { mutableStateOf(waterSource.isFavourite) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .padding(8.dp),
    ) {
        // TODO - hide before release (don't need this for normal users)
        Icon(
            imageVector = Icons.Default.Delete,
            tint = Color.Red,
            contentDescription = "Delete source button",
            modifier = Modifier
                .align(Alignment.CenterStart)
                .clickable {
                    onDeleteIconClick.invoke()
                }
        )

        IconToggleButton(
            checked = favouriteState,
            onCheckedChange = {
                favouriteState = it
                onFavouriteIconClick.invoke(it)
            },
            modifier = Modifier.padding(start = 32.dp)
        ) {
            if (favouriteState) {
                Icon(Icons.Default.Favorite, contentDescription = "Favourite water source button")
            } else {
                Icon(
                    Icons.Default.FavoriteBorder,
                    contentDescription = "Un-favourite water source button"
                )
            }
        }

        Button(
            modifier = Modifier
                .wrapContentWidth()
                .align(Alignment.CenterEnd),
            onClick = {
                // open Google Maps
                val gmmIntentUri =
                    "http://maps.google.com/maps?q=loc:" + waterSource.latitude + "," + waterSource.longitude
                val mapIntent =
                    Intent(Intent.ACTION_VIEW, Uri.parse(gmmIntentUri))
                context.startActivity(mapIntent)
            },
        ) {
            Icon(
                imageVector = Icons.Default.Directions,
                contentDescription = "Directions button icon",
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = stringResource(R.string.take_me_there))
        }
    }
}