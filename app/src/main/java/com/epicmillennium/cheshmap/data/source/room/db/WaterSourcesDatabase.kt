package com.epicmillennium.cheshmap.data.source.room.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.epicmillennium.cheshmap.data.source.room.converter.StringListConverter
import com.epicmillennium.cheshmap.data.source.room.dao.WaterSourcesDao
import com.epicmillennium.cheshmap.data.source.room.entity.WaterSourceEntity

@TypeConverters(StringListConverter::class)
@Database(
    version = 1,
    entities = [WaterSourceEntity::class],
)
abstract class WaterSourcesDatabase : RoomDatabase() {
    abstract fun waterSourcesDao(): WaterSourcesDao
}