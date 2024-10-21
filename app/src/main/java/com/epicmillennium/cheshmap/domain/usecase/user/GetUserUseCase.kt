package com.epicmillennium.cheshmap.domain.usecase.user

import com.epicmillennium.cheshmap.domain.auth.User
import com.epicmillennium.cheshmap.domain.auth.UserRepository
import kotlinx.coroutines.flow.Flow

class GetUserUseCase(private val userRepository: UserRepository) {
    suspend operator fun invoke(): Result<Flow<User?>> = Result.runCatching {
        userRepository.getUser()
    }
}