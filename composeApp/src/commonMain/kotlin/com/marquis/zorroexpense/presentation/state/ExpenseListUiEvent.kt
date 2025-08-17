package com.marquis.zorroexpense.presentation.state

import com.marquis.zorroexpense.domain.model.Category
import com.marquis.zorroexpense.domain.model.Expense

sealed class ExpenseListUiEvent {
    object LoadExpenses : ExpenseListUiEvent()

    object RefreshExpenses : ExpenseListUiEvent()

    data class SearchQueryChanged(
        val query: String,
    ) : ExpenseListUiEvent()

    data class SearchExpandedChanged(
        val isExpanded: Boolean,
    ) : ExpenseListUiEvent()

    data class CategoryToggled(
        val category: Category,
    ) : ExpenseListUiEvent()

    data class SortOptionChanged(
        val sortOption: SortOption,
    ) : ExpenseListUiEvent()

    data class MonthToggleCollapsed(
        val monthYear: String,
    ) : ExpenseListUiEvent()

    data class FabExpandedChanged(
        val isExpanded: Boolean,
    ) : ExpenseListUiEvent()

    data class ExpenseClicked(
        val expense: Expense,
    ) : ExpenseListUiEvent()

    object AddExpenseClicked : ExpenseListUiEvent()

    // Delete-related events
    data class PendingDeleteExpense(
        val expenseId: String,
    ) : ExpenseListUiEvent()

    data class UndoDeleteExpense(
        val expenseId: String,
    ) : ExpenseListUiEvent()

    data class ConfirmDeleteExpense(
        val expenseId: String,
    ) : ExpenseListUiEvent()

    object ToggleUpcomingExpenses : ExpenseListUiEvent()
}
