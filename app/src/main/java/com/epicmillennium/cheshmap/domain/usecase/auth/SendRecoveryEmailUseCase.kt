package com.epicmillennium.cheshmap.domain.usecase.auth

import com.epicmillennium.cheshmap.domain.auth.AccountService

class SendRecoveryEmailUseCase(private val accountService: AccountService) {
    suspend operator fun invoke(email: String): Result<Boolean> = Result.runCatching {
        accountService.sendRecoveryEmail(email)
        true
    }
}