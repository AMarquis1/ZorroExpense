package com.marquis.zorroexpense.data.datasource

import com.marquis.zorroexpense.AppConfig
import com.marquis.zorroexpense.MockExpenseData
import com.marquis.zorroexpense.data.remote.FirestoreService
import com.marquis.zorroexpense.data.remote.dto.toDomain
import com.marquis.zorroexpense.domain.model.Expense

/**
 * Remote data source implementation for expenses
 * Handles network-based data operations
 */
class ExpenseRemoteDataSourceImpl(
    private val firestoreService: FirestoreService
) : ExpenseRemoteDataSource {

    override suspend fun getExpenses(): Result<List<Expense>> {
        return try {
            if (AppConfig.USE_MOCK_DATA) {
                MockExpenseData.getMockExpenses()
            } else {
                firestoreService.getExpenses()
                    .mapCatching { expenseDtos ->
                        expenseDtos.map { dto ->
                            dto.toDomain(firestoreService)
                        }
                    }
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun addExpense(expense: Expense): Result<Unit> {
        return try {
            if (AppConfig.USE_MOCK_DATA) {
                // Mock implementation - in real app would call firestoreService.addExpense()
                Result.success(Unit)
            } else {
                // TODO: Implement when FirestoreService.addExpense() is available
                Result.failure(NotImplementedError("Remote addExpense not yet implemented"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateExpense(expense: Expense): Result<Unit> {
        return try {
            if (AppConfig.USE_MOCK_DATA) {
                // Mock implementation - in real app would call firestoreService.updateExpense()
                Result.success(Unit)
            } else {
                // TODO: Implement when FirestoreService.updateExpense() is available
                Result.failure(NotImplementedError("Remote updateExpense not yet implemented"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteExpense(expenseId: String): Result<Unit> {
        return try {
            if (AppConfig.USE_MOCK_DATA) {
                // Mock implementation - in real app would call firestoreService.deleteExpense()
                Result.success(Unit)
            } else {
                // TODO: Implement when FirestoreService.deleteExpense() is available
                Result.failure(NotImplementedError("Remote deleteExpense not yet implemented"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}