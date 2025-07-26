package com.marquis.zorroexpense.presentation.state

import com.marquis.zorroexpense.domain.model.Expense

sealed class ExpenseDetailUiState {
    object Loading : ExpenseDetailUiState()
    
    data class Success(
        val expense: Expense
    ) : ExpenseDetailUiState()
    
    data class Error(
        val message: String
    ) : ExpenseDetailUiState()
}

sealed class ExpenseDetailUiEvent {
    object LoadExpense : ExpenseDetailUiEvent()
    object BackClicked : ExpenseDetailUiEvent()
}