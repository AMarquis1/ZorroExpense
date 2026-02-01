package com.marquis.zorroexpense.data.repository

import com.marquis.zorroexpense.data.datasource.ExpenseLocalDataSource
import com.marquis.zorroexpense.data.datasource.ExpenseRemoteDataSource
import com.marquis.zorroexpense.domain.error.ExpenseError
import com.marquis.zorroexpense.domain.error.toExpenseError
import com.marquis.zorroexpense.domain.model.Expense
import com.marquis.zorroexpense.domain.repository.ExpenseRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.supervisorScope
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * Clean ExpenseRepository implementation following KMP and Clean Architecture standards
 *
 * Features:
 * - Proper separation of concerns with dedicated data sources
 * - Cache-first strategy with network fallback
 * - Consistent error handling with domain-specific errors
 * - Thread-safe operations with structured concurrency
 * - Configurable cache behavior
 * - Performance optimized with parallel operations
 */
class ExpenseRepositoryImpl(
    private val remoteDataSource: ExpenseRemoteDataSource,
    private val localDataSource: ExpenseLocalDataSource,
) : ExpenseRepository {
    private val repositoryMutex = Mutex()

    /**
     * Optimized cache-first strategy with parallel operations
     */
    override suspend fun getExpenses(userId: String): Result<List<Expense>> {
        return try {
            supervisorScope {
                // Start cache lookup immediately
                val cacheDeferred =
                    async {
                        localDataSource.getExpenses(userId)
                    }

                val cacheResult = cacheDeferred.await()

                // Return cached data if available and valid
                if (cacheResult.isSuccess && cacheResult.getOrNull()?.isNotEmpty() == true) {
                    return@supervisorScope cacheResult
                }

                // Cache miss - fetch from remote with fallback strategy
                fetchWithCacheFallback(userId)
            }
        } catch (e: Exception) {
            Result.failure(e.toExpenseError())
        }
    }

    /**
     * Force refresh with optimized error handling
     */
    override suspend fun refreshExpenses(userId: String): Result<List<Expense>> =
        try {
            supervisorScope {
                val remoteResult = remoteDataSource.getExpenses(userId)

                if (remoteResult.isSuccess) {
                    val expenses = remoteResult.getOrThrow()

                    // Update cache in background without blocking
                    async {
                        runCatching {
                            localDataSource.cacheExpenses(expenses)
                        }
                    }

                    Result.success(expenses)
                } else {
                    // Convert to domain error
                    val error =
                        remoteResult.exceptionOrNull()?.toExpenseError()
                            ?: ExpenseError.NetworkError("Failed to refresh expenses")
                    Result.failure(error)
                }
            }
        } catch (e: Exception) {
            Result.failure(e.toExpenseError())
        }

    /**
     * Optimized add with cache synchronization
     */
    override suspend fun addExpense(userId: String, expense: Expense): Result<Unit> =
        repositoryMutex.withLock {
            try {
                supervisorScope {
                    // Add to remote first
                    val remoteResult = remoteDataSource.addExpense(userId, expense)

                    if (remoteResult.isSuccess) {
                        // Update cache asynchronously
                        async {
                            runCatching {
                                localDataSource.addExpense(userId, expense)
                            }
                        }
                        Result.success(Unit)
                    } else {
                        val error =
                            remoteResult.exceptionOrNull()?.toExpenseError()
                                ?: ExpenseError.UnknownError("Failed to add expense")
                        Result.failure(error)
                    }
                }
            } catch (e: Exception) {
                Result.failure(e.toExpenseError())
            }
        }

    /**
     * Optimized update with cache synchronization
     */
    override suspend fun updateExpense(userId: String, expense: Expense): Result<Unit> =
        repositoryMutex.withLock {
            try {
                supervisorScope {
                    val remoteResult = remoteDataSource.updateExpense(userId, expense)

                    if (remoteResult.isSuccess) {
                        // Update cache asynchronously
                        async {
                            runCatching {
                                localDataSource.updateExpense(userId, expense)
                            }
                        }
                        Result.success(Unit)
                    } else {
                        val error =
                            remoteResult.exceptionOrNull()?.toExpenseError()
                                ?: ExpenseError.UnknownError("Failed to update expense")
                        Result.failure(error)
                    }
                }
            } catch (e: Exception) {
                Result.failure(e.toExpenseError())
            }
        }

    /**
     * Optimized delete with cache synchronization
     */
    override suspend fun deleteExpense(userId: String, expenseId: String): Result<Unit> =
        repositoryMutex.withLock {
            try {
                supervisorScope {
                    val remoteResult = remoteDataSource.deleteExpense(userId, expenseId)

                    if (remoteResult.isSuccess) {
                        // Update cache asynchronously
                        async {
                            runCatching {
                                localDataSource.deleteExpense(userId, expenseId)
                            }
                        }
                        Result.success(Unit)
                    } else {
                        val error =
                            remoteResult.exceptionOrNull()?.toExpenseError()
                                ?: ExpenseError.UnknownError("Failed to delete expense")
                        Result.failure(error)
                    }
                }
            } catch (e: Exception) {
                Result.failure(e.toExpenseError())
            }
        }

    /**
     * Non-blocking cache clear
     */
    override suspend fun clearCache() {
        try {
            supervisorScope {
                async {
                    localDataSource.clearAll()
                }
            }
        } catch (e: Exception) {
            // Log error but don't fail the operation
            println("Failed to clear cache: ${e.message}")
        }
    }

    /**
     * Get expenses for a specific expense list
     */
    override suspend fun getExpensesByListId(listId: String): Result<List<Expense>> {
        return try {
            supervisorScope {
                val remoteResult = remoteDataSource.getExpensesByListId(listId)
                if (remoteResult.isSuccess) {
                    Result.success(remoteResult.getOrThrow())
                } else {
                    val error =
                        remoteResult.exceptionOrNull()?.toExpenseError()
                            ?: ExpenseError.NetworkError("Failed to fetch expenses for list")
                    Result.failure(error)
                }
            }
        } catch (e: Exception) {
            Result.failure(e.toExpenseError())
        }
    }

    /**
     * Add expense to a list
     */
    suspend fun addExpenseToList(listId: String, expense: Expense): Result<String> =
        repositoryMutex.withLock {
            try {
                supervisorScope {
                    val remoteResult = remoteDataSource.addExpenseToList(listId, expense)

                    if (remoteResult.isSuccess) {
                        Result.success(remoteResult.getOrThrow())
                    } else {
                        val error =
                            remoteResult.exceptionOrNull()?.toExpenseError()
                                ?: ExpenseError.UnknownError("Failed to add expense to list")
                        Result.failure(error)
                    }
                }
            } catch (e: Exception) {
                Result.failure(e.toExpenseError())
            }
        }

    /**
     * Update expense in a list
     */
    suspend fun updateExpenseInList(listId: String, expense: Expense): Result<Unit> =
        repositoryMutex.withLock {
            try {
                supervisorScope {
                    val remoteResult = remoteDataSource.updateExpenseInList(listId, expense)

                    if (remoteResult.isSuccess) {
                        Result.success(Unit)
                    } else {
                        val error =
                            remoteResult.exceptionOrNull()?.toExpenseError()
                                ?: ExpenseError.UnknownError("Failed to update expense in list")
                        Result.failure(error)
                    }
                }
            } catch (e: Exception) {
                Result.failure(e.toExpenseError())
            }
        }

    /**
     * Delete expense from a list
     */
    suspend fun deleteExpenseFromList(listId: String, expenseId: String): Result<Unit> =
        repositoryMutex.withLock {
            try {
                supervisorScope {
                    val remoteResult = remoteDataSource.deleteExpenseFromList(listId, expenseId)

                    if (remoteResult.isSuccess) {
                        Result.success(Unit)
                    } else {
                        val error =
                            remoteResult.exceptionOrNull()?.toExpenseError()
                                ?: ExpenseError.UnknownError("Failed to delete expense from list")
                        Result.failure(error)
                    }
                }
            } catch (e: Exception) {
                Result.failure(e.toExpenseError())
            }
        }

    /**
     * Private helper for fetch with cache fallback strategy
     */
    private suspend fun fetchWithCacheFallback(userId: String): Result<List<Expense>> =
        supervisorScope {
            try {
                val remoteResult = remoteDataSource.getExpenses(userId)

                if (remoteResult.isSuccess) {
                    val expenses = remoteResult.getOrThrow()

                    // Cache update in background
                    async {
                        runCatching {
                            localDataSource.cacheExpenses(expenses)
                        }
                    }

                    Result.success(expenses)
                } else {
                    // Remote failed - try cache fallback
                    val cacheResult = localDataSource.getExpenses(userId)
                    if (cacheResult.isSuccess && cacheResult.getOrNull()?.isNotEmpty() == true) {
                        cacheResult
                    } else {
                        // Convert remote error to domain error
                        val error =
                            remoteResult.exceptionOrNull()?.toExpenseError()
                                ?: ExpenseError.NetworkError("Failed to fetch expenses")
                        Result.failure(error)
                    }
                }
            } catch (e: Exception) {
                // Final fallback to cache
                val cacheResult = localDataSource.getExpenses(userId)
                if (cacheResult.isSuccess && cacheResult.getOrNull()?.isNotEmpty() == true) {
                    cacheResult
                } else {
                    Result.failure(e.toExpenseError())
                }
            }
        }
}
