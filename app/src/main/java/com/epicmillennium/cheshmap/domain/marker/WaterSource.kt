package com.epicmillennium.cheshmap.domain.marker

data class WaterSource(
    // Basic info
    val id: String,
    val name: String,

    // Marker Details
    val details: String,
    val latitude: Double,
    val longitude: Double,
    val type: WaterSourceType,
    val status: WaterSourceStatus,
    val photos: List<WaterSourcePhoto>,
) {
    companion object {
        fun fromFirestoreWaterSource(firestoreWaterSource: FirestoreWaterSource) = WaterSource(
            firestoreWaterSource.id,
            firestoreWaterSource.name,
            firestoreWaterSource.details,
            firestoreWaterSource.latitude,
            firestoreWaterSource.longitude,
            WaterSourceType.fromOrdinal(firestoreWaterSource.type),
            WaterSourceStatus.fromOrdinal(firestoreWaterSource.status),
            firestoreWaterSource.photos.map { WaterSourcePhoto(it) }
        )
    }
}