package com.epicmillennium.cheshmap.data.repository.user

import com.epicmillennium.cheshmap.domain.auth.User
import kotlinx.coroutines.flow.Flow

interface UserTable {
    suspend fun insertUser(user: User)
    suspend fun getUser(): Flow<User?>
    suspend fun deleteUser()
}