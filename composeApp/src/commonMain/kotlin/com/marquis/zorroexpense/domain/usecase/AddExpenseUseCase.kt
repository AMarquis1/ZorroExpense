package com.marquis.zorroexpense.domain.usecase

import com.marquis.zorroexpense.domain.model.Expense
import com.marquis.zorroexpense.domain.repository.ExpenseRepository

class AddExpenseUseCase(
    private val expenseRepository: ExpenseRepository,
) {
    suspend operator fun invoke(listId: String, expense: Expense): Result<String> = expenseRepository.addExpenseToList(listId, expense)
}
