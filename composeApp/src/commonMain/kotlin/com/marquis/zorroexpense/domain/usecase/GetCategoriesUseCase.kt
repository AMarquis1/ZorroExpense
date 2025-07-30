package com.marquis.zorroexpense.domain.usecase

import com.marquis.zorroexpense.domain.model.Category
import com.marquis.zorroexpense.domain.repository.CategoryRepository

class GetCategoriesUseCase(
    private val repository: CategoryRepository
) {
    suspend operator fun invoke(): Result<List<Category>> = repository.getCategories()
}