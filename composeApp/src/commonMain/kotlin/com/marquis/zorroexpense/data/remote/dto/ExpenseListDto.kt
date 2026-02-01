package com.marquis.zorroexpense.data.remote.dto

// Common interface for platform-specific ExpenseListDto implementations
interface ExpenseListDto {
    val listId: String
    val name: String
    val createdBy: String
    val members: List<Any>
    val shareCode: String
    val createdAt: String
    val isArchived: Boolean
    val categories: List<Any>
}

expect fun List<Any>.getMemberIds(): List<String>

expect fun List<Any>.getCategoryPaths(): List<String>
