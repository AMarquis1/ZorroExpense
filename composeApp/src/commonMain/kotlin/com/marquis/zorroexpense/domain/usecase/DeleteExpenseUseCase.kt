package com.marquis.zorroexpense.domain.usecase

import com.marquis.zorroexpense.domain.repository.ExpenseRepository

class DeleteExpenseUseCase(
    private val expenseRepository: ExpenseRepository,
) {
    suspend operator fun invoke(listId: String, expenseId: String): Result<Unit> = expenseRepository.deleteExpenseFromList(listId, expenseId)
}
