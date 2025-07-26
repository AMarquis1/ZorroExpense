package com.marquis.zorroexpense.data.remote

import com.marquis.zorroexpense.data.remote.dto.ExpenseDto
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.firestore.firestore

@Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
actual class FirestoreService {
    private val firestore = Firebase.firestore

    actual suspend fun getExpenses(): Result<List<ExpenseDto>> {
        return try {
            val snapshot = firestore.collection("Expense").get()
            val expenses = snapshot.documents.map { document ->
                document.data<ExpenseDto>()
            }

            Result.success(expenses)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}