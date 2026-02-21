package com.marquis.zorroexpense.domain.usecase

import com.marquis.zorroexpense.domain.model.Category
import com.marquis.zorroexpense.domain.model.Group
import com.marquis.zorroexpense.domain.model.User
import com.marquis.zorroexpense.domain.repository.GroupRepository
import kotlinx.datetime.Clock

class CreateGroupUseCase(
    private val groupRepository: GroupRepository,
    private val getUsersUseCase: GetUsersUseCase,
) {
    suspend operator fun invoke(
        userId: String,
        name: String,
        categories: List<Category> = emptyList(),
    ): Result<String> {
        val shareCode = generateShareCode()

        // Fetch the creator's user data
        val creatorUser = getUsersUseCase.invoke(listOf(userId)).getOrNull()?.firstOrNull()
            ?: User(userId = userId, name = "", profileImage = "")

        val group =
            Group(
                name = name,
                createdBy = userId,
                members = listOf(creatorUser),
                shareCode = shareCode,
                createdAt = Clock.System.now().toString(),
                categories = categories,
            )

        return groupRepository.createGroup(group)
    }

    private fun generateShareCode(): String {
        val chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"
        return (1..6)
            .map { chars.random() }
            .joinToString("")
    }
}
