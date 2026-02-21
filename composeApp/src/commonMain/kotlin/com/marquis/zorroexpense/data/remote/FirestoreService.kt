package com.marquis.zorroexpense.data.remote

import com.marquis.zorroexpense.data.remote.dto.CategoryDto
import com.marquis.zorroexpense.data.remote.dto.ExpenseDto
import com.marquis.zorroexpense.data.remote.dto.ExpenseListDto
import com.marquis.zorroexpense.data.remote.dto.UserDto
import com.marquis.zorroexpense.domain.model.UserProfile

@Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
expect class FirestoreService() {
    suspend fun getCategories(): Result<List<CategoryDto>>

    suspend fun getUserById(userId: String): Result<UserDto?>

    suspend fun getCategoryById(categoryId: String): Result<CategoryDto?>

    suspend fun createUserProfile(
        userId: String,
        profile: UserProfile,
    ): Result<Unit>

    // List-based expense operations
    suspend fun getUserExpenseLists(userId: String): Result<List<ExpenseListDto>>

    suspend fun getExpenseListById(listId: String): Result<ExpenseListDto?>

    suspend fun createExpenseList(list: ExpenseListDto): Result<String>

    suspend fun updateExpenseList(
        listId: String,
        list: ExpenseListDto,
    ): Result<Unit>

    suspend fun deleteExpenseList(listId: String): Result<Unit>

    suspend fun getExpenseListByShareCode(shareCode: String): Result<ExpenseListDto?>

    suspend fun addUserToExpenseListMembers(
        listId: String,
        userId: String,
    ): Result<Unit>

    suspend fun addGroupToUser(
        userId: String,
        listId: String,
    ): Result<Unit>

    suspend fun removeUserFromExpenseListMembers(
        listId: String,
        userId: String,
    ): Result<Unit>

    suspend fun addExpenseListReferenceForUser(
        userId: String,
        listId: String,
    ): Result<Unit>

    suspend fun removeExpenseListReferenceForUser(
        userId: String,
        listId: String,
    ): Result<Unit>

    suspend fun getExpensesByListId(listId: String): Result<List<ExpenseDto>>

    suspend fun getExpenseById(
        listId: String,
        expenseId: String,
    ): Result<ExpenseDto?>

    suspend fun addExpenseToList(
        listId: String,
        expense: ExpenseDto,
    ): Result<String>

    suspend fun updateExpenseInList(
        listId: String,
        expenseId: String,
        expense: ExpenseDto,
    ): Result<Unit>

    suspend fun deleteExpenseFromList(
        listId: String,
        expenseId: String,
    ): Result<Unit>

    suspend fun updateExpenseListLastModified(listId: String): Result<Unit>

    // Group categories subcollection operations
    suspend fun getGroupCategories(listId: String): Result<List<CategoryDto>>

    suspend fun setGroupCategories(
        listId: String,
        categories: List<CategoryDto>,
    ): Result<Unit>

    suspend fun deleteGroupCategory(
        listId: String,
        categoryId: String,
    ): Result<Unit>
}
