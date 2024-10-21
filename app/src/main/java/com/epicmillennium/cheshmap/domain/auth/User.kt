package com.epicmillennium.cheshmap.domain.auth

data class User(
    val uid: String = "",
    val documentId: String = "",
    val likedSourcesIds: List<String> = listOf(),
    val dislikedSourcesIds: List<String> = listOf(),
) {
    companion object {
        fun fromFirestoreUserButCouldBeNull(firestoreUser: FirestoreUser?): User? {
            firestoreUser ?: return null

            return User(
                uid = firestoreUser.uid,
                documentId = "",
                likedSourcesIds = firestoreUser.liked,
                dislikedSourcesIds = firestoreUser.disliked,
            )
        }
    }
}