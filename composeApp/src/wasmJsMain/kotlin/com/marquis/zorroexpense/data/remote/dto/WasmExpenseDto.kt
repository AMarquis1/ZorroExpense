package com.marquis.zorroexpense.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class WasmExpenseDto(
    @SerialName("description")
    override val description: String = "",
    @SerialName("name")
    override val name: String = "",
    @SerialName("price")
    override val price: Double = 0.0,
    @SerialName("date")
    override val date: String = "",
    @SerialName("category")
    val categoryId: String = "",
    @SerialName("paidBy")
    val paidById: String = "",
    @SerialName("splitWith")
    val splitWithIds: List<String> = emptyList(),
    @SerialName("isFromRecurring")
    override val isFromRecurring: Boolean = false,
    override val documentId: String,
) : ExpenseDto {
    override val category: Any? get() = categoryId
    override val paidBy: Any? get() = paidById
    override val splitWith: List<Any> get() = splitWithIds
}
