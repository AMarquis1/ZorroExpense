package com.marquis.zorroexpense.domain.usecase

import com.marquis.zorroexpense.domain.model.Group
import com.marquis.zorroexpense.domain.repository.GroupRepository

/**
 * Use case for joining an expense list using share code
 */
class JoinGroupUseCase(
    private val groupRepository: GroupRepository,
) {
    suspend operator fun invoke(
        userId: String,
        shareCode: String,
    ): Result<Group> = groupRepository.joinGroup(userId, shareCode)
}
