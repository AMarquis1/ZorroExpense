package com.marquis.zorroexpense.data.remote.dto

import com.marquis.zorroexpense.domain.model.ExpenseList
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * DTO for expense list from Firestore
 */
@Serializable
data class ExpenseListDto(
    @SerialName("listId")
    val listId: String = "",
    @SerialName("name")
    val name: String = "",
    @SerialName("createdBy")
    val createdBy: String = "",
    @SerialName("members")
    val members: List<String> = emptyList(),
    @SerialName("shareCode")
    val shareCode: String = "",
    @SerialName("createdAt")
    val createdAt: String = "",
    @SerialName("isArchived")
    val isArchived: Boolean = false,
    @SerialName("categories")
    val categories: List<String> = emptyList()
)

fun ExpenseListDto.toDomain(): ExpenseList = ExpenseList(
    listId = listId,
    name = name,
    createdBy = createdBy,
    members = members,
    shareCode = shareCode,
    createdAt = createdAt,
    isArchived = isArchived,
    categories = categories
)

fun ExpenseList.toDto(): ExpenseListDto = ExpenseListDto(
    listId = listId,
    name = name,
    createdBy = createdBy,
    members = members,
    shareCode = shareCode,
    createdAt = createdAt,
    isArchived = isArchived,
    categories = categories
)
