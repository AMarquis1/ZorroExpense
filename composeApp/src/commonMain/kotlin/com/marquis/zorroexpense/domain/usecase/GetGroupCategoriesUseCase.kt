package com.marquis.zorroexpense.domain.usecase

import com.marquis.zorroexpense.domain.model.Category
import com.marquis.zorroexpense.domain.repository.GroupRepository

class GetGroupCategoriesUseCase(
    private val repository: GroupRepository,
) {
    suspend operator fun invoke(groupId: String): Result<List<Category>> = repository.getCategories(groupId)
}
