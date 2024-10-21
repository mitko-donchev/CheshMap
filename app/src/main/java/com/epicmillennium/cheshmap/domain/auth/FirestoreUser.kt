package com.epicmillennium.cheshmap.domain.auth

import androidx.annotation.Keep
import kotlinx.serialization.Serializable

@Keep
@Serializable
data class FirestoreUser(
    @Keep val uid: String = "",
    @Keep val liked: List<String> = emptyList(),
    @Keep val disliked: List<String> = emptyList(),
)