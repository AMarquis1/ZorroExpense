package com.marquis.zorroexpense.data.remote.dto

import com.marquis.zorroexpense.domain.model.AuthUser
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * DTO for authentication user from Firebase Auth.
 * Maps between Firebase Auth and domain model.
 */
@Serializable
data class AuthUserDto(
    @SerialName("uid")
    val userId: String = "",
    @SerialName("email")
    val email: String = "",
    @SerialName("displayName")
    val displayName: String? = null,
    @SerialName("emailVerified")
    val isEmailVerified: Boolean = false
)

fun AuthUserDto.toDomain(): AuthUser = AuthUser(
    userId = userId,
    email = email,
    displayName = displayName,
    isEmailVerified = isEmailVerified
)

fun AuthUser.toDto(): AuthUserDto = AuthUserDto(
    userId = userId,
    email = email,
    displayName = displayName,
    isEmailVerified = isEmailVerified
)
