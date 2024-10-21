package com.epicmillennium.cheshmap.domain.usecase.auth

import com.epicmillennium.cheshmap.domain.auth.AccountService

class SignInWithGoogleUseCase(private val accountService: AccountService) {
    suspend operator fun invoke(idToken: String): Result<Boolean> =
        Result.runCatching {
            accountService.signInWithGoogle(idToken)
            true
        }
}