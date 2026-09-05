package com.marquis.zorroexpense.data.datasource

import com.marquis.zorroexpense.AppConfig
import com.marquis.zorroexpense.MockExpenseData
import com.marquis.zorroexpense.data.remote.FirestoreService
import com.marquis.zorroexpense.data.remote.dto.getReferencePath
import com.marquis.zorroexpense.data.remote.dto.getSplitDetailData
import com.marquis.zorroexpense.data.remote.dto.toDomain
import com.marquis.zorroexpense.data.remote.dto.toDto
import com.marquis.zorroexpense.domain.model.Category
import com.marquis.zorroexpense.domain.model.Expense
import com.marquis.zorroexpense.domain.model.ExpensePage
import com.marquis.zorroexpense.domain.model.User
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope

class ExpenseRemoteDataSourceImpl(
    private val firestoreService: FirestoreService,
) : ExpenseRemoteDataSource {
    private val categoriesByListId = mutableMapOf<String, Map<String, Category>>()
    private val usersById = mutableMapOf<String, User>()

    override suspend fun getExpensePage(
        listId: String,
        cursor: String?,
        pageSize: Int,
    ): Result<ExpensePage> =
        try {
            if (AppConfig.USE_MOCK_DATA) {
                val expenses =
                    MockExpenseData.getMockExpenses().getOrDefault(emptyList())
                        .filter { it.listId == listId }
                        .sortedByDescending { it.date }
                val startIndex = cursor?.toIntOrNull() ?: 0
                val pageExpenses = expenses.drop(startIndex).take(pageSize)
                val nextIndex = startIndex + pageExpenses.size
                Result.success(ExpensePage(pageExpenses, nextIndex.takeIf { it < expenses.size }?.toString(), nextIndex < expenses.size))
            } else {
                val page = firestoreService.getExpensePage(listId, cursor, pageSize).getOrElse { return Result.failure(it) }
                val categoriesCache =
                    categoriesByListId.getOrPut(listId) {
                        firestoreService.getGroupCategories(listId).getOrNull()
                            ?.associate { it.documentId to it.toDomain() } ?: emptyMap()
                    }
                val allUserIds = buildSet {
                    page.expenses.forEach { dto ->
                        dto.paidBy.getReferencePath()?.let { add(it.substringAfterLast("/")) }
                        dto.splitDetails.getSplitDetailData().forEach { (path, _) -> add(path.substringAfterLast("/")) }
                    }
                }
                val missingUserIds = allUserIds.filterNot(usersById::containsKey)
                val resolvedUsers: Map<String, User> = coroutineScope {
                    missingUserIds.map { userId -> async { firestoreService.getUserById(userId).getOrNull()?.toDomain(userId)?.let { userId to it } } }
                        .awaitAll().filterNotNull().toMap()
                }
                usersById.putAll(resolvedUsers)
                Result.success(ExpensePage(page.expenses.map { it.toDomain(categoriesCache, usersById) }, page.nextCursor, page.hasMore))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }

    override suspend fun getExpensesByListId(listId: String): Result<List<Expense>> =
        try {
            if (AppConfig.USE_MOCK_DATA) {
                // Mock implementation - return expenses filtered by listId
                MockExpenseData
                    .getMockExpenses()
                    .getOrDefault(emptyList())
                    .filter { it.listId == listId }
                    .let { Result.success(it) }
            } else {
                val dtos = firestoreService
                    .getExpensesByListId(listId)
                    .getOrElse { return Result.failure(it) }

                // 1 call: fetch all categories for this group
                val categoriesCache: Map<String, Category> = firestoreService
                    .getGroupCategories(listId)
                    .getOrNull()
                    ?.associate { it.documentId to it.toDomain() }
                    ?: emptyMap()

                // Collect unique user IDs from all expense DTOs
                val allUserIds: Set<String> = buildSet {
                    dtos.forEach { dto ->
                        dto.paidBy.getReferencePath()?.let { add(it.substringAfterLast("/")) }
                        dto.splitDetails.getSplitDetailData().forEach { (path, _) ->
                            add(path.substringAfterLast("/"))
                        }
                    }
                }

                // Fetch unique users in parallel
                val usersCache: Map<String, User> = coroutineScope {
                    allUserIds.map { userId ->
                        async {
                            firestoreService.getUserById(userId).getOrNull()
                                ?.toDomain(userId)
                                ?.let { user -> userId to user }
                        }
                    }.awaitAll()
                }.filterNotNull().toMap()

                Result.success(dtos.map { it.toDomain(categoriesCache, usersCache) })
            }
        } catch (e: Exception) {
            Result.failure(e)
        }

    override suspend fun getExpenseById(
        listId: String,
        expenseId: String,
    ): Result<Expense?> =
        try {
            if (AppConfig.USE_MOCK_DATA) {
                // Mock implementation - find expense by ID
                MockExpenseData
                    .getMockExpenses()
                    .getOrDefault(emptyList())
                    .find { it.documentId == expenseId }
                    .let { Result.success(it) }
            } else {
                firestoreService
                    .getExpenseById(listId, expenseId)
                    .mapCatching { dto ->
                        dto?.toDomain(firestoreService)
                    }
            }
        } catch (e: Exception) {
            Result.failure(e)
        }

    override suspend fun addExpenseToList(
        listId: String,
        expense: Expense,
    ): Result<String> =
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

    override suspend fun updateExpenseInList(
        listId: String,
        expense: Expense,
    ): Result<Unit> =
        try {
            if (AppConfig.USE_MOCK_DATA) {
                // Mock implementation - just return success
                Result.success(Unit)
            } else {
                val expenseDto = expense.copy(listId = listId).toDto()
                firestoreService.updateExpenseInList(listId, expense.documentId, expenseDto)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }

    override suspend fun deleteExpenseFromList(
        listId: String,
        expenseId: String,
    ): Result<Unit> =
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
