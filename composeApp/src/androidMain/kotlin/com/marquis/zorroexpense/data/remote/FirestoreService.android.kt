package com.marquis.zorroexpense.data.remote

import com.marquis.zorroexpense.data.remote.dto.AndroidExpenseDto
import com.marquis.zorroexpense.data.remote.dto.CategoryDto
import com.marquis.zorroexpense.data.remote.dto.ExpenseDto
import com.marquis.zorroexpense.data.remote.dto.UserDto
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.firestore.firestore

@Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
actual class FirestoreService {
    private val firestore = Firebase.firestore

    actual suspend fun getExpenses(): Result<List<ExpenseDto>> =
        try {
            val snapshot = firestore.collection("Expense").get()
            val expenses =
                snapshot.documents.map { document ->
                    document.data<AndroidExpenseDto>().copy(documentId = document.id)
                }

            Result.success(expenses)
        } catch (e: Exception) {
            Result.failure(e)
        }

    actual suspend fun addExpense(expense: ExpenseDto): Result<Unit> =
        try {
            // Cast to AndroidExpenseDto for platform-specific implementation
            val androidExpenseDto = expense as AndroidExpenseDto

            // Add the expense to Firestore
            firestore.collection("Expense").add(androidExpenseDto)

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }

    actual suspend fun updateExpense(
        expenseId: String,
        expense: ExpenseDto,
    ): Result<Unit> =
        try {
            val androidExpenseDto = expense as AndroidExpenseDto

            firestore.collection("Expense").document(expenseId).set(androidExpenseDto)

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }

    actual suspend fun deleteExpense(expenseId: String): Result<Unit> =
        try {
            firestore.collection("Expense").document(expenseId).delete()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }

    actual suspend fun getUsers(): Result<List<UserDto>> =
        try {
            val snapshot = firestore.collection("Users").get()
            val users =
                snapshot.documents.map { document ->
                    val userData = document.data<UserDto>()
                    userData.copy(documentId = document.id) // Set the document ID
                }

            Result.success(users)
        } catch (e: Exception) {
            Result.failure(e)
        }

    actual suspend fun getCategories(): Result<List<CategoryDto>> =
        try {
            val snapshot = firestore.collection("Categories").get()
            val categories =
                snapshot.documents.map { document ->
                    val categoryDto = document.data<CategoryDto>()
                    categoryDto.copy(documentId = document.id)
                }

            Result.success(categories)
        } catch (e: Exception) {
            Result.failure(e)
        }

    actual suspend fun getUserById(userId: String): Result<UserDto?> =
        try {
            val document = firestore.document(userId).get()
            val user =
                if (document.exists) {
                    document.data<UserDto>()
                } else {
                    null
                }

            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }

    actual suspend fun getCategoryById(categoryId: String): Result<CategoryDto?> =
        try {
            val document = firestore.document(categoryId).get()
            val category =
                if (document.exists) {
                    val categoryDto = document.data<CategoryDto>()
                    categoryDto.copy(documentId = document.id)
                } else {
                    null
                }

            Result.success(category)
        } catch (e: Exception) {
            Result.failure(e)
        }
}
