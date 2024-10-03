package com.epicmillennium.cheshmap.data.repository.watersources

import com.epicmillennium.cheshmap.domain.marker.WaterSource
import kotlinx.coroutines.flow.Flow

interface WaterSourcesTable {
    suspend fun insertAll(notes: List<WaterSource>)
    suspend fun getAll(): Flow<List<WaterSource>>
    suspend fun getWaterSourceById(id: String): Flow<WaterSource?>
    suspend fun deleteWaterSourceById(id: String)
    suspend fun deleteAllNotes()
}