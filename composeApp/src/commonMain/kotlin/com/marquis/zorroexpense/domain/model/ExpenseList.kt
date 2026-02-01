package com.marquis.zorroexpense.domain.model

/**
 * Represents an expense list that groups related expenses.
 * Multiple users can be members of the same list.
 * Each list has its own set of categories selected from the global Categories collection.
 */
data class ExpenseList(
    val listId: String = "",
    val name: String = "",
    val createdBy: String = "",
    val members: List<String> = emptyList(),
    val shareCode: String = "",
    val createdAt: String = "",
    val isArchived: Boolean = false,
    val categories: List<Category> = emptyList()
)
