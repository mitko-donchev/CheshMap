package com.epicmillennium.cheshmap.domain.usecase.user

import com.epicmillennium.cheshmap.domain.auth.UserRepository

class DeleteUserUseCase(private val userRepository: UserRepository) {
    suspend operator fun invoke(): Result<Boolean> = Result.runCatching {
        userRepository.deleteUser()
        true
    }
}