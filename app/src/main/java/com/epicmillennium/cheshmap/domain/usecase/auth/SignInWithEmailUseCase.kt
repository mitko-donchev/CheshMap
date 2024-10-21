package com.epicmillennium.cheshmap.domain.usecase.auth

import com.epicmillennium.cheshmap.domain.auth.AccountService

class SignInWithEmailUseCase(private val accountService: AccountService) {
    suspend operator fun invoke(email: String, password: String): Result<Boolean> =
        Result.runCatching {
            accountService.signInWithEmail(email, password)
            true
        }
}