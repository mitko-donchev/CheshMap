package com.epicmillennium.cheshmap.data.source.room.entity

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.epicmillennium.cheshmap.domain.auth.User
import kotlinx.parcelize.Parcelize

@Parcelize
@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey(autoGenerate = false)
    val uid: String,
    val documentId: String,
    val likedSourcesIds: List<String>,
    val dislikedSourcesIds: List<String>
) : Parcelable {
    companion object {
        fun fromUser(user: User): UserEntity = UserEntity(
            uid = user.uid,
            documentId = user.documentId,
            likedSourcesIds = user.likedSourcesIds,
            dislikedSourcesIds = user.dislikedSourcesIds
        )

        fun toUser(userEntity: UserEntity): User = User(
            uid = userEntity.uid,
            documentId = userEntity.documentId,
            likedSourcesIds = userEntity.likedSourcesIds,
            dislikedSourcesIds = userEntity.dislikedSourcesIds
        )
    }
}