package com.marquis.zorroexpense.presentation.state

/**
 * UI state for creating a new expense list
 */
sealed class CreateExpenseListUiState {
    data object Idle : CreateExpenseListUiState()

    data object Loading : CreateExpenseListUiState()

    data class Success(
        val listId: String,
        val listName: String,
    ) : CreateExpenseListUiState()

    data class Error(
        val message: String,
    ) : CreateExpenseListUiState()
}

/**
 * User events for creating an expense list
 */
sealed class CreateExpenseListUiEvent {
    data class ListNameChanged(
        val name: String,
    ) : CreateExpenseListUiEvent()

    data class CategoriesSelected(
        val categoryIds: List<String>,
    ) : CreateExpenseListUiEvent()

    data object CreateList : CreateExpenseListUiEvent()

    data object ClearError : CreateExpenseListUiEvent()
}
