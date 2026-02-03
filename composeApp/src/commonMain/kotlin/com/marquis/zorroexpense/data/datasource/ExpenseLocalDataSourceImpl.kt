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
    private fun getCacheKey(userId: String): String = "expenses_$userId"

//    override suspend fun getExpenses(userId: String): Result<List<Expense>> =
//        try {
//            val cachedExpenses = cacheManager.get(getCacheKey(userId))
//            if (cachedExpenses != null) {
//                Result.success(cachedExpenses)
//            } else {
//                Result.success(emptyList()) // No cached data available
//            }
//        } catch (e: Exception) {
//            Result.failure(e)
//        }

    override suspend fun cacheExpenses(expenses: List<Expense>) {
        // Note: This method doesn't have userId, so it's deprecated
        // Use cacheExpensesForUser instead
        try {
            // Keep old behavior for backward compatibility with mock data
            cacheManager.put("all_expenses", expenses)
        } catch (e: Exception) {
            println("Failed to cache expenses: ${e.message}")
        }
    }
//
//    suspend fun cacheExpensesForUser(userId: String, expenses: List<Expense>) {
//        try {
//            cacheManager.put(getCacheKey(userId), expenses)
//        } catch (e: Exception) {
//            println("Failed to cache expenses for user $userId: ${e.message}")
//        }
//    }
//
//    override suspend fun addExpense(userId: String, expense: Expense): Result<Unit> =
//        try {
//            // Get current cached expenses for this user
//            val currentExpenses = cacheManager.get(getCacheKey(userId)) ?: emptyList()
//
//            // Add new expense to the list
//            val updatedExpenses = currentExpenses + expense
//
//            // Update cache
//            cacheManager.put(getCacheKey(userId), updatedExpenses)
//
//            Result.success(Unit)
//        } catch (e: Exception) {
//            Result.failure(e)
//        }
//
//    override suspend fun updateExpense(userId: String, expense: Expense): Result<Unit> =
//        try {
//            // Get current cached expenses for this user
//            val currentExpenses = cacheManager.get(getCacheKey(userId)) ?: emptyList()
//
//            // Update the expense in the list
//            val updatedExpenses =
//                currentExpenses.map { existingExpense ->
//                    if (existingExpense.documentId == expense.documentId) {
//                        expense
//                    } else {
//                        existingExpense
//                    }
//                }
//
//            // Update cache
//            cacheManager.put(getCacheKey(userId), updatedExpenses)
//
//            Result.success(Unit)
//        } catch (e: Exception) {
//            Result.failure(e)
//        }
//
//    override suspend fun deleteExpense(userId: String, expenseId: String): Result<Unit> =
//        try {
//            // Get current cached expenses for this user
//            val currentExpenses = cacheManager.get(getCacheKey(userId)) ?: emptyList()
//
//            // Remove expense from the list
//            val updatedExpenses =
//                currentExpenses.filter { expense ->
//                    expense.documentId != expenseId
//                }
//
//            // Update cache
//            cacheManager.put(getCacheKey(userId), updatedExpenses)
//
//            Result.success(Unit)
//        } catch (e: Exception) {
//            Result.failure(e)
//        }

    override suspend fun clearAll() {
        try {
            cacheManager.clear()
        } catch (e: Exception) {
            // Log error but don't fail the operation
            println("Failed to clear cache: ${e.message}")
        }
    }

    private fun getListCacheKey(listId: String): String = "expenses_list_$listId"

    override suspend fun getExpensesByListId(listId: String): Result<List<Expense>> =
        try {
            val cachedExpenses = cacheManager.get(getListCacheKey(listId))
            if (cachedExpenses != null) {
                Result.success(cachedExpenses)
            } else {
                Result.success(emptyList())
            }
        } catch (e: Exception) {
            Result.failure(e)
        }

    override suspend fun getExpenseById(
        listId: String,
        expenseId: String,
    ): Result<Expense?> =
        try {
            val cachedExpenses = cacheManager.get(getListCacheKey(listId))
            val expense = cachedExpenses?.find { it.documentId == expenseId }
            Result.success(expense)
        } catch (e: Exception) {
            Result.failure(e)
        }

    override suspend fun addExpenseToList(
        listId: String,
        expense: Expense,
    ): Result<String> =
        try {
            val currentExpenses = cacheManager.get(getListCacheKey(listId)) ?: emptyList()
            val updatedExpenses = currentExpenses + expense
            cacheManager.put(getListCacheKey(listId), updatedExpenses)
            Result.success(expense.documentId)
        } catch (e: Exception) {
            Result.failure(e)
        }

    override suspend fun updateExpenseInList(
        listId: String,
        expense: Expense,
    ): Result<Unit> =
        try {
            val currentExpenses = cacheManager.get(getListCacheKey(listId)) ?: emptyList()
            val updatedExpenses =
                currentExpenses.map { existingExpense ->
                    if (existingExpense.documentId == expense.documentId) {
                        expense
                    } else {
                        existingExpense
                    }
                }
            cacheManager.put(getListCacheKey(listId), updatedExpenses)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }

    override suspend fun deleteExpenseFromList(
        listId: String,
        expenseId: String,
    ): Result<Unit> =
        try {
            val currentExpenses = cacheManager.get(getListCacheKey(listId)) ?: emptyList()
            val updatedExpenses =
                currentExpenses.filter { expense ->
                    expense.documentId != expenseId
                }
            cacheManager.put(getListCacheKey(listId), updatedExpenses)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
}
