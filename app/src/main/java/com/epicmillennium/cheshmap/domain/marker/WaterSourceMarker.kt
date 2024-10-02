package com.epicmillennium.cheshmap.domain.marker

data class WaterSourceMarker(
    // Basic info
    val id: String,
    val name: String,

    // Marker Details
    val details: String,
    val latitude: Double,
    val longitude: Double,
    val type: WaterSourceType,
    val status: WaterSourceStatus,
    val photos: List<WaterSourceMarkerPhoto>,
)