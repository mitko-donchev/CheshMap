package com.epicmillennium.cheshmap.domain.marker

enum class WaterSourceStatus {
    WORKING,
    UNDER_CONSTRUCTION,
    OUT_OF_ORDER,
    FOR_REVIEW;

    companion object {
        fun fromOrdinal(ordinal: Int) = WaterSourceStatus.entries[ordinal]
    }
}