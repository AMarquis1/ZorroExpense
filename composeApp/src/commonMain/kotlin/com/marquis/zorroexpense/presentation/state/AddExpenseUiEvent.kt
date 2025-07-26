package com.marquis.zorroexpense.presentation.state

sealed class AddExpenseUiEvent {
    data class NameChanged(val name: String) : AddExpenseUiEvent()
    data class DescriptionChanged(val description: String) : AddExpenseUiEvent()
    data class PriceChanged(val price: String) : AddExpenseUiEvent()
    
    object SaveExpense : AddExpenseUiEvent()
    object ResetForm : AddExpenseUiEvent()
    object DismissError : AddExpenseUiEvent()
}