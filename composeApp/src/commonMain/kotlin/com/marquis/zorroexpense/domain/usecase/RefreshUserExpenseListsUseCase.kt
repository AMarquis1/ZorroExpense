package com.marquis.zorroexpense.domain.usecase

import com.marquis.zorroexpense.domain.model.ExpenseList
import com.marquis.zorroexpense.domain.repository.ExpenseListRepository

/**
 * Use case for refreshing user expense lists
 * Forces fresh data fetch bypassing cache
 *
 * Follows Clean Architecture principles:
 * - Depends only on domain interfaces
 * - Single responsibility
 * - No framework dependencies
 */
class RefreshUserExpenseListsUseCase(
    private val expenseListRepository: ExpenseListRepository,
) {
    /**
     * Refresh user expense lists from remote source
     * @param userId The user ID to refresh lists for
     * @return Result containing fresh list of expense lists or error
     */
    suspend operator fun invoke(userId: String): Result<List<ExpenseList>> =
        try {
            expenseListRepository.refreshUserExpenseLists(userId)
        } catch (e: Exception) {
            Result.failure(e)
        }
}
