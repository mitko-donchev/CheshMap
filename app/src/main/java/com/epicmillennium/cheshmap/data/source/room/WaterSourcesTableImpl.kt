package com.epicmillennium.cheshmap.data.source.room

import com.epicmillennium.cheshmap.data.repository.watersources.WaterSourcesTable
import com.epicmillennium.cheshmap.data.source.room.entity.WaterSourceEntity
import com.epicmillennium.cheshmap.domain.marker.WaterSource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class WaterSourcesTableImpl(
    private val waterSourcesDatabase: WaterSourcesDatabase
) : WaterSourcesTable {
    override suspend fun insertWaterSource(waterSource: WaterSource) {
        waterSourcesDatabase.waterSourcesDao()
            .insertWaterSource(WaterSourceEntity.fromWaterSource(waterSource))
    }

    override suspend fun insertAll(waterSources: List<WaterSource>) {
        waterSourcesDatabase.waterSourcesDao()
            .insertAll(waterSources.map { WaterSourceEntity.fromWaterSource(it) })
    }

    override suspend fun getAll(): Flow<List<WaterSource>> {
        return waterSourcesDatabase.waterSourcesDao().getAll().map { waterSourceEntities ->
            waterSourceEntities.map {
                WaterSourceEntity.toWaterSource(it)
            }
        }
    }

    override suspend fun getWaterSourceById(id: String): Flow<WaterSource?> {
        return waterSourcesDatabase.waterSourcesDao().getWaterSourceById(id)
            .map { it?.let { WaterSourceEntity.toWaterSource(it) } }
    }

    override suspend fun deleteWaterSourceById(id: String) =
        waterSourcesDatabase.waterSourcesDao().deleteWaterSourceById(id)

    override suspend fun deleteAllNotes() = waterSourcesDatabase.waterSourcesDao().deleteAllNotes()
}