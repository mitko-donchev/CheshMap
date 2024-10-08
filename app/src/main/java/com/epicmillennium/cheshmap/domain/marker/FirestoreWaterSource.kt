package com.epicmillennium.cheshmap.domain.marker

data class FirestoreWaterSource(
    val id: String = "",
    val name: String = "",
    val details: String = "",
    val latitude: String = "0.0",
    val longitude: String = "0.0",
    val type: String = "1",
    val status: String = "3",
    val photos: String = "none",
)