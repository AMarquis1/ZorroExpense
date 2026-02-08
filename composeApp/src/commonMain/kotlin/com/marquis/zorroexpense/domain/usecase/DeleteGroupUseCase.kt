package com.marquis.zorroexpense.domain.usecase

import com.marquis.zorroexpense.domain.repository.GroupRepository

class DeleteGroupUseCase(
    private val groupRepository: GroupRepository,
) {
    suspend operator fun invoke(listId: String): Result<Unit> =
        groupRepository.deleteGroup(listId)
}
