package com.marquis.zorroexpense.domain.usecase

import com.marquis.zorroexpense.domain.model.ExpenseList
import com.marquis.zorroexpense.domain.repository.ExpenseListRepository

class UpdateExpenseListUseCase(
    private val expenseListRepository: ExpenseListRepository,
) {
    suspend operator fun invoke(
        listId: String,
        list: ExpenseList,
    ): Result<Unit> = expenseListRepository.updateExpenseList(listId, list)
}
