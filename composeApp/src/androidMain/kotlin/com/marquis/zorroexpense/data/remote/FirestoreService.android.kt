package com.marquis.zorroexpense.data.remote

import com.marquis.zorroexpense.data.remote.dto.ExpenseDto
import com.marquis.zorroexpense.data.remote.dto.AndroidExpenseDto
import com.marquis.zorroexpense.data.remote.dto.UserDto
import com.marquis.zorroexpense.data.remote.dto.CategoryDto
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.firestore.firestore
import dev.gitlive.firebase.firestore.DocumentReference

@Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
actual class FirestoreService {
    private val firestore = Firebase.firestore

    actual suspend fun getExpenses(): Result<List<ExpenseDto>> {
        return try {
            val snapshot = firestore.collection("Expense").get()
            val expenses = snapshot.documents.map { document ->
                document.data<AndroidExpenseDto>()
            }

            Result.success(expenses)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    actual suspend fun getUsers(): Result<List<UserDto>> {
        return try {
            val snapshot = firestore.collection("Users").get()
            val users = snapshot.documents.map { document ->
                document.data<UserDto>()
            }

            Result.success(users)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    actual suspend fun getCategories(): Result<List<CategoryDto>> {
        return try {
            val snapshot = firestore.collection("Categories").get()
            val categories = snapshot.documents.map { document ->
                document.data<CategoryDto>()
            }

            Result.success(categories)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    actual suspend fun getUserById(userId: String): Result<UserDto?> {
        return try {
            val document = firestore.collection("Users").document(userId).get()
            val user = if (document.exists) {
                document.data<UserDto>()
            } else {
                null
            }

            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    actual suspend fun getCategoryById(categoryId: String): Result<CategoryDto?> {
        return try {
            val document = firestore.document(categoryId).get()
            val category = if (document.exists) {
                document.data<CategoryDto>()
            } else {
                null
            }

            Result.success(category)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}