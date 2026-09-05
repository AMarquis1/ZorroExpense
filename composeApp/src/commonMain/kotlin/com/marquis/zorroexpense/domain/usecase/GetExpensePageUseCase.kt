package com.marquis.zorroexpense.domain.usecase

import com.marquis.zorroexpense.domain.model.ExpensePage
import com.marquis.zorroexpense.domain.repository.ExpenseRepository

class GetExpensePageUseCase(
    private val expenseRepository: ExpenseRepository,
) {
    suspend operator fun invoke(
        listId: String,
        cursor: String?,
        pageSize: Int = 10,
    ): Result<ExpensePage> = expenseRepository.getExpensePage(listId, cursor, pageSize)
}
