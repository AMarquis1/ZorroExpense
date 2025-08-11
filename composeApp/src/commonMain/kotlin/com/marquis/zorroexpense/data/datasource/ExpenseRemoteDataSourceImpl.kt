package com.marquis.zorroexpense.data.datasource

import com.marquis.zorroexpense.AppConfig
import com.marquis.zorroexpense.MockExpenseData
import com.marquis.zorroexpense.data.remote.FirestoreService
import com.marquis.zorroexpense.data.remote.dto.toDomain
import com.marquis.zorroexpense.data.remote.dto.toDto
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
                // Mock implementation - just return success
                Result.success(Unit)
            } else {
                // Convert domain expense to DTO and save to Firestore
                val expenseDto = expense.toDto()
                firestoreService.addExpense(expenseDto)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateExpense(expense: Expense): Result<Unit> {
        return try {
            if (AppConfig.USE_MOCK_DATA) {
                // Mock implementation - just return success
                Result.success(Unit)
            } else {
                // Convert domain expense to DTO and update in Firestore
                val expenseDto = expense.toDto()
                // Note: For update we would need expense ID, this is a simplified implementation
                Result.failure(NotImplementedError("Update requires expense ID - to be implemented with proper ID handling"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteExpense(expenseId: String): Result<Unit> {
        return try {
            if (AppConfig.USE_MOCK_DATA) {
                // Mock implementation - just return success
                Result.success(Unit)
            } else {
                // Delete from Firestore
                firestoreService.deleteExpense(expenseId)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}