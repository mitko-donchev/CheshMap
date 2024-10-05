package com.epicmillennium.cheshmap.domain.marker

import kotlinx.coroutines.flow.Flow

interface WaterSourcesRepository {
    suspend fun addWaterSource(waterSource: WaterSource)
    suspend fun addAll(waterSources: List<WaterSource>)
    suspend fun getAll(): Flow<List<WaterSource>>
    suspend fun getWaterSourceById(id: String): Flow<WaterSource?>
    suspend fun deleteWaterSourceById(id: String)
    suspend fun deleteAll()
}