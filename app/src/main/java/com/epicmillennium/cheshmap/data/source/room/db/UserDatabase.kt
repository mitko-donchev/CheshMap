package com.epicmillennium.cheshmap.data.source.room.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.epicmillennium.cheshmap.data.source.room.converter.StringListConverter
import com.epicmillennium.cheshmap.data.source.room.dao.UserDao
import com.epicmillennium.cheshmap.data.source.room.entity.UserEntity

@TypeConverters(StringListConverter::class)
@Database(
    version = 1,
    entities = [UserEntity::class]
)
abstract class UserDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
}