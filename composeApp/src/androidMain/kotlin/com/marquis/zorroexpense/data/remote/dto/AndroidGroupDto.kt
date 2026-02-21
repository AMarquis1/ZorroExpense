package com.marquis.zorroexpense.data.remote.dto

import com.marquis.zorroexpense.domain.model.User
import dev.gitlive.firebase.firestore.DocumentReference
import dev.gitlive.firebase.firestore.Timestamp
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AndroidGroupDto(
    @SerialName("listId")
    override val groupId: String = "",
    @SerialName("name")
    override val name: String = "",
    @SerialName("createdBy")
    override val createdBy: String = "",
    @SerialName("members")
    val memberRefs: List<DocumentReference> = emptyList(),
    @SerialName("shareCode")
    override val shareCode: String = "",
    @SerialName("createdAt")
    override val createdAt: String = "",
    @SerialName("lastModified")
    override val lastModified: Timestamp,
) : GroupDto {
    override val members: List<Any> get() = memberRefs
}

actual fun List<Any>.getMemberIds(): List<String> =
    filterIsInstance<DocumentReference>().map { ref ->
        ref.path
    }

actual fun List<Any>.getMemberUsers(): List<User> =
    filterIsInstance<DocumentReference>().map { ref ->
        // Extract user ID from the reference path (e.g., "users/userId")
        val userId = ref.path.substringAfterLast("/")
        User(userId = userId, name = "", profileImage = "")
    }
