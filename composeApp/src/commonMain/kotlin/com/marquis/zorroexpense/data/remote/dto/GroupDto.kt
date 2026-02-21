package com.marquis.zorroexpense.data.remote.dto

import com.marquis.zorroexpense.domain.model.User

// Common interface for platform-specific ExpenseListDto implementations
interface GroupDto {
    val groupId: String
    val name: String
    val createdBy: String
    val members: List<Any>
    val shareCode: String
    val createdAt: String
    val lastModified: Any
}

expect fun List<Any>.getMemberIds(): List<String>

expect fun List<Any>.getMemberUsers(): List<User>
