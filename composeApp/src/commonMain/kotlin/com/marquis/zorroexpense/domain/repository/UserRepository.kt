package com.marquis.zorroexpense.domain.repository

import com.marquis.zorroexpense.domain.model.User

/**
 * Repository interface for user-related operations.
 * Abstracts user data access and retrieval.
 */
interface UserRepository {
    /**
     * Get a single user by ID.
     */
    suspend fun getUserById(userId: String): Result<User?>

    /**
     * Get multiple users by their IDs.
     */
    suspend fun getUsersByIds(userIds: List<String>): Result<List<User>>

    /**
     * Update a user's profile information.
     *
     * @param userId The ID of the user
     * @param name The new name for the user
     * @param profileImageUrl The new profile image URL (optional)
     */
    suspend fun updateProfile(userId: String, name: String, profileImageUrl: String?): Result<Unit>
}
