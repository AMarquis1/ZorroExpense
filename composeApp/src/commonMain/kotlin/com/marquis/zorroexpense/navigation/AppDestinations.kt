package com.marquis.zorroexpense.navigation

import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Serializable
sealed class AppDestinations {
    @Serializable
    data object Login : AppDestinations()

    @Serializable
    data object SignUp : AppDestinations()

    @Serializable
    data object ExpenseLists : AppDestinations()

    @Serializable
    data object CreateExpenseList : AppDestinations()

    @Serializable
    data class ExpenseList(
        val listId: String,
    ) : AppDestinations()

    @Serializable
    data class AddExpense(
        val listId: String,
    ) : AppDestinations()

    @Serializable
    data class ExpenseDetail(
        val listId: String,
        val expenseId: String,
        val expenseName: String,
        val expenseDescription: String,
        val expensePrice: Double,
        val expenseDate: String,
        val categoryDocumentId: String,
        val categoryName: String,
        val categoryIcon: String,
        val categoryColor: String,
        val paidByUserId: String,
        val paidByUserName: String,
        val paidByUserProfile: String,
        val splitDetailsJson: String,
    ) : AppDestinations() {
        val splitDetails: List<SplitDetailNavigation>
            get() =
                try {
                    Json.decodeFromString<List<SplitDetailNavigation>>(splitDetailsJson)
                } catch (e: Exception) {
                    emptyList()
                }

        companion object {
            fun createSplitDetailsJson(splitDetails: List<SplitDetailNavigation>): String = Json.encodeToString(splitDetails)
        }
    }

    @Serializable
    data class SplitDetailNavigation(
        val userId: String,
        val userName: String,
        val userProfile: String,
        val amount: Double,
    )

    @Serializable
    data class EditExpense(
        val listId: String,
        val expenseId: String,
        val expenseName: String,
        val expenseDescription: String,
        val expensePrice: Double,
        val expenseDate: String,
        val categoryDocumentId: String,
        val categoryName: String,
        val categoryIcon: String,
        val categoryColor: String,
        val paidByUserId: String,
        val paidByUserName: String,
        val paidByUserProfile: String,
        val splitDetailsJson: String,
    ) : AppDestinations() {
        val splitDetails: List<SplitDetailNavigation>
            get() =
                try {
                    Json.decodeFromString<List<SplitDetailNavigation>>(splitDetailsJson)
                } catch (e: Exception) {
                    emptyList()
                }

        companion object {
            fun createSplitDetailsJson(splitDetails: List<SplitDetailNavigation>): String = Json.encodeToString(splitDetails)
        }
    }
}
