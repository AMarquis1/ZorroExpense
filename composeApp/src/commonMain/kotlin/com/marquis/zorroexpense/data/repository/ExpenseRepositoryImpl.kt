package com.marquis.zorroexpense.data.repository

import com.marquis.zorroexpense.AppConfig
import com.marquis.zorroexpense.MockExpenseData
import com.marquis.zorroexpense.data.remote.FirestoreService
import com.marquis.zorroexpense.data.remote.dto.toDomain
import com.marquis.zorroexpense.domain.model.Expense
import com.marquis.zorroexpense.domain.repository.ExpenseRepository

class ExpenseRepositoryImpl(
    private val firestoreService: FirestoreService
) : ExpenseRepository {

    override suspend fun getExpenses(): Result<List<Expense>> {
        return try {
            if (AppConfig.USE_MOCK_DATA) {
                MockExpenseData.getMockExpenses()
            } else {
                firestoreService.getExpenses()
                    .mapCatching { expenseDtos ->
                        expenseDtos.map { expenseDto ->
                            expenseDto.toDomain(firestoreService)
                        }
                    }
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun addExpense(expense: Expense): Result<Unit> {
        // TODO: Implement addExpense in FirestoreService
        return Result.failure(NotImplementedError("addExpense not yet implemented"))
    }

    override suspend fun updateExpense(expense: Expense): Result<Unit> {
        // TODO: Implement updateExpense in FirestoreService
        return Result.failure(NotImplementedError("updateExpense not yet implemented"))
    }

    override suspend fun deleteExpense(expenseId: String): Result<Unit> {
        // TODO: Implement deleteExpense in FirestoreService
        return Result.failure(NotImplementedError("deleteExpense not yet implemented"))
    }
}