package com.marquis.zorroexpense.data.remote.dto

import com.marquis.zorroexpense.domain.model.Category
import com.marquis.zorroexpense.domain.model.Expense
import com.marquis.zorroexpense.domain.model.User
import com.marquis.zorroexpense.data.remote.FirestoreService

// DTO to Domain mappings
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

// Platform-specific timestamp conversion
expect fun Any?.toDateString(): String

// Platform-specific helper functions
expect fun Any?.getReferencePath(): String?
expect fun List<Any>.getReferencePaths(): List<String>

// These will be used by the repository to create domain objects with resolved references
suspend fun ExpenseDto.toDomain(
    firestoreService: FirestoreService,
    expenseId: String = ""
): Expense {
    // Resolve category reference
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

// Domain to DTO mappings
fun User.toDto(): UserDto = UserDto(
    name = name,
    profileImage = profileImage
)

fun Category.toDto(): CategoryDto = CategoryDto(
    name = name,
    icon = icon,
    color = color
)

// Note: Domain to DTO conversion is not fully implemented
// since it requires platform-specific DocumentReference creation
// This would be needed for creating/updating expenses
// For now, this is just a placeholder
fun Expense.toDto(): Any {
    TODO("Domain to DTO conversion requires platform-specific implementation")
}