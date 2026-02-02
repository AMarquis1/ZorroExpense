package com.marquis.zorroexpense.data.remote.dto

import com.marquis.zorroexpense.domain.model.UserProfile
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * DTO for user profile stored in Firestore.
 * Maps between Firestore documents and domain model.
 */
@Serializable
data class UserProfileDto(
    @SerialName("userId")
    val userId: String = "",
    @SerialName("email")
    val email: String = "",
    @SerialName("name")
    val name: String = "",
    @SerialName("profileImage")
    val profileImage: String? = null,
    @SerialName("createdAt")
    val createdAt: String = "",
)

fun UserProfileDto.toDomain(): UserProfile =
    UserProfile(
        userId = userId,
        email = email,
        name = name,
        profileImage = profileImage,
        createdAt = createdAt,
    )

fun UserProfile.toDto(): UserProfileDto =
    UserProfileDto(
        userId = userId,
        email = email,
        name = name,
        profileImage = profileImage,
        createdAt = createdAt,
    )
