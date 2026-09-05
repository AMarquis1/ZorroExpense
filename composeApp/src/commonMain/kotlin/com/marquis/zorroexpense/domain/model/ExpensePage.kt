package com.marquis.zorroexpense.domain.model

data class ExpensePage(
    val expenses: List<Expense>,
    val nextCursor: String?,
    val hasMore: Boolean,
)
