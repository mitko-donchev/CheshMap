package com.epicmillennium.cheshmap.domain.usecase.auth

import com.epicmillennium.cheshmap.domain.auth.AccountService

class HasUserUseCase(private val accountService: AccountService) {
    operator fun invoke(): Result<Boolean> = Result.runCatching { accountService.hasUser }
}