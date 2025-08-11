package com.marquis.zorroexpense.domain.usecase

import com.marquis.zorroexpense.domain.model.Expense
import com.marquis.zorroexpense.domain.repository.ExpenseRepository

class GetExpensesUseCase(
    private val expenseRepository: ExpenseRepository,
) {
    suspend operator fun invoke(): Result<List<Expense>> = expenseRepository.getExpenses()
}
