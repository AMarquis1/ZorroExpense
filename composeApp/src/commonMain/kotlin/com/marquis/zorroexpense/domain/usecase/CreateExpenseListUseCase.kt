package com.marquis.zorroexpense.domain.usecase

import com.marquis.zorroexpense.data.remote.FirestoreService
import com.marquis.zorroexpense.data.remote.dto.toDomain
import com.marquis.zorroexpense.domain.model.ExpenseList
import com.marquis.zorroexpense.domain.model.User
import com.marquis.zorroexpense.domain.repository.ExpenseListRepository
import kotlinx.datetime.Clock

class CreateExpenseListUseCase(
    private val expenseListRepository: ExpenseListRepository,
    private val firestoreService: FirestoreService,
    private val getUsersUseCase: GetUsersUseCase,
) {
    suspend operator fun invoke(
        userId: String,
        name: String,
        categoryIds: List<String> = emptyList(),
    ): Result<String> {
        val shareCode = generateShareCode()

        // Fetch category objects for the provided category IDs
        val categories =
            categoryIds.mapNotNull { categoryId ->
                firestoreService.getCategoryById("Categories/$categoryId").getOrNull()?.toDomain()
            }

        // Fetch the creator's user data
        val creatorUser = getUsersUseCase.invoke(listOf(userId)).getOrNull()?.firstOrNull()
            ?: User(userId = userId, name = "", profileImage = "")

        val expenseList =
            ExpenseList(
                name = name,
                createdBy = userId,
                members = listOf(creatorUser),
                shareCode = shareCode,
                createdAt = Clock.System.now().toString(),
                categories = categories,
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
