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
}
