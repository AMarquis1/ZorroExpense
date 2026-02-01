package com.marquis.zorroexpense.domain.repository

import com.marquis.zorroexpense.domain.error.AuthError
import com.marquis.zorroexpense.domain.model.AuthUser
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for authentication operations.
 * Abstracts Firebase Auth implementation details.
 */
interface AuthRepository {
    /**
     * Reactive flow of the current user. Emits null when unauthenticated.
     */
    val currentUser: Flow<AuthUser?>

    /**
     * Sign up a new user with email and password.
     * Automatically creates user profile in Firestore.
     */
    suspend fun signUp(
        email: String,
        password: String,
        displayName: String
    ): Result<AuthUser>

    /**
     * Sign in an existing user with email and password.
     */
    suspend fun signIn(
        email: String,
        password: String
    ): Result<AuthUser>

    /**
     * Sign out the current user.
     */
    suspend fun signOut(): Result<Unit>

    /**
     * Get the current user one-time (not reactive).
     * Returns null if no user is authenticated.
     */
    suspend fun getCurrentUser(): Result<AuthUser?>

    /**
     * Check if a user is currently authenticated.
     */
    suspend fun isAuthenticated(): Boolean
}
