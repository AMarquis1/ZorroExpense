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
    private val firestoreService: FirestoreService,
) : ExpenseRemoteDataSource {
    override suspend fun getExpenses(userId: String): Result<List<Expense>> =
        try {
            if (AppConfig.USE_MOCK_DATA) {
                MockExpenseData.getMockExpenses()
            } else {
                firestoreService
                    .getExpenses(userId)
                    .mapCatching { expenseDtos ->
                        expenseDtos.map { dto ->
                            dto.toDomain(firestoreService)
                        }
                    }
            }
        } catch (e: Exception) {
            Result.failure(e)
        }

    override suspend fun addExpense(userId: String, expense: Expense): Result<Unit> =
        try {
            if (AppConfig.USE_MOCK_DATA) {
                // Mock implementation - just return success
                Result.success(Unit)
            } else {
                // Convert domain expense to DTO and save to Firestore
                val expenseDto = expense.toDto()
                firestoreService.addExpense(userId, expenseDto)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }

    override suspend fun updateExpense(userId: String, expense: Expense): Result<Unit> =
        try {
            if (AppConfig.USE_MOCK_DATA) {
                // Mock implementation - just return success
                Result.success(Unit)
            } else {
                // Convert domain expense to DTO and update in Firestore
                val expenseDto = expense.toDto()
                firestoreService.updateExpense(userId, expense.documentId, expenseDto)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }

    override suspend fun deleteExpense(userId: String, expenseId: String): Result<Unit> =
        try {
            if (AppConfig.USE_MOCK_DATA) {
                // Mock implementation - just return success
                Result.success(Unit)
            } else {
                // Delete from Firestore
                firestoreService.deleteExpense(userId, expenseId)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }

    override suspend fun getExpensesByListId(listId: String): Result<List<Expense>> =
        try {
            if (AppConfig.USE_MOCK_DATA) {
                // Mock implementation - return expenses filtered by listId
                MockExpenseData.getMockExpenses().getOrDefault(emptyList()).filter { it.listId == listId }
                    .let { Result.success(it) }
            } else {
                firestoreService
                    .getExpensesByListId(listId)
                    .mapCatching { expenseDtos ->
                        expenseDtos.map { dto ->
                            dto.toDomain(firestoreService)
                        }
                    }
            }
        } catch (e: Exception) {
            Result.failure(e)
        }

    override suspend fun addExpenseToList(listId: String, expense: Expense): Result<String> =
        try {
            if (AppConfig.USE_MOCK_DATA) {
                // Mock implementation - return a generated ID
                Result.success("mock-expense-${expense.documentId}")
            } else {
                val expenseDto = expense.toDto()
                firestoreService.addExpenseToList(listId, expenseDto)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }

    override suspend fun updateExpenseInList(listId: String, expense: Expense): Result<Unit> =
        try {
            if (AppConfig.USE_MOCK_DATA) {
                // Mock implementation - just return success
                Result.success(Unit)
            } else {
                val expenseDto = expense.toDto()
                firestoreService.updateExpenseInList(listId, expense.documentId, expenseDto)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }

    override suspend fun deleteExpenseFromList(listId: String, expenseId: String): Result<Unit> =
        try {
            if (AppConfig.USE_MOCK_DATA) {
                // Mock implementation - just return success
                Result.success(Unit)
            } else {
                firestoreService.deleteExpenseFromList(listId, expenseId)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
}
