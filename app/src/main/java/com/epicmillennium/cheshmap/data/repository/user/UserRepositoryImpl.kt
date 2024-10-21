package com.epicmillennium.cheshmap.data.repository.user

import com.epicmillennium.cheshmap.domain.auth.User
import com.epicmillennium.cheshmap.domain.auth.UserRepository
import kotlinx.coroutines.flow.Flow

class UserRepositoryImpl(
    private val userTable: UserTable
) : UserRepository {
    override suspend fun insertUser(user: User) = userTable.insertUser(user)

    override suspend fun getUser(): Flow<User?> = userTable.getUser()

    override suspend fun deleteUser() = userTable.deleteUser()
}