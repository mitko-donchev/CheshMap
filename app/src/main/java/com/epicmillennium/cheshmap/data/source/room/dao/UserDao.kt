package com.epicmillennium.cheshmap.data.source.room.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.epicmillennium.cheshmap.data.source.room.entity.UserEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertUser(user: UserEntity)

    @Query("SELECT * FROM users")
    fun getUser(): Flow<UserEntity?>

    @Query("DELETE FROM users")
    fun deleteUser()
}