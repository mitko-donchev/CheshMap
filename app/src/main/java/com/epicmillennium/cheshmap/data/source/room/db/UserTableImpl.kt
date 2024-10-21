package com.epicmillennium.cheshmap.data.source.room.db

import com.epicmillennium.cheshmap.data.repository.user.UserTable
import com.epicmillennium.cheshmap.data.source.room.entity.UserEntity
import com.epicmillennium.cheshmap.domain.auth.User
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class UserTableImpl(
    private val userDatabase: UserDatabase
) : UserTable {
    override suspend fun insertUser(user: User) {
        userDatabase.userDao().insertUser(UserEntity.fromUser(user))
    }

    override suspend fun getUser(): Flow<User?> {
        return userDatabase.userDao().getUser().map {
            if (it == null) null else UserEntity.toUser(it)
        }
    }

    override suspend fun deleteUser() = userDatabase.userDao().deleteUser()
}