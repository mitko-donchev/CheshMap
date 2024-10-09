package com.epicmillennium.cheshmap.domain.marker

import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.clustering.ClusterItem

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

    // Other
    val isFavourite: Boolean = false
) : ClusterItem {
    companion object {
        fun fromFirestoreWaterSource(firestoreWaterSource: FirestoreWaterSource) = WaterSource(
            firestoreWaterSource.id,
            firestoreWaterSource.name,
            firestoreWaterSource.details,
            firestoreWaterSource.latitude.toDouble(),
            firestoreWaterSource.longitude.toDouble(),
            WaterSourceType.fromFirestoreString(firestoreWaterSource.type),
            WaterSourceStatus.fromFirestoreString(firestoreWaterSource.status),
            processPhotosString(firestoreWaterSource.photos)
        )

        private fun processPhotosString(photosString: String?): List<WaterSourcePhoto> {
            val googleDrivePattern = "https://drive.google.com/open?id="
            val googleDriveReplacementPrefix = "https://drive.usercontent.google.com/download?id="
            val googleDriveReplacementSuffix = "&export=view&authuser=0"

            return photosString?.takeIf { it != "none" && it.isNotBlank() }
                ?.split(",")
                ?.map { it.trim() }
                ?.filter { it.isNotEmpty() }
                ?.map {
                    if (it.startsWith(googleDrivePattern)) {
                        val processedUrl =
                            it.replace(googleDrivePattern, googleDriveReplacementPrefix)
                        processedUrl + googleDriveReplacementSuffix
                    } else {
                        it // Leave the URL unchanged if it's not a Google Drive link
                    }
                }
                ?.map { WaterSourcePhoto(it) }
                ?: emptyList()
        }
    }

    override fun getPosition(): LatLng = LatLng(latitude, longitude)

    override fun getTitle(): String = name

    override fun getSnippet(): String? = null

    override fun getZIndex(): Float = 1f
}