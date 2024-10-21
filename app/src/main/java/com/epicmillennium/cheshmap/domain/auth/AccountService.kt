package com.epicmillennium.cheshmap.domain.auth

import kotlinx.coroutines.flow.Flow

interface AccountService {
    val hasUser: Boolean
    val currentUserId: String

    val currentUser: Flow<User>

    suspend fun signInWithEmail(email: String, password: String)
    suspend fun signInWithGoogle(idToken: String)

    suspend fun signUpWithEmail(email: String, password: String)

    suspend fun sendRecoveryEmail(email: String)
    suspend fun deleteAccount()
    suspend fun signOut()
}