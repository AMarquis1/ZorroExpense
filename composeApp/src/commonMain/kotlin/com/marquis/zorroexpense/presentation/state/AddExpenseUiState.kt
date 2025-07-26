package com.marquis.zorroexpense.presentation.state

sealed class AddExpenseUiState {
    object Idle : AddExpenseUiState()
    object Loading : AddExpenseUiState()
    object Success : AddExpenseUiState()
    
    data class Error(
        val message: String
    ) : AddExpenseUiState()
}

data class AddExpenseFormState(
    val expenseName: String = "",
    val expenseDescription: String = "",
    val expensePrice: String = "",
    val isNameValid: Boolean = false,
    val isPriceValid: Boolean = false,
    val isFormValid: Boolean = false
)