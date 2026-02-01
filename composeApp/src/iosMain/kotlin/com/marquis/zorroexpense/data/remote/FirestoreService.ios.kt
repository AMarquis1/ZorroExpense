package com.marquis.zorroexpense.data.remote

import com.marquis.zorroexpense.data.remote.dto.CategoryDto
import com.marquis.zorroexpense.data.remote.dto.ExpenseDto
import com.marquis.zorroexpense.data.remote.dto.IosExpenseDto
import com.marquis.zorroexpense.data.remote.dto.UserDto
import com.marquis.zorroexpense.data.remote.dto.UserProfileDto
import com.marquis.zorroexpense.data.remote.dto.toDto
import com.marquis.zorroexpense.domain.model.UserProfile
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.firestore.firestore
import dev.gitlive.firebase.firestore.firestoreSettings

@Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
actual class FirestoreService {
    private val firestore =
        Firebase.firestore.apply {
            // Configure Firestore with offline persistence for iOS using GitLive API
            try {
                settings =
                    firestoreSettings {
                        sslEnabled = true
                        // Note: GitLive Firebase SDK enables offline persistence by default on iOS
                        // The persistent cache will be configured automatically
                    }
            } catch (e: Exception) {
                // Settings might already be configured, that's okay
                println("FirestoreService iOS: Settings already configured or error: ${e.message}")
            }
        }

    actual suspend fun getExpenses(): Result<List<ExpenseDto>> =
        try {
            val snapshot = firestore.collection("Expense").get()
            val expenses =
                snapshot.documents.map { document ->
                    document.data<IosExpenseDto>().copy(documentId = document.id)
                }

            Result.success(expenses)
        } catch (e: Exception) {
            Result.failure(e)
        }

    actual suspend fun getUsers(): Result<List<UserDto>> =
        try {
            val snapshot = firestore.collection("Users").get()
            val users =
                snapshot.documents.map { document ->
                    document.data<UserDto>()
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

    actual suspend fun addExpense(expense: ExpenseDto): Result<Unit> =
        try {
            // Cast to IosExpenseDto for platform-specific implementation
            val iosExpenseDto = expense as IosExpenseDto

            println("YOLO ${iosExpenseDto.categoryRef} ${iosExpenseDto.paidByRef} ${iosExpenseDto.splitDetailsDto}")
            firestore.collection("Expense").add(iosExpenseDto)

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }

    actual suspend fun updateExpense(
        expenseId: String,
        expense: ExpenseDto,
    ): Result<Unit> =
        try {
            val iosExpenseDto = expense as IosExpenseDto

            firestore.collection("Expense").document(expenseId).set(iosExpenseDto)

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

    actual suspend fun createUserProfile(userId: String, profile: UserProfile): Result<Unit> =
        try {
            val profileDto = profile.toDto()
            firestore.collection("Users").document(userId).set(profileDto)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
}
