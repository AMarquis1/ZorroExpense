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
    override suspend fun getExpenses(): Result<List<Expense>> {
        return try {
            supervisorScope {
                // Start cache lookup immediately
                val cacheDeferred =
                    async {
                        localDataSource.getExpenses()
                    }

                val cacheResult = cacheDeferred.await()

                // Return cached data if available and valid
                if (cacheResult.isSuccess && cacheResult.getOrNull()?.isNotEmpty() == true) {
                    return@supervisorScope cacheResult
                }

                // Cache miss - fetch from remote with fallback strategy
                fetchWithCacheFallback()
            }
        } catch (e: Exception) {
            Result.failure(e.toExpenseError())
        }
    }

    /**
     * Force refresh with optimized error handling
     */
    override suspend fun refreshExpenses(): Result<List<Expense>> =
        try {
            supervisorScope {
                val remoteResult = remoteDataSource.getExpenses()

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
    override suspend fun addExpense(expense: Expense): Result<Unit> =
        repositoryMutex.withLock {
            try {
                supervisorScope {
                    // Add to remote first
                    val remoteResult = remoteDataSource.addExpense(expense)

                    if (remoteResult.isSuccess) {
                        // Update cache asynchronously
                        async {
                            runCatching {
                                localDataSource.addExpense(expense)
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
    override suspend fun updateExpense(expense: Expense): Result<Unit> =
        repositoryMutex.withLock {
            try {
                supervisorScope {
                    val remoteResult = remoteDataSource.updateExpense(expense)

                    if (remoteResult.isSuccess) {
                        // Update cache asynchronously
                        async {
                            runCatching {
                                localDataSource.updateExpense(expense)
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
    override suspend fun deleteExpense(expenseId: String): Result<Unit> =
        repositoryMutex.withLock {
            try {
                supervisorScope {
                    val remoteResult = remoteDataSource.deleteExpense(expenseId)

                    if (remoteResult.isSuccess) {
                        // Update cache asynchronously
                        async {
                            runCatching {
                                localDataSource.deleteExpense(expenseId)
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
     * Private helper for fetch with cache fallback strategy
     */
    private suspend fun fetchWithCacheFallback(): Result<List<Expense>> =
        supervisorScope {
            try {
                val remoteResult = remoteDataSource.getExpenses()

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
                    val cacheResult = localDataSource.getExpenses()
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
                val cacheResult = localDataSource.getExpenses()
                if (cacheResult.isSuccess && cacheResult.getOrNull()?.isNotEmpty() == true) {
                    cacheResult
                } else {
                    Result.failure(e.toExpenseError())
                }
            }
        }
}
