package com.marquis.zorroexpense.data.remote

import com.marquis.zorroexpense.data.remote.dto.ExpenseDto

expect class FirestoreService() {
    suspend fun getExpenses(): Result<List<ExpenseDto>>
}