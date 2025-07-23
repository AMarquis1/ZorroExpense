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
        val expenseName: String,
        val expenseDescription: String,
        val expensePrice: Double,
        val expenseDate: String
    ) : AppDestinations()
}