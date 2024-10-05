package com.epicmillennium.cheshmap.presentation.ui.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.Icon
import androidx.compose.material3.IconToggleButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.epicmillennium.cheshmap.R
import com.epicmillennium.cheshmap.domain.marker.WaterSource
import com.epicmillennium.cheshmap.domain.marker.WaterSourceStatus
import com.epicmillennium.cheshmap.domain.marker.WaterSourceType

@Composable
fun FavouriteWaterSourceItem(
    modifier: Modifier,
    waterSource: WaterSource,
    onFavouriteIconClick: (Boolean) -> Unit
) {
    Row(
        modifier = modifier
            .wrapContentHeight()
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        var favouriteState by remember { mutableStateOf(waterSource.isFavourite) }

        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(if (waterSource.photos.isEmpty()) "" else waterSource.photos[0].imageUrl)
                .allowHardware(false)
                .build(),
            placeholder = painterResource(id = R.drawable.no_image),
            error = painterResource(id = R.drawable.no_image),
            contentDescription = stringResource(R.string.water_source_image),
            modifier = Modifier
                .size(64.dp)
                .clip(RoundedCornerShape(8.dp)),
            contentScale = ContentScale.Fit
        )

        Text(text = waterSource.name)

        Spacer(modifier = Modifier.width(16.dp))

        IconToggleButton(checked = favouriteState,
            onCheckedChange = {
                favouriteState = it
                onFavouriteIconClick.invoke(it)
            }
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
    }
}

@Preview(showBackground = true, showSystemUi = true, device = Devices.PIXEL_7_PRO)
@Composable
fun FavouriteWaterSourceItemPreview() {
    FavouriteWaterSourceItem(
        modifier = Modifier,
        WaterSource(
            id = "1",
            name = "Water Source 1",
            latitude = 0.0,
            longitude = 0.0,
            type = WaterSourceType.ESTABLISHMENT,
            status = WaterSourceStatus.WORKING,
            photos = emptyList(),
            isFavourite = true,
            details = "This is a water source"
        ),
        onFavouriteIconClick = {}
    )
}