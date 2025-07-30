package com.marquis.zorroexpense.domain.repository

import com.marquis.zorroexpense.domain.model.Category

interface CategoryRepository {
    suspend fun getCategories(): Result<List<Category>>
}