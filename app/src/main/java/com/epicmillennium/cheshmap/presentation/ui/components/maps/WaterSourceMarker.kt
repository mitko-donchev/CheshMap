package com.epicmillennium.cheshmap.presentation.ui.components.maps

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import com.epicmillennium.cheshmap.R
import com.epicmillennium.cheshmap.domain.marker.WaterSource
import com.epicmillennium.cheshmap.domain.marker.WaterSourceStatus
import com.epicmillennium.cheshmap.domain.marker.WaterSourceType
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.MarkerInfoWindow
import com.google.maps.android.compose.MarkerState

@Composable
fun WaterSourceMarker(
    waterSource: WaterSource,
    onMarkerInfoWindowsClicked: (WaterSource) -> Unit
) {

    val latLng = LatLng(waterSource.latitude, waterSource.longitude)

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

    MarkerInfoWindow(
        state = remember { MarkerState(position = latLng) },
        icon = BitmapDescriptorFactory.fromResource(R.drawable.ic_water_source_marker),
        onInfoWindowClick = {
            onMarkerInfoWindowsClicked.invoke(waterSource)
        },
    ) {
        // Custom MarkerInfoWindow content
        Card(
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(4.dp),
            modifier = Modifier
                .wrapContentHeight()
                .width(256.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth()
                    .wrapContentHeight()
            ) {
                // Image of the water source
                // Will comment out for now as I don't know how expensive it would be to load the images from the cloud every time
                // Also, there is an issue with https://github.com/coil-kt/coil/issues/159
                // TODO - check if we can afford loading images even here
//                Row(
//                    modifier = Modifier.fillMaxWidth(),
//                    horizontalArrangement = Arrangement.Center
//                ) {
//                    AsyncImage(
//                        model = ImageRequest.Builder(LocalContext.current)
//                            .data(if (waterSource.photos.isEmpty()) "" else waterSource.photos[0].imageUrl)
//                            .allowHardware(false)
//                            .build(),
//                        placeholder = painterResource(id = R.drawable.no_image),
//                        error = painterResource(id = R.drawable.no_image),
//                        contentDescription = stringResource(R.string.water_source_image),
//                        modifier = Modifier
//                            .size(224.dp)
//                            .clip(RoundedCornerShape(8.dp)),
//                        contentScale = ContentScale.Fit
//                    )
//                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = waterSource.name,
                    style = MaterialTheme.typography.titleLarge,
                )

                HorizontalDivider(
                    thickness = 2.dp,
                    modifier = Modifier.padding(bottom = 4.dp)
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = stringResource(R.string.type_with_columns, waterSourceType),
                    style = MaterialTheme.typography.bodyMedium,
                )

                Text(
                    text = stringResource(R.string.status_with_columns, waterSourceStatus),
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        }
    }
}