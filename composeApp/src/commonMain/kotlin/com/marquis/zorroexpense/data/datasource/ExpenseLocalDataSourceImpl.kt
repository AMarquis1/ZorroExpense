package com.marquis.zorroexpense.data.datasource

import com.marquis.zorroexpense.domain.cache.CacheManager
import com.marquis.zorroexpense.domain.model.Expense

/**
 * Local data source implementation for expenses
 * Uses cache manager for local storage operations
 */
class ExpenseLocalDataSourceImpl(
    private val cacheManager: CacheManager<String, List<Expense>>,
) : ExpenseLocalDataSource {
    companion object {
        private const val EXPENSES_CACHE_KEY = "all_expenses"
    }

    override suspend fun getExpenses(): Result<List<Expense>> =
        try {
            val cachedExpenses = cacheManager.get(EXPENSES_CACHE_KEY)
            if (cachedExpenses != null) {
                Result.success(cachedExpenses)
            } else {
                Result.success(emptyList()) // No cached data available
            }
        } catch (e: Exception) {
            Result.failure(e)
        }

    override suspend fun cacheExpenses(expenses: List<Expense>) {
        try {
            cacheManager.put(EXPENSES_CACHE_KEY, expenses)
        } catch (e: Exception) {
            // Log error but don't fail the operation
            // In a real app, you might want to use a proper logging framework
            println("Failed to cache expenses: ${e.message}")
        }
    }

    override suspend fun addExpense(expense: Expense): Result<Unit> =
        try {
            // Get current cached expenses
            val currentExpenses = cacheManager.get(EXPENSES_CACHE_KEY) ?: emptyList()

            // Add new expense to the list
            val updatedExpenses = currentExpenses + expense

            // Update cache
            cacheManager.put(EXPENSES_CACHE_KEY, updatedExpenses)

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }

    override suspend fun updateExpense(expense: Expense): Result<Unit> =
        try {
            // Get current cached expenses
            val currentExpenses = cacheManager.get(EXPENSES_CACHE_KEY) ?: emptyList()

            // Update the expense in the list
            val updatedExpenses =
                currentExpenses.map { existingExpense ->
                    if (existingExpense.name == expense.name) { // Using name as identifier for now
                        expense
                    } else {
                        existingExpense
                    }
                }

            // Update cache
            cacheManager.put(EXPENSES_CACHE_KEY, updatedExpenses)

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }

    override suspend fun deleteExpense(expenseId: String): Result<Unit> =
        try {
            // Get current cached expenses
            val currentExpenses = cacheManager.get(EXPENSES_CACHE_KEY) ?: emptyList()

            // Remove expense from the list
            val updatedExpenses =
                currentExpenses.filter { expense ->
                    expense.name != expenseId // Using name as identifier for now
                }

            // Update cache
            cacheManager.put(EXPENSES_CACHE_KEY, updatedExpenses)

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }

    override suspend fun clearAll() {
        try {
            cacheManager.clear()
        } catch (e: Exception) {
            // Log error but don't fail the operation
            println("Failed to clear cache: ${e.message}")
        }
    }
}
