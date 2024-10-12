package com.epicmillennium.cheshmap.presentation.ui.components.maps


import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.NearMe
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.epicmillennium.cheshmap.R
import com.epicmillennium.cheshmap.presentation.ui.components.onDebounceClick

@Composable
fun CenterOnUserLocationButton(onCenterOnUserLocationClicked: () -> Unit) {
    FilledIconButton(
        modifier = Modifier
            .padding(start = 14.dp, top = 14.dp, end = 14.dp)
            .size(34.dp),
        shape = CircleShape,
        onClick = onDebounceClick { onCenterOnUserLocationClicked() }
    ) {
        Icon(
            imageVector = Icons.Outlined.NearMe,
            contentDescription = stringResource(R.string.center_on_user_location)
        )
    }
}