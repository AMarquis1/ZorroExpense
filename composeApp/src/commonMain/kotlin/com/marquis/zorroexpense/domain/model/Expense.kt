package com.marquis.zorroexpense.domain.model

data class Expense(
    val documentId: String = "",
    val description: String = "",
    val name: String = "",
    val price: Double = 0.0,
    val date: String = "",
    val category: Category = Category(),
    val paidBy: User = User(), // User object of who paid
    val splitWith: List<User> = emptyList() // List of User objects who split the expense
)