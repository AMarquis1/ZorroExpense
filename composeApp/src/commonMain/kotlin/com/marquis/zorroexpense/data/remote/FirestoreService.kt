package com.marquis.zorroexpense.data.remote

import com.marquis.zorroexpense.data.remote.dto.ExpenseDto
import com.marquis.zorroexpense.data.remote.dto.UserDto
import com.marquis.zorroexpense.data.remote.dto.CategoryDto

expect class FirestoreService() {
    suspend fun getExpenses(): Result<List<ExpenseDto>>
    suspend fun getUsers(): Result<List<UserDto>>
    suspend fun getCategories(): Result<List<CategoryDto>>
    suspend fun getUserById(userId: String): Result<UserDto?>
    suspend fun getCategoryById(categoryId: String): Result<CategoryDto?>
}