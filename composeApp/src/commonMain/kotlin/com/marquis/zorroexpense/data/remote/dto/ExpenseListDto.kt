package com.marquis.zorroexpense.data.remote.dto

import com.marquis.zorroexpense.domain.model.User

// Common interface for platform-specific ExpenseListDto implementations
interface ExpenseListDto {
    val listId: String
    val name: String
    val createdBy: String
    val members: List<Any>
    val shareCode: String
    val createdAt: String
    val lastModified: Any
    val categories: List<Any>
}

expect fun List<Any>.getMemberIds(): List<String>

expect fun List<Any>.getMemberUsers(): List<User>

expect fun List<Any>.getCategoryPaths(): List<String>
