package com.epicmillennium.cheshmap.domain.marker

import androidx.annotation.Keep
import kotlinx.serialization.Serializable

@Keep
@Serializable
data class FirestoreWaterSource(
    @Keep val id: String = "",
    @Keep val name: String = "",
    @Keep val details: String = "",
    @Keep val latitude: String = "0.0",
    @Keep val longitude: String = "0.0",
    @Keep val type: String = "1",
    @Keep val status: String = "3",
    @Keep val photos: String = "none",
)