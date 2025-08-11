package com.marquis.zorroexpense.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.marquis.zorroexpense.domain.model.Expense
import com.marquis.zorroexpense.domain.usecase.DeleteExpenseUseCase
import com.marquis.zorroexpense.presentation.state.ExpenseDetailUiEvent
import com.marquis.zorroexpense.presentation.state.ExpenseDetailUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ExpenseDetailViewModel(
    private val expense: Expense,
    private val deleteExpenseUseCase: DeleteExpenseUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<ExpenseDetailUiState>(
        ExpenseDetailUiState.Success(expense)
    )
    val uiState: StateFlow<ExpenseDetailUiState> = _uiState.asStateFlow()

    fun onEvent(event: ExpenseDetailUiEvent) {
        when (event) {
            is ExpenseDetailUiEvent.LoadExpense -> {
                // Already loaded in constructor, but could be used for refresh
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
            // Start deleting state
            _uiState.value = currentState.copy(
                showDeleteDialog = false,
                isDeleting = true
            )

            viewModelScope.launch {
                if (expense.documentId.isBlank()) {
                    _uiState.value = ExpenseDetailUiState.Error(
                        message = "Cannot delete expense: missing document ID"
                    )
                    return@launch
                }
                
                deleteExpenseUseCase(expense.documentId).fold(
                    onSuccess = {
                        // Successfully deleted
                        _uiState.value = ExpenseDetailUiState.Deleted
                    },
                    onFailure = { exception ->
                        // Failed to delete
                        _uiState.value = ExpenseDetailUiState.Error(
                            message = exception.message ?: "Failed to delete expense"
                        )
                    }
                )
            }
        }
    }
}