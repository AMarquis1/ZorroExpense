package com.marquis.zorroexpense.navigation

import kotlinx.serialization.Serializable

@Serializable
sealed class AppDestinations {
    @Serializable
    data object ExpenseList : AppDestinations()
    
    @Serializable
    data object AddExpense : AppDestinations()
    
    @Serializable
    data class ExpenseDetail(
        val expenseId: String,
        val expenseName: String,
        val expenseDescription: String,
        val expensePrice: Double,
        val expenseDate: String,
        val categoryName: String,
        val categoryIcon: String,
        val categoryColor: String,
        val paidByUserId: String,
        val splitWithUserIds: List<String>
    ) : AppDestinations()
}