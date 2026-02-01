package com.marquis.zorroexpense.domain.usecase

import com.marquis.zorroexpense.domain.model.Expense
import com.marquis.zorroexpense.domain.repository.ExpenseRepository

/**
 * Use case for getting all expenses for a specific expense list
 */
class GetExpensesByListIdUseCase(
    private val expenseRepository: ExpenseRepository,
) {
    suspend operator fun invoke(listId: String): Result<List<Expense>> =
        expenseRepository.getExpensesByListId(listId)
}
