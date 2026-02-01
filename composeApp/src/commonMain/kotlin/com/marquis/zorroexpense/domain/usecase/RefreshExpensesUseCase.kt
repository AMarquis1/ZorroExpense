package com.marquis.zorroexpense.domain.usecase

import com.marquis.zorroexpense.domain.model.Expense
import com.marquis.zorroexpense.domain.repository.ExpenseRepository

/**
 * Use case for refreshing expenses data
 * Forces fresh data fetch bypassing cache
 *
 * Follows Clean Architecture principles:
 * - Depends only on domain interfaces
 * - Single responsibility
 * - No framework dependencies
 */
class RefreshExpensesUseCase(
    private val expenseRepository: ExpenseRepository,
) {
    /**
     * Refresh expenses from remote source
     * @param userId The user ID to refresh expenses for
     * @return Result containing fresh list of expenses or error
     */
//    suspend operator fun invoke(userId: String): Result<List<Expense>> =
//        try {
//            expenseRepository.refreshExpenses(userId)
//        } catch (e: Exception) {
//            Result.failure(e)
//        }
}
