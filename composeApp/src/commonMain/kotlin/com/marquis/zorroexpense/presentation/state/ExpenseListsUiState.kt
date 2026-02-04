package com.marquis.zorroexpense.presentation.state

import com.marquis.zorroexpense.domain.model.ExpenseList

/**
 * UI state for the expense lists selection screen
 */
sealed class ExpenseListsUiState {
    data object Loading : ExpenseListsUiState()

    data class Success(
        val lists: List<ExpenseList>,
        val isRefreshing: Boolean = false,
        val showDeleteDialog: Boolean = false,
        val listToDelete: ExpenseList? = null,
    ) : ExpenseListsUiState()

    data class Error(
        val message: String,
        val cachedLists: List<ExpenseList>? = null,
    ) : ExpenseListsUiState()

    data object Empty : ExpenseListsUiState()
}

/**
 * User events for the expense lists selection screen
 */
sealed class ExpenseListsUiEvent {
    data object LoadLists : ExpenseListsUiEvent()

    data object CreateNewList : ExpenseListsUiEvent()
    data object RefreshLists: ExpenseListsUiEvent()

    data class SelectList(
        val listId: String,
    ) : ExpenseListsUiEvent()

    data class JoinList(
        val shareCode: String,
    ) : ExpenseListsUiEvent()

    data class DeleteList(val list: ExpenseList) : ExpenseListsUiEvent()

    data object ConfirmDelete : ExpenseListsUiEvent()

    data object CancelDelete : ExpenseListsUiEvent()

    data class EditList(val list: ExpenseList) : ExpenseListsUiEvent()

    data object RetryLoad : ExpenseListsUiEvent()
}
