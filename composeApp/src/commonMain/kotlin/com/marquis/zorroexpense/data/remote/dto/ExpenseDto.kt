package com.marquis.zorroexpense.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ExpenseDto(
    @SerialName("description")
    val description: String = "",
    @SerialName("name")
    val name: String = "",
    @SerialName("price") 
    val price: Double = 0.0,
    @SerialName("date")
    val date: String = "",
    @SerialName("category")
    val category: CategoryDto = CategoryDto(),
    @SerialName("paidBy")
    val paidBy: String = "",
    @SerialName("splitWith")
    val splitWith: List<String> = emptyList()
)