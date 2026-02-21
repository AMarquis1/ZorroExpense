package com.marquis.zorroexpense.domain.usecase

import com.marquis.zorroexpense.domain.model.Category
import com.marquis.zorroexpense.domain.repository.GroupRepository

class CreateCategoryUseCase(private val groupRepository: GroupRepository) {
    suspend operator fun invoke(groupId: String, category: Category): Result<String> =
        groupRepository.createCategory(groupId, category)
}
