package com.marquis.zorroexpense.domain.usecase

import com.marquis.zorroexpense.domain.model.Group
import com.marquis.zorroexpense.domain.repository.GroupRepository

class UpdateGroupUseCase(
    private val groupRepository: GroupRepository,
) {
    suspend operator fun invoke(
        listId: String,
        list: Group,
    ): Result<Unit> = groupRepository.updateGroup(listId, list)
}
