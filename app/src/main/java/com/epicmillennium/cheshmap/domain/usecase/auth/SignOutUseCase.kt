package com.epicmillennium.cheshmap.domain.usecase.auth

import com.epicmillennium.cheshmap.domain.auth.AccountService

class SignOutUseCase(private val accountService: AccountService) {
    suspend operator fun invoke(): Result<Boolean> = Result.runCatching {
        accountService.signOut()
        true
    }
}