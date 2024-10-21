package com.epicmillennium.cheshmap.domain.auth

import kotlinx.coroutines.flow.Flow

interface UserRepository {
    suspend fun insertUser(user: User)
    suspend fun getUser(): Flow<User?>
    suspend fun deleteUser()
}