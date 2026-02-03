package com.marquis.zorroexpense.domain.repository

import com.marquis.zorroexpense.domain.model.ExpenseList

/**
 * Repository interface for expense list operations.
 */
interface ExpenseListRepository {
    /**
     * Force refresh all expense lists for a user, bypassing cache
     * Always fetches from remote data source
     */
    suspend fun refreshUserExpenseLists(userId: String): Result<List<ExpenseList>>

    /**
     * Get all expense lists for a user (lists they own or are members of)
     */
    suspend fun getUserExpenseLists(userId: String): Result<List<ExpenseList>>

    /**
     * Get a specific expense list by ID
     */
    suspend fun getExpenseListById(listId: String): Result<ExpenseList?>

    /**
     * Create a new expense list
     */
    suspend fun createExpenseList(list: ExpenseList): Result<String>

    /**
     * Update an expense list
     */
    suspend fun updateExpenseList(
        listId: String,
        list: ExpenseList,
    ): Result<Unit>

    /**
     * Delete an expense list
     */
    suspend fun deleteExpenseList(listId: String): Result<Unit>

    /**
     * Join a list using share code
     */
    suspend fun joinExpenseList(
        userId: String,
        shareCode: String,
    ): Result<ExpenseList>

    /**
     * Remove a member from a list
     */
    suspend fun removeMemberFromList(
        listId: String,
        userId: String,
    ): Result<Unit>
}
