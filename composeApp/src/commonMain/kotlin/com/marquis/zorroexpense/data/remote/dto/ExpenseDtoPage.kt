package com.marquis.zorroexpense.data.remote.dto

data class ExpenseDtoPage(
    val expenses: List<ExpenseDto>,
    val nextCursor: String?,
    val hasMore: Boolean,
)
