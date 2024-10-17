package com.epicmillennium.cheshmap.data.source.room.entity

import android.os.Parcelable
import androidx.room.ColumnInfo
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
    val photos: List<String>,
    @ColumnInfo(defaultValue = 0.toString())
    val isFavourite: Boolean = false,
    @ColumnInfo(defaultValue = 0.toString())
    val totalLikes: Long = 0,
    @ColumnInfo(defaultValue = 0.toString())
    val totalDislikes: Long = 0
) : Parcelable {
    companion object {
        fun fromWaterSource(waterSource: WaterSource) = WaterSourceEntity(
            waterSource.id,
            waterSource.name,
            waterSource.details,
            waterSource.latitude,
            waterSource.longitude,
            waterSource.type.ordinal,
            waterSource.status.ordinal,
            waterSource.photos.map { it.imageUrl },
            waterSource.isFavourite,
            waterSource.totalLikes,
            waterSource.totalDislikes
        )

        fun toWaterSource(waterSourceEntity: WaterSourceEntity) = WaterSource(
            waterSourceEntity.id,
            waterSourceEntity.name,
            waterSourceEntity.details,
            waterSourceEntity.latitude,
            waterSourceEntity.longitude,
            WaterSourceType.fromOrdinal(waterSourceEntity.type),
            WaterSourceStatus.fromOrdinal(waterSourceEntity.status),
            waterSourceEntity.photos.map { WaterSourcePhoto(it) },
            waterSourceEntity.totalLikes,
            waterSourceEntity.totalDislikes,
            waterSourceEntity.isFavourite
        )
    }
}