package com.epicmillennium.cheshmap.domain.usecase.auth

import com.epicmillennium.cheshmap.domain.auth.AccountService

class SignUpWithEmailUseCase(private val accountService: AccountService) {
    suspend operator fun invoke(email: String, password: String): Result<Boolean> =
        Result.runCatching {
            accountService.signUpWithEmail(email, password)
            true
        }
}