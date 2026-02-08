package com.marquis.zorroexpense.domain.usecase

import com.marquis.zorroexpense.domain.model.Group
import com.marquis.zorroexpense.domain.repository.GroupRepository

class GetUserGroupUseCase(
    private val groupRepository: GroupRepository,
) {
    suspend operator fun invoke(userId: String): Result<List<Group>> = groupRepository.getUserGroups(userId)
}
