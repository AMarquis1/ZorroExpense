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
    data class EditExpenseList(
        val listId: String,
        val listName: String,
    ) : AppDestinations()

    @Serializable
    data class ExpenseList(
        val listId: String,
        val listName: String = "",
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
    data class ExpenseListDetail(
        val listId: String,
        val listName: String,
        val shareCode: String,
        val createdBy: String,
        val createdAt: String,
        val lastModified: String,
        val membersJson: String,
        val categoriesJson: String,
    ) : AppDestinations() {
        companion object {
            fun createMembersJson(members: List<MemberNavigation>): String = Json.encodeToString(members)

            fun createCategoriesJson(categories: List<CategoryNavigation>): String = Json.encodeToString(categories)
        }

        val members: List<MemberNavigation>
            get() = try {
                Json.decodeFromString<List<MemberNavigation>>(membersJson)
            } catch (e: Exception) {
                emptyList()
            }

        val categories: List<CategoryNavigation>
            get() = try {
                Json.decodeFromString<List<CategoryNavigation>>(categoriesJson)
            } catch (e: Exception) {
                emptyList()
            }
    }

    @Serializable
    data class MemberNavigation(
        val userId: String,
        val name: String,
        val profileImage: String,
    )

    @Serializable
    data class CategoryNavigation(
        val documentId: String,
        val name: String,
        val icon: String,
        val color: String,
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
