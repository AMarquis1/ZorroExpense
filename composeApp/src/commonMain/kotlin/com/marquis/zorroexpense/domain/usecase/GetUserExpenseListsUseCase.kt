package com.marquis.zorroexpense.domain.usecase

import com.marquis.zorroexpense.domain.model.ExpenseList
import com.marquis.zorroexpense.domain.repository.ExpenseListRepository

/**
 * Use case for getting all expense lists for a user
 */
class GetUserExpenseListsUseCase(
    private val expenseListRepository: ExpenseListRepository,
) {
    suspend operator fun invoke(userId: String): Result<List<ExpenseList>> =
        expenseListRepository.getUserExpenseLists(userId)
}
