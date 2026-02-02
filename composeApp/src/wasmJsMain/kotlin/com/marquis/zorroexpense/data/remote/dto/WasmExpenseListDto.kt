package com.marquis.zorroexpense.data.remote.dto

import com.marquis.zorroexpense.domain.model.User
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class WasmExpenseListDto(
    @SerialName("listId")
    override val listId: String = "",
    @SerialName("name")
    override val name: String = "",
    @SerialName("createdBy")
    override val createdBy: String = "",
    @SerialName("members")
    val memberIds: List<String> = emptyList(),
    @SerialName("shareCode")
    override val shareCode: String = "",
    @SerialName("createdAt")
    override val createdAt: String = "",
    @SerialName("isArchived")
    override val isArchived: Boolean = false,
    @SerialName("categories")
    val categoryIds: List<String> = emptyList(),
) : ExpenseListDto {
    override val members: List<Any> get() = memberIds
    override val categories: List<Any> get() = categoryIds
}

actual fun List<Any>.getMemberIds(): List<String> = filterIsInstance<String>()

actual fun List<Any>.getMemberUsers(): List<User> =
    filterIsInstance<String>().map { userId ->
        User(userId = userId, name = "", profileImage = "")
    }
