package com.marquis.zorroexpense.domain.model

data class Expense(
    val description: String = "",
    val name: String = "",
    val price: Double = 0.0,
    val date: String = "",
    val category: Category = Category(),
    val paidBy: String = "", // userId of who paid
    val splitWith: List<String> = emptyList() // List of userIds who split the expense
)