package com.marquis.zorroexpense.domain.usecase

import com.marquis.zorroexpense.domain.model.ExpenseList
import com.marquis.zorroexpense.domain.repository.ExpenseListRepository

/**
 * Use case for joining an expense list using share code
 */
class JoinExpenseListUseCase(
    private val expenseListRepository: ExpenseListRepository,
) {
    suspend operator fun invoke(
        userId: String,
        shareCode: String,
    ): Result<ExpenseList> = expenseListRepository.joinExpenseList(userId, shareCode)
}
