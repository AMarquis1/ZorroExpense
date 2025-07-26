package com.marquis.zorroexpense.domain.repository

import com.marquis.zorroexpense.domain.model.Expense

interface ExpenseRepository {
    suspend fun getExpenses(): Result<List<Expense>>
    suspend fun addExpense(expense: Expense): Result<Unit>
    suspend fun updateExpense(expense: Expense): Result<Unit>
    suspend fun deleteExpense(expenseId: String): Result<Unit>
}