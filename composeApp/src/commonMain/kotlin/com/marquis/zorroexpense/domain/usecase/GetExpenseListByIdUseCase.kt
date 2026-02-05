package com.marquis.zorroexpense.domain.usecase

import com.marquis.zorroexpense.domain.model.ExpenseList
import com.marquis.zorroexpense.domain.repository.ExpenseListRepository

class GetExpenseListByIdUseCase(
    private val repository: ExpenseListRepository,
) {
    suspend operator fun invoke(listId: String): Result<ExpenseList?> = repository.getExpenseListById(listId)
}
