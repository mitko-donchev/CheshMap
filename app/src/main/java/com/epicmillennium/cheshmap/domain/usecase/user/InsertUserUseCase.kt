package com.epicmillennium.cheshmap.domain.usecase.user

import com.epicmillennium.cheshmap.domain.auth.User
import com.epicmillennium.cheshmap.domain.auth.UserRepository

class InsertUserUseCase(private val userRepository: UserRepository) {
    suspend operator fun invoke(user: User): Result<Boolean> = Result.runCatching {
        userRepository.insertUser(user)
        true
    }
}