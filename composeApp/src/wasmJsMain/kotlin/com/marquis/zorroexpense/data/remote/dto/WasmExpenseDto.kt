package com.marquis.zorroexpense.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class WasmSplitDetailDto(
    @SerialName("user")
    val userId: String = "",
    @SerialName("amount")
    val amount: Double = 0.0
)

@Serializable
data class WasmExpenseDto(
    override val documentId: String = "",
    @SerialName("listId")
    val listIdStr: String = "",
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
    @SerialName("splitDetails")
    val splitDetailsDto: List<WasmSplitDetailDto> = emptyList(),
    @SerialName("isFromRecurring")
    override val isFromRecurring: Boolean = false,
) : ExpenseDto {
    override val listId: Any get() = listIdStr
    override val category: Any? get() = categoryId
    override val paidBy: Any? get() = paidById
    override val splitDetails: List<Any> get() = splitDetailsDto
}
