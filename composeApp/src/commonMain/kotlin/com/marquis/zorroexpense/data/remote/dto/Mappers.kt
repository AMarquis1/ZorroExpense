package com.marquis.zorroexpense.data.remote.dto

import com.marquis.zorroexpense.data.remote.FirestoreService
import com.marquis.zorroexpense.domain.model.Category
import com.marquis.zorroexpense.domain.model.Expense
import com.marquis.zorroexpense.domain.model.ExpenseList
import com.marquis.zorroexpense.domain.model.SplitDetail
import com.marquis.zorroexpense.domain.model.User

fun UserDto.toDomain(userId: String = ""): User =
    User(
        userId = userId.ifBlank { this.userId },
        name = name,
        profileImage = profileImage,
    )

fun CategoryDto.toDomain(): Category =
    Category(
        documentId = documentId,
        name = name,
        icon = icon,
        color = color,
    )

expect fun Any?.toDateString(): String

expect fun Any?.getReferencePath(): String?

expect fun Any?.getListIdPath(): String

expect fun List<Any>.getReferencePaths(): List<String>

expect fun List<Any>.getSplitDetailData(): List<Pair<String, Double>>

suspend fun ExpenseDto.toDomain(firestoreService: FirestoreService): Expense {
    val categoryPath = category.getReferencePath()

    val resolvedCategory =
        categoryPath?.let { path ->
            val categoryResult = firestoreService.getCategoryById(path)
            categoryResult.getOrNull()?.toDomain()
        } ?: Category()

    val resolvedPaidBy =
        paidBy.getReferencePath()?.let { path ->
            val userId = path.substringAfterLast("/")
            firestoreService.getUserById(path).getOrNull()?.toDomain(userId)
        } ?: User()

    val resolvedSplitDetails =
        splitDetails.getSplitDetailData().mapNotNull { (userPath, amount) ->
            val userId = userPath.substringAfterLast("/")
            firestoreService.getUserById(userPath).getOrNull()?.toDomain(userId)?.let { user ->
                SplitDetail(user = user, amount = amount)
            }
        }

    return Expense(
        documentId = documentId,
        listId = listId.getListIdPath(),
        description = description,
        name = name,
        price = price,
        date = date.toDateString(),
        category = resolvedCategory,
        paidBy = resolvedPaidBy,
        splitDetails = resolvedSplitDetails,
        isFromRecurring = isFromRecurring,
    )
}

suspend fun ExpenseListDto.toDomain(firestoreService: FirestoreService): ExpenseList {
    val resolvedCategories =
        categories.getCategoryPaths().mapNotNull { categoryPath ->
            firestoreService.getCategoryById(categoryPath).getOrNull()?.toDomain()
        }

    return ExpenseList(
        listId = listId,
        name = name,
        createdBy = createdBy,
        members = members.getMemberUsers(),
        shareCode = shareCode,
        createdAt = createdAt,
        isArchived = isArchived,
        categories = resolvedCategories,
    )
}
