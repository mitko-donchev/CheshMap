package com.epicmillennium.cheshmap.domain.marker

data class FirestoreWaterSource(
    // Basic info
    val id: String = "",
    val name: String = "",

    // Marker Details
    val details: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val type: Int = 1,
    val status: Int = 3,
    val photos: List<String> = emptyList(),
)
