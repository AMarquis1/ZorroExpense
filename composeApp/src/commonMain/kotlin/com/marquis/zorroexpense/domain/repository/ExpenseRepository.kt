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
    suspend fun getExpenses(): Result<List<Expense>>

    /**
     * Force refresh expenses, bypassing cache
     * Always fetches from remote data source
     */
    suspend fun refreshExpenses(): Result<List<Expense>>

    /**
     * Add a new expense
     * Invalidates relevant cache entries
     */
    suspend fun addExpense(expense: Expense): Result<Unit>

    /**
     * Update an existing expense
     * Invalidates relevant cache entries
     */
    suspend fun updateExpense(expense: Expense): Result<Unit>

    /**
     * Delete an expense by ID
     * Invalidates relevant cache entries
     */
    suspend fun deleteExpense(expenseId: String): Result<Unit>

    /**
     * Clear all cached data
     * Useful for logout or data reset scenarios
     */
    suspend fun clearCache()
}
