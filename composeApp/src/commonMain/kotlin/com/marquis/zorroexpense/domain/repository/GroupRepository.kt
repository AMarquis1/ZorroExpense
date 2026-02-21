package com.marquis.zorroexpense.domain.repository

import com.marquis.zorroexpense.domain.model.Category
import com.marquis.zorroexpense.domain.model.Group

/**
 * Repository interface for expense list operations.
 */
interface GroupRepository {
    /**
     * Force refresh all expense lists for a user, bypassing cache
     * Always fetches from remote data source
     */
    suspend fun refreshUserGroups(userId: String): Result<List<Group>>

    /**
     * Get all expense lists for a user (lists they own or are members of)
     */
    suspend fun getUserGroups(userId: String): Result<List<Group>>

    /**
     * Get a specific expense list by ID
     */
    suspend fun getGroup(listId: String): Result<Group?>

    /**
     * Create a new expense list
     */
    suspend fun createGroup(list: Group): Result<String>

    /**
     * Update an expense list
     */
    suspend fun updateGroup(
        listId: String,
        list: Group,
    ): Result<Unit>

    /**
     * Delete an expense list
     */
    suspend fun deleteGroup(listId: String): Result<Unit>

    /**
     * Join a list using share code
     */
    suspend fun joinGroup(
        userId: String,
        shareCode: String,
    ): Result<Group>

    /**
     * Remove a member from a list
     */
    suspend fun removeMemberFromGroup(
        listId: String,
        userId: String,
    ): Result<Unit>

    suspend fun createCategory(
        groupId: String,
        category: Category
    ): Result<String>

    suspend fun updateCategory(
        groupId: String,
        category: Category
    ): Result<Unit>

    suspend fun deleteCategory(
        groupId: String,
        categoryId: String
    ): Result<Unit>
}
