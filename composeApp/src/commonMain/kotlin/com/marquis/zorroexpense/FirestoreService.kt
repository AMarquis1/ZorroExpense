package com.marquis.zorroexpense

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class User(
    @SerialName("userId")
    val userId: String = "",
    @SerialName("name")
    val name: String = "",
    @SerialName("profileImage")
    val profileImage: String = "" // Will store resource name like "sarah" or "alex"
)

@Serializable
data class Category(
    @SerialName("name")
    val name: String = "",
    @SerialName("icon")
    val icon: String = "",
    @SerialName("color")
    val color: String = "" // Hex color string like "#FF5722"
)

@Serializable
data class Expense(
    @SerialName("description")
    val description: String = "",
    @SerialName("name")
    val name: String = "",
    @SerialName("price") 
    val price: Double = 0.0,
    @SerialName("date")
    val date: String = "",
    @SerialName("category")
    val category: Category = Category(),
    @SerialName("paidBy")
    val paidBy: String = "", // userId of who paid
    @SerialName("splitWith")
    val splitWith: List<String> = emptyList() // List of userIds who split the expense
)

expect class FirestoreService() {
    suspend fun getExpenses(): Result<List<Expense>>
}