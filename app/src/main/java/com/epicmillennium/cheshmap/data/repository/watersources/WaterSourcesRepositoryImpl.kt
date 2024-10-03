package com.epicmillennium.cheshmap.data.repository.watersources

import com.epicmillennium.cheshmap.domain.marker.WaterSource
import com.epicmillennium.cheshmap.domain.marker.WaterSourcesRepository

class WaterSourcesRepositoryImpl(
    private val waterSourcesTable: WaterSourcesTable
) : WaterSourcesRepository {
    override suspend fun addAll(waterSources: List<WaterSource>) =
        waterSourcesTable.insertAll(waterSources)

    override suspend fun getAll() = waterSourcesTable.getAll()
    override suspend fun getWaterSourceById(id: String) = waterSourcesTable.getWaterSourceById(id)
    override suspend fun deleteWaterSourceById(id: String) =
        waterSourcesTable.deleteWaterSourceById(id)

    override suspend fun deleteAll() = waterSourcesTable.deleteAllNotes()
}