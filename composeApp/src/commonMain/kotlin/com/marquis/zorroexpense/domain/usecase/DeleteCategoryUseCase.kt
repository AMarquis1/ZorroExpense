package com.marquis.zorroexpense.domain.usecase

import com.marquis.zorroexpense.domain.repository.GroupRepository

class DeleteCategoryUseCase(private val groupRepository: GroupRepository) {
    suspend operator fun invoke(groupId: String, categoryId: String): Result<Unit> =
        groupRepository.deleteCategory(groupId, categoryId)
}
