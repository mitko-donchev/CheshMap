package com.epicmillennium.cheshmap.domain.usecase.auth

import com.epicmillennium.cheshmap.domain.auth.AccountService
import com.epicmillennium.cheshmap.domain.auth.User
import kotlinx.coroutines.flow.Flow

class GetCurrentUserUseCase(private val accountService: AccountService) {
    operator fun invoke(): Result<Flow<User>> = Result.runCatching { accountService.currentUser }
}