package com.marquis.zorroexpense.domain.repository

import com.marquis.zorroexpense.domain.model.Expense
import com.marquis.zorroexpense.domain.model.ExpensePage

/**
 * Repository interface for expense data operations
 * Follows Clean Architecture principles with proper abstraction
 */
interface ExpenseRepository {
    suspend fun getExpensePage(
        listId: String,
        cursor: String?,
        pageSize: Int,
    ): Result<ExpensePage>

    /**
     * Force refresh expenses for a specific list, bypassing cache
     * Always fetches from remote data source and updates cache
     */
    suspend fun refreshExpensesByListId(listId: String): Result<List<Expense>>

    /**
     * Get expenses for a specific expense list
     */
    suspend fun getExpensesByListId(listId: String): Result<List<Expense>>

    /**
     * Get a single expense by ID with full reference resolution
     */
    suspend fun getExpenseById(
        listId: String,
        expenseId: String,
    ): Result<Expense?>

    /**
     * Add a new expense to a specific expense list
     */
    suspend fun addExpense(
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
