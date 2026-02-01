package com.marquis.zorroexpense.domain.repository

import com.marquis.zorroexpense.domain.model.Expense

/**
 * Repository interface for expense data operations
 * Follows Clean Architecture principles with proper abstraction
 */
interface ExpenseRepository {
    /**
     * Get expenses with automatic cache handling
     * Uses cache-first strategy with fallback to network
     */
    suspend fun getExpenses(userId: String): Result<List<Expense>>

    /**
     * Force refresh expenses, bypassing cache
     * Always fetches from remote data source
     */
    suspend fun refreshExpenses(userId: String): Result<List<Expense>>

    /**
     * Add a new expense
     * Invalidates relevant cache entries
     */
    suspend fun addExpense(userId: String, expense: Expense): Result<Unit>

    /**
     * Update an existing expense
     * Invalidates relevant cache entries
     */
    suspend fun updateExpense(userId: String, expense: Expense): Result<Unit>

    /**
     * Delete an expense by ID
     * Invalidates relevant cache entries
     */
    suspend fun deleteExpense(userId: String, expenseId: String): Result<Unit>

    /**
     * Get expenses for a specific expense list
     */
    suspend fun getExpensesByListId(listId: String): Result<List<Expense>>

    /**
     * Clear all cached data
     * Useful for logout or data reset scenarios
     */
    suspend fun clearCache()
}
