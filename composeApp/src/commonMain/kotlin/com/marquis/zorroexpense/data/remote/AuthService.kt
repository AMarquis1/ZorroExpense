package com.marquis.zorroexpense.data.remote

import com.marquis.zorroexpense.data.remote.dto.AuthUserDto

/**
 * Platform-specific authentication service.
 * Abstracts Firebase Auth implementation details.
 */
expect class AuthService {
    /**
     * Sign up a new user with email and password.
     * Creates user in Firebase Auth and returns user data.
     */
    suspend fun signUp(
        email: String,
        password: String,
        displayName: String
    ): Result<AuthUserDto>

    /**
     * Sign in with email and password.
     */
    suspend fun signIn(
        email: String,
        password: String
    ): Result<AuthUserDto>

    /**
     * Sign out the current user.
     */
    suspend fun signOut(): Result<Unit>

    /**
     * Get the current user without creating a new session.
     */
    suspend fun getCurrentUser(): Result<AuthUserDto?>

    /**
     * Get auth state changed flow for reactive updates.
     */
    fun getAuthStateFlow(): kotlinx.coroutines.flow.Flow<AuthUserDto?>

    /**
     * Check if current user is authenticated.
     */
    suspend fun isAuthenticated(): Boolean

    companion object {
        fun create(): AuthService
    }
}
