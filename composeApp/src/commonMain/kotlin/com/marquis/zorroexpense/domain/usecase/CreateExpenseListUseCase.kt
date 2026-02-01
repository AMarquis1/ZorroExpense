package com.marquis.zorroexpense.domain.usecase

import com.marquis.zorroexpense.domain.model.ExpenseList
import com.marquis.zorroexpense.domain.repository.ExpenseListRepository
import kotlinx.datetime.Clock
class CreateExpenseListUseCase(
    private val expenseListRepository: ExpenseListRepository,
) {
    suspend operator fun invoke(
        userId: String,
        name: String,
        categories: List<String> = emptyList()
    ): Result<String> {
        val shareCode = generateShareCode()

        val expenseList = ExpenseList(
            name = name,
            createdBy = userId,
            members = listOf(userId),
            shareCode = shareCode,
            createdAt = Clock.System.now().toString(),
            categories = categories
        )

        return expenseListRepository.createExpenseList(expenseList)
    }

    private fun generateShareCode(): String {
        val chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"
        return (1..6)
            .map { chars.random() }
            .joinToString("")
    }
}
