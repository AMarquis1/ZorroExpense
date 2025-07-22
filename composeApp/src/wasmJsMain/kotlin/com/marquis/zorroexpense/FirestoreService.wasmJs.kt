package com.marquis.zorroexpense

actual class FirestoreService actual constructor() {
    actual suspend fun getExpenses(): Result<List<Expense>> {
        TODO("Not yet implemented")
    }

    actual fun close() {
    }
}