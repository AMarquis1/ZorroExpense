package com.marquis.zorroexpense.presentation.state

import com.marquis.zorroexpense.domain.model.ExpenseList

/**
 * UI state for the expense lists selection screen
 */
sealed class ExpenseListsUiState {
    data object Loading : ExpenseListsUiState()

    data class Success(
        val lists: List<ExpenseList>,
    ) : ExpenseListsUiState()

    data class Error(
        val message: String,
    ) : ExpenseListsUiState()

    data object Empty : ExpenseListsUiState()
}

/**
 * User events for the expense lists selection screen
 */
sealed class ExpenseListsUiEvent {
    data object LoadLists : ExpenseListsUiEvent()

    data object CreateNewList : ExpenseListsUiEvent()

    data class SelectList(
        val listId: String,
    ) : ExpenseListsUiEvent()

    data class JoinList(
        val shareCode: String,
    ) : ExpenseListsUiEvent()

    data object RetryLoad : ExpenseListsUiEvent()
}
