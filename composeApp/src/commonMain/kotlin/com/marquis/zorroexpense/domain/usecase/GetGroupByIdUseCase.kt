package com.marquis.zorroexpense.domain.usecase

import com.marquis.zorroexpense.domain.model.Group
import com.marquis.zorroexpense.domain.repository.GroupRepository

class GetGroupByIdUseCase(
    private val repository: GroupRepository,
) {
    suspend operator fun invoke(listId: String): Result<Group?> = repository.getGroup(listId)
}
