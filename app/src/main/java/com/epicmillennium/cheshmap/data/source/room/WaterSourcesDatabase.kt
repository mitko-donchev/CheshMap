package com.epicmillennium.cheshmap.data.source.room

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.epicmillennium.cheshmap.data.source.room.converter.StringListConverter
import com.epicmillennium.cheshmap.data.source.room.dao.WaterSourcesDao
import com.epicmillennium.cheshmap.data.source.room.entity.WaterSourceEntity

@TypeConverters(StringListConverter::class)
@Database(entities = [WaterSourceEntity::class], version = 1)
abstract class WaterSourcesDatabase : RoomDatabase() {
    abstract fun waterSourcesDao(): WaterSourcesDao
}