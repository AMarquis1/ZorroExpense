package com.marquis.zorroexpense

import android.util.Log
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

@Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
actual class FirestoreService {
    private val firestore = Firebase.firestore("(default)")

    actual suspend fun getExpenses(): Result<List<Expense>> {
        return suspendCancellableCoroutine { continuation ->
            Log.d("FirestoreService", "Starting to fetch expenses from database 'zorrofinance'...")

            firestore.collection("Expense")
                .get()
                .addOnSuccessListener { result ->
                    try {
                        Log.d("FirestoreService", "Query successful. Document count: ${result.size()}")
                        val expenses = mutableListOf<Expense>()

                        for (document in result) {
                            Log.d("FirestoreService", "${document.id} => ${document.data}")

                            try {
                                val expense = Expense(
                                    description = document.getString("description") ?: "",
                                    name = document.getString("name") ?: "",
                                    price = document.getDouble("price") ?: 0.0,
                                    date = document.getTimestamp("date")?.toDate()?.toString() ?: ""
                                )
                                expenses.add(expense)
                                Log.d("FirestoreService", "Parsed expense: $expense")
                            } catch (e: Exception) {
                                Log.w("FirestoreService", "Error parsing document ${document.id}", e)
                            }
                        }

                        Log.d("FirestoreService", "Total expenses parsed: ${expenses.size}")
                        continuation.resume(Result.success(expenses))
                    } catch (e: Exception) {
                        Log.w("FirestoreService", "Error processing results", e)
                        continuation.resume(Result.failure(e))
                    }
                }
                .addOnFailureListener { exception ->
                    Log.w("FirestoreService", "Error getting documents from 'zorrofinance' database.", exception)
                    continuation.resume(Result.failure(exception))
                }
        }
    }

}