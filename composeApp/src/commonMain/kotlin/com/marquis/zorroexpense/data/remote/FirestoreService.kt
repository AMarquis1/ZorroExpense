package com.marquis.zorroexpense.data.remote

import com.marquis.zorroexpense.data.remote.dto.CategoryDto
import com.marquis.zorroexpense.data.remote.dto.ExpenseDto
import com.marquis.zorroexpense.data.remote.dto.UserDto
import com.marquis.zorroexpense.data.remote.dto.UserProfileDto
import com.marquis.zorroexpense.domain.model.UserProfile

@Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
expect class FirestoreService() {
    suspend fun getExpenses(): Result<List<ExpenseDto>>

    suspend fun addExpense(expense: ExpenseDto): Result<Unit>

    suspend fun updateExpense(
        expenseId: String,
        expense: ExpenseDto,
    ): Result<Unit>

    suspend fun deleteExpense(expenseId: String): Result<Unit>

    suspend fun getUsers(): Result<List<UserDto>>

    suspend fun getCategories(): Result<List<CategoryDto>>

    suspend fun getUserById(userId: String): Result<UserDto?>

    suspend fun getCategoryById(categoryId: String): Result<CategoryDto?>

    suspend fun createUserProfile(userId: String, profile: UserProfile): Result<Unit>
}
