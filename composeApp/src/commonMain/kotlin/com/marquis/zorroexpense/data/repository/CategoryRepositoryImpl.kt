package com.marquis.zorroexpense.data.repository

import com.marquis.zorroexpense.AppConfig
import com.marquis.zorroexpense.MockExpenseData
import com.marquis.zorroexpense.data.remote.FirestoreService
import com.marquis.zorroexpense.data.remote.dto.toDomain
import com.marquis.zorroexpense.domain.model.Category
import com.marquis.zorroexpense.domain.repository.CategoryRepository

class CategoryRepositoryImpl(
    private val firestoreService: FirestoreService
) : CategoryRepository {

    override suspend fun getCategories(): Result<List<Category>> {
        return try {
            if (AppConfig.USE_MOCK_DATA) {
                // Use mock data for development/testing
                Result.success(MockExpenseData.allCategories)
            } else {
                // Use Firestore for production
                firestoreService.getCategories()
                    .mapCatching { categoryDtos ->
                        categoryDtos.map { it.toDomain() }
                    }
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}