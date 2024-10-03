package com.epicmillennium.cheshmap.data.source.room.entity

import android.os.Parcelable
import android.util.Log
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.epicmillennium.cheshmap.domain.marker.WaterSource
import com.epicmillennium.cheshmap.domain.marker.WaterSourcePhoto
import com.epicmillennium.cheshmap.domain.marker.WaterSourceStatus
import com.epicmillennium.cheshmap.domain.marker.WaterSourceType
import kotlinx.parcelize.Parcelize

@Parcelize
@Entity(tableName = "water_sources")
data class WaterSourceEntity(
    @PrimaryKey(autoGenerate = false)
    var id: String,
    val name: String,
    val details: String,
    val latitude: Double,
    val longitude: Double,
    val type: Int,
    val status: Int,
    val photos: List<String>
) : Parcelable {
    companion object {
        fun fromWaterSource(waterSource: WaterSource): WaterSourceEntity {
            Log.d("WaterSourceEntity", "Mapping WaterSource with ID: ${waterSource.id}")
            return WaterSourceEntity(
                waterSource.id,
                waterSource.name,
                waterSource.details,
                waterSource.latitude,
                waterSource.longitude,
                waterSource.type.ordinal,
                waterSource.status.ordinal,
                waterSource.photos.map { it.imageUrl }
            )
        }

        fun toWaterSource(waterSourceEntity: WaterSourceEntity): WaterSource {
            return WaterSource(
                waterSourceEntity.id,
                waterSourceEntity.name,
                waterSourceEntity.details,
                waterSourceEntity.latitude,
                waterSourceEntity.longitude,
                WaterSourceType.fromOrdinal(waterSourceEntity.type),
                WaterSourceStatus.fromOrdinal(waterSourceEntity.status),
                waterSourceEntity.photos.map { WaterSourcePhoto(it) }
            )
        }
    }
}