package com.marquis.zorroexpense.data.remote.dto

import dev.gitlive.firebase.firestore.DocumentReference
import dev.gitlive.firebase.firestore.Timestamp
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SplitDetailDto(
    @SerialName("user")
    val userRef: DocumentReference? = null,
    @SerialName("amount")
    val amount: Double = 0.0,
)

@Serializable
data class AndroidExpenseDto(
    override val documentId: String = "",
    @SerialName("listId")
    override val listId: DocumentReference,
    @SerialName("description")
    override val description: String = "",
    @SerialName("name")
    override val name: String = "",
    @SerialName("price")
    override val price: Double = 0.0,
    @SerialName("date")
    override val date: Timestamp? = null,
    @SerialName("category")
    val categoryRef: DocumentReference? = null,
    @SerialName("paidBy")
    val paidByRef: DocumentReference? = null,
    @SerialName("splitDetails")
    val splitDetailsDto: List<SplitDetailDto> = emptyList(),
    @SerialName("isFromRecurring")
    override val isFromRecurring: Boolean = false,
) : ExpenseDto {
    override val category: Any? get() = categoryRef
    override val paidBy: Any? get() = paidByRef
    override val splitDetails: List<Any> get() = splitDetailsDto
}
