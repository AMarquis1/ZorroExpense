package com.marquis.zorroexpense

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Expense(
    @SerialName("Description") 
    val description: String = "",
    @SerialName("Name") 
    val name: String = "",
    @SerialName("price") 
    val price: Double = 0.0,
    @SerialName("date")
    val date: String = ""
)

expect class FirestoreService() {
    suspend fun getExpenses(): Result<List<Expense>>
}