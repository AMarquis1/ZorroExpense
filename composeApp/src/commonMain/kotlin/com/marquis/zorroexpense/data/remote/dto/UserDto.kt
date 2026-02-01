package com.marquis.zorroexpense.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UserDto(
    val documentId: String = "",
    @SerialName("name")
    val name: String = "",
    @SerialName("profileImage")
    val profileImage: String = "",
    @SerialName("ExpenseListReferences")
    val expenseListReferences: List<String> = emptyList(),
)
