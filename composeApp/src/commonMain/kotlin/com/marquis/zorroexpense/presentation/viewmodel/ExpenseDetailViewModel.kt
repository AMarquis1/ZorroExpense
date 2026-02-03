package com.marquis.zorroexpense.presentation.viewmodel

import androidx.lifecycle.ViewModel
import com.marquis.zorroexpense.domain.model.Expense
import com.marquis.zorroexpense.presentation.state.ExpenseDetailUiEvent
import com.marquis.zorroexpense.presentation.state.ExpenseDetailUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class ExpenseDetailViewModel(
    private val expense: Expense,
) : ViewModel() {
    private val _uiState =
        MutableStateFlow<ExpenseDetailUiState>(
            ExpenseDetailUiState.Success(expense),
        )
    val uiState: StateFlow<ExpenseDetailUiState> = _uiState.asStateFlow()

    fun onEvent(event: ExpenseDetailUiEvent) {
        when (event) {
            is ExpenseDetailUiEvent.LoadExpense -> {
                // Already loaded in constructor
                _uiState.value = ExpenseDetailUiState.Success(expense)
            }
            is ExpenseDetailUiEvent.BackClicked -> {
                // Navigation will be handled by the UI layer
            }
            is ExpenseDetailUiEvent.DeleteExpense -> {
                // Show delete confirmation dialog
                val currentState = _uiState.value
                if (currentState is ExpenseDetailUiState.Success) {
                    _uiState.value = currentState.copy(showDeleteDialog = true)
                }
            }
            is ExpenseDetailUiEvent.CancelDelete -> {
                // Hide delete confirmation dialog
                val currentState = _uiState.value
                if (currentState is ExpenseDetailUiState.Success) {
                    _uiState.value = currentState.copy(showDeleteDialog = false)
                }
            }
            is ExpenseDetailUiEvent.ConfirmDelete -> {
                deleteExpense()
            }
        }
    }

    private fun deleteExpense() {
        val currentState = _uiState.value
        if (currentState is ExpenseDetailUiState.Success) {
            // Check if expense has valid documentId for deletion
            if (expense.documentId.isBlank()) {
                _uiState.value =
                    ExpenseDetailUiState.Error(
                        message = "Cannot delete expense: missing document ID",
                    )
                return
            }

            // Hide dialog and mark as "deleted" to trigger navigation
            // The actual database deletion will be handled by ExpenseListViewModel after delay
            _uiState.value =
                currentState.copy(
                    showDeleteDialog = false,
                    isDeleting = false,
                )

            // Immediately transition to "Deleted" state to trigger navigation callback
            _uiState.value = ExpenseDetailUiState.Deleted
        }
    }
}
