package com.epicmillennium.cheshmap.presentation.ui.components.maps

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.FilledIconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.epicmillennium.cheshmap.R
import com.epicmillennium.cheshmap.presentation.ui.components.onDebounceClick

@Composable
fun CompassButton(bearing: Float, onCompassClick: () -> Unit) {
    FilledIconButton(
        modifier = Modifier
            .padding(14.dp)
            .size(34.dp),
        shape = CircleShape,
        onClick = onDebounceClick { onCompassClick() }
    ) {
        Image(
            painter = painterResource(id = R.drawable.ic_compass),
            contentDescription = stringResource(R.string.compass),
            contentScale = ContentScale.FillBounds,
            modifier = Modifier
                .fillMaxSize()
                .rotate(-bearing)  // Rotate based on bearing
        )
    }
}