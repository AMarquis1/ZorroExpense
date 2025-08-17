package com.marquis.zorroexpense.navigation

import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

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
        val splitDetailsJson: String,
    ) : AppDestinations() {
        
        val splitDetails: List<SplitDetailNavigation>
            get() = try {
                Json.decodeFromString<List<SplitDetailNavigation>>(splitDetailsJson)
            } catch (e: Exception) {
                emptyList()
            }
        
        companion object {
            fun createSplitDetailsJson(splitDetails: List<SplitDetailNavigation>): String {
                return Json.encodeToString(splitDetails)
            }
        }
    }
    
    @Serializable
    data class SplitDetailNavigation(
        val userId: String,
        val amount: Double,
    )
}
