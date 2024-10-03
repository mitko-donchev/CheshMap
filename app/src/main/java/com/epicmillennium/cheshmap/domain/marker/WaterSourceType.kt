package com.epicmillennium.cheshmap.domain.marker

enum class WaterSourceType {
    ESTABLISHMENT,
    URBAN_WATER,
    MINERAL_WATER,
    HOT_MINERAL_WATER,
    SPRING_WATER;

    companion object {
        fun fromOrdinal(ordinal: Int) = WaterSourceType.entries[ordinal]
    }
}