package com.marquis.zorroexpense.domain.usecase

import com.marquis.zorroexpense.domain.repository.ExpenseListRepository

class DeleteExpenseListUseCase(
    private val expenseListRepository: ExpenseListRepository,
) {
    suspend operator fun invoke(listId: String): Result<Unit> =
        expenseListRepository.deleteExpenseList(listId)
}
