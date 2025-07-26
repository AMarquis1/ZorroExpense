package com.marquis.zorroexpense.presentation.viewmodel

import androidx.lifecycle.ViewModel
import com.marquis.zorroexpense.domain.model.Expense
import com.marquis.zorroexpense.presentation.state.ExpenseDetailUiEvent
import com.marquis.zorroexpense.presentation.state.ExpenseDetailUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class ExpenseDetailViewModel(
    private val expense: Expense
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
        }
    }
}