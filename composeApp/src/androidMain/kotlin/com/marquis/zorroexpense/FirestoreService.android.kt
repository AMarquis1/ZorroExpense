package com.marquis.zorroexpense

import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.firestore.firestore

@Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
actual class FirestoreService {
    private val firestore = Firebase.firestore

    actual suspend fun getExpenses(): Result<List<Expense>> {
        return try {
            val snapshot = firestore.collection("Expense").get()
            val expenses = snapshot.documents.map { document ->
                document.data<Expense>()
            }

            Result.success(expenses)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}