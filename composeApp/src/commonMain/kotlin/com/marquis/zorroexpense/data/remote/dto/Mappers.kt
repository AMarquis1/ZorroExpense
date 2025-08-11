package com.marquis.zorroexpense.data.remote.dto

import com.marquis.zorroexpense.domain.model.Category
import com.marquis.zorroexpense.domain.model.Expense
import com.marquis.zorroexpense.domain.model.User
import com.marquis.zorroexpense.data.remote.FirestoreService

fun UserDto.toDomain(userId: String = ""): User = User(
    userId = userId,
    name = name,
    profileImage = profileImage
)

fun CategoryDto.toDomain(): Category = Category(
    name = name,
    icon = icon,
    color = color
)

expect fun Any?.toDateString(): String
expect fun Any?.getReferencePath(): String?
expect fun List<Any>.getReferencePaths(): List<String>

suspend fun ExpenseDto.toDomain(
    firestoreService: FirestoreService,
): Expense {
    val categoryPath = category.getReferencePath()
    
    val resolvedCategory = categoryPath?.let { path ->
        val categoryResult = firestoreService.getCategoryById(path)
        categoryResult.getOrNull()?.toDomain()
    } ?: Category()

    val resolvedPaidBy = paidBy.getReferencePath()?.let { path ->
        val userId = path.substringAfterLast("/")
        firestoreService.getUserById(path).getOrNull()?.toDomain(userId)
    } ?: User()

    val resolvedSplitWith = splitWith.getReferencePaths().mapNotNull { path ->
        val userId = path.substringAfterLast("/")
        firestoreService.getUserById(path).getOrNull()?.toDomain(userId)
    }

    return Expense(
        description = description,
        name = name,
        price = price,
        date = date.toDateString(),
        category = resolvedCategory,
        paidBy = resolvedPaidBy,
        splitWith = resolvedSplitWith
    )
}