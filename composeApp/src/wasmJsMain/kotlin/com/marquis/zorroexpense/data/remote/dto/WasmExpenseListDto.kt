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
    @SerialName("lastModified")
    override val lastModified: String = "",
) : ExpenseListDto {
    override val members: List<Any> get() = memberIds
}

actual fun List<Any>.getMemberIds(): List<String> = filterIsInstance<String>()

actual fun List<Any>.getMemberUsers(): List<User> =
    filterIsInstance<String>().map { userId ->
        User(userId = userId, name = "", profileImage = "")
    }
