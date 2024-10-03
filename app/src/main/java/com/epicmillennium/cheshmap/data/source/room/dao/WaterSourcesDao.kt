package com.epicmillennium.cheshmap.data.source.room.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.epicmillennium.cheshmap.data.source.room.entity.WaterSourceEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface WaterSourcesDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(waterSources: List<WaterSourceEntity>)

    @Query("SELECT * FROM water_sources")
    fun getAll(): Flow<List<WaterSourceEntity>>

    @Query("SELECT * FROM water_sources WHERE id = :id")
    fun getWaterSourceById(id: String): Flow<WaterSourceEntity?>

    @Query("DELETE FROM water_sources WHERE id = :id")
    fun deleteWaterSourceById(id: String)

    @Query("DELETE FROM water_sources")
    fun deleteAllNotes()
}