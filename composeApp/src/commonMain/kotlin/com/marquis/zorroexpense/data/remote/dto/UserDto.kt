package com.marquis.zorroexpense.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UserDto(
    val documentId: String = "", // Firestore document ID - not stored in document, set when fetching
    @SerialName("name")
    val name: String = "",
    @SerialName("profileImage")
    val profileImage: String = ""
)