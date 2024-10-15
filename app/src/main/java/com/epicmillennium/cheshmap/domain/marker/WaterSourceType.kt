package com.epicmillennium.cheshmap.domain.marker

enum class WaterSourceType {
    ESTABLISHMENT,
    URBAN_WATER,
    MINERAL_WATER,
    HOT_MINERAL_WATER,
    SPRING_WATER,
    NONE;

    companion object {
        fun fromOrdinal(ordinal: Int) = WaterSourceType.entries[ordinal]

        fun fromFirestoreString(firestoreString: String) =
            WaterSourceType.entries[firestoreString.toInt()]
    }
}