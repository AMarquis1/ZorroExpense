package com.marquis.zorroexpense.domain.repository

import com.marquis.zorroexpense.domain.model.Expense

/**
 * Repository interface for expense data operations
 * Follows Clean Architecture principles with proper abstraction
 */
interface ExpenseRepository {
    /**
     * Force refresh expenses, bypassing cache
     * Always fetches from remote data source
     */
//    suspend fun refreshExpenses(userId: String): Result<List<Expense>>

    /**
     * Get expenses for a specific expense list
     */
    suspend fun getExpensesByListId(listId: String): Result<List<Expense>>

    /**
     * Add a new expense to a specific expense list
     */
    suspend fun addExpenseToList(
        listId: String,
        expense: Expense,
    ): Result<String>

    /**
     * Update an existing expense in a specific expense list
     */
    suspend fun updateExpenseInList(
        listId: String,
        expense: Expense,
    ): Result<Unit>

    /**
     * Delete an expense from a specific expense list
     */
    suspend fun deleteExpenseFromList(
        listId: String,
        expenseId: String,
    ): Result<Unit>

    /**
     * Clear all cached data
     * Useful for logout or data reset scenarios
     */
    suspend fun clearCache()
}
