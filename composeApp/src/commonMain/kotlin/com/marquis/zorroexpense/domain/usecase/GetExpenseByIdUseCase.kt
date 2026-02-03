package com.marquis.zorroexpense.domain.usecase

import com.marquis.zorroexpense.domain.model.Expense
import com.marquis.zorroexpense.domain.repository.ExpenseRepository

class GetExpenseByIdUseCase(
    private val repository: ExpenseRepository,
) {
    suspend operator fun invoke(
        listId: String,
        expenseId: String,
    ): Result<Expense?> = repository.getExpenseById(listId, expenseId)
}