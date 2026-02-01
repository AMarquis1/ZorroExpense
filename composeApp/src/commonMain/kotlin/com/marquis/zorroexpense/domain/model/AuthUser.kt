package com.marquis.zorroexpense.domain.model

/**
 * Represents an authenticated user in the application.
 * This is the domain model for authentication state.
 */
data class AuthUser(
    val userId: String,
    val email: String,
    val displayName: String? = null,
    val isEmailVerified: Boolean = false
)
