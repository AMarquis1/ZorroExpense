package com.marquis.zorroexpense.data.remote

import com.marquis.zorroexpense.data.remote.dto.CategoryDto
import com.marquis.zorroexpense.data.remote.dto.ExpenseDto
import com.marquis.zorroexpense.data.remote.dto.GroupDto
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
    suspend fun getUserGroups(userId: String): Result<List<GroupDto>>

    suspend fun getGroupById(listId: String): Result<GroupDto?>

    suspend fun createGroup(group: GroupDto): Result<String>

    suspend fun updateGroup(
        listId: String,
        list: GroupDto,
    ): Result<Unit>

    suspend fun deleteGroup(groupId: String): Result<Unit>

    suspend fun getGroupByShareCode(shareCode: String): Result<GroupDto?>

    suspend fun addUserToExpenseListMembers(
        groupId: String,
        userId: String,
    ): Result<Unit>

    suspend fun addGroupToUser(
        userId: String,
        groupId: String,
    ): Result<Unit>

    suspend fun removeUserFromExpenseListMembers(
        groupId: String,
        userId: String,
    ): Result<Unit>

    suspend fun addExpenseListReferenceForUser(
        userId: String,
        groupId: String,
    ): Result<Unit>

    suspend fun removeExpenseListReferenceForUser(
        userId: String,
        groupId: String,
    ): Result<Unit>

    suspend fun getExpensesByListId(groupId: String): Result<List<ExpenseDto>>

    suspend fun getExpenseById(
        groupId: String,
        expenseId: String,
    ): Result<ExpenseDto?>

    suspend fun addExpenseToList(
        groupId: String,
        expense: ExpenseDto,
    ): Result<String>

    suspend fun updateExpenseInList(
        groupId: String,
        expenseId: String,
        expense: ExpenseDto,
    ): Result<Unit>

    suspend fun deleteExpenseFromList(
        groupId: String,
        expenseId: String,
    ): Result<Unit>

    suspend fun updateExpenseListLastModified(groupId: String): Result<Unit>

    // Group categories subcollection operations
    suspend fun getGroupCategories(groupId: String): Result<List<CategoryDto>>

    suspend fun setGroupCategories(
        listId: String,
        categories: List<CategoryDto>,
    ): Result<Unit>

    suspend fun deleteGroupCategory(
        listId: String,
        categoryId: String,
    ): Result<Unit>

    // Global category operations
    suspend fun createCategory(
        groupId: String,
        category: CategoryDto
    ): Result<String>

    suspend fun updateCategory(
        groupId: String,
        category: CategoryDto
    ): Result<Unit>

    suspend fun deleteCategory(
        groupId: String,
        categoryId: String
    ): Result<Unit>
}
