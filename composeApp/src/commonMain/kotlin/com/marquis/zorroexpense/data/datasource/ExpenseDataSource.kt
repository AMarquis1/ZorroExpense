package com.marquis.zorroexpense.data.datasource

import com.marquis.zorroexpense.domain.model.Expense

/**
 * Abstract data source for expense operations
 * Provides clean separation between local and remote data sources
 */
interface ExpenseDataSource {
    suspend fun getExpenses(): Result<List<Expense>>
    suspend fun addExpense(expense: Expense): Result<Unit>
    suspend fun updateExpense(expense: Expense): Result<Unit>
    suspend fun deleteExpense(expenseId: String): Result<Unit>
}

/**
 * Remote data source interface
 * Represents network-based data access
 */
interface ExpenseRemoteDataSource : ExpenseDataSource

/**
 * Local data source interface  
 * Represents local storage or cache-based data access
 */
interface ExpenseLocalDataSource : ExpenseDataSource {
    suspend fun clearAll()
    suspend fun cacheExpenses(expenses: List<Expense>)
}