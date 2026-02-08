package com.marquis.zorroexpense.domain.usecase

import com.marquis.zorroexpense.domain.model.Group
import com.marquis.zorroexpense.domain.repository.GroupRepository

class RefreshUserGroupUseCase(
    private val groupRepository: GroupRepository,
) {
    suspend operator fun invoke(userId: String): Result<List<Group>> =
        try {
            groupRepository.refreshUserGroups(userId)
        } catch (e: Exception) {
            Result.failure(e)
        }
}
