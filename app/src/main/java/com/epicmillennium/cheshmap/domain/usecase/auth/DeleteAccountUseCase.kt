package com.epicmillennium.cheshmap.domain.usecase.auth

import com.epicmillennium.cheshmap.domain.auth.AccountService

class DeleteAccountUseCase(private val accountService: AccountService) {
    suspend operator fun invoke(): Result<Boolean> = Result.runCatching {
        accountService.deleteAccount()
        true
    }
}