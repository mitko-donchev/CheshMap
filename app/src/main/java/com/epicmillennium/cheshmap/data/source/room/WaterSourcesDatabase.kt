package com.epicmillennium.cheshmap.data.source.room

import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.epicmillennium.cheshmap.data.source.room.converter.StringListConverter
import com.epicmillennium.cheshmap.data.source.room.dao.WaterSourcesDao
import com.epicmillennium.cheshmap.data.source.room.entity.WaterSourceEntity

@TypeConverters(StringListConverter::class)
@Database(
    version = 3,
    entities = [WaterSourceEntity::class],
    autoMigrations = [
        AutoMigration (from = 1, to = 2),
        AutoMigration (from = 2, to = 3)
    ]
)
abstract class WaterSourcesDatabase : RoomDatabase() {
    abstract fun waterSourcesDao(): WaterSourcesDao
}