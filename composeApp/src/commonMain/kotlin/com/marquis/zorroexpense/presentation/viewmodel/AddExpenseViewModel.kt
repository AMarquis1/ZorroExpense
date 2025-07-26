package com.marquis.zorroexpense.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.marquis.zorroexpense.domain.model.Expense
import com.marquis.zorroexpense.domain.usecase.AddExpenseUseCase
import com.marquis.zorroexpense.presentation.state.AddExpenseFormState
import com.marquis.zorroexpense.presentation.state.AddExpenseUiEvent
import com.marquis.zorroexpense.presentation.state.AddExpenseUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class AddExpenseViewModel(
    private val addExpenseUseCase: AddExpenseUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<AddExpenseUiState>(AddExpenseUiState.Idle)
    val uiState: StateFlow<AddExpenseUiState> = _uiState.asStateFlow()

    private val _formState = MutableStateFlow(AddExpenseFormState())
    val formState: StateFlow<AddExpenseFormState> = _formState.asStateFlow()

    fun onEvent(event: AddExpenseUiEvent) {
        when (event) {
            is AddExpenseUiEvent.NameChanged -> updateName(event.name)
            is AddExpenseUiEvent.DescriptionChanged -> updateDescription(event.description)
            is AddExpenseUiEvent.PriceChanged -> updatePrice(event.price)
            is AddExpenseUiEvent.SaveExpense -> saveExpense()
            is AddExpenseUiEvent.ResetForm -> resetForm()
            is AddExpenseUiEvent.DismissError -> dismissError()
        }
    }

    private fun updateName(name: String) {
        _formState.update { currentState ->
            val isNameValid = name.isNotBlank()
            currentState.copy(
                expenseName = name,
                isNameValid = isNameValid,
                isFormValid = isNameValid && currentState.isPriceValid
            )
        }
    }

    private fun updateDescription(description: String) {
        _formState.update { currentState ->
            currentState.copy(expenseDescription = description)
        }
    }

    private fun updatePrice(price: String) {
        // Only allow numbers and decimal point
        if (price.isEmpty() || price.matches(Regex("^\\d*\\.?\\d*$"))) {
            _formState.update { currentState ->
                val isPriceValid = price.isNotBlank() && 
                                   price.toDoubleOrNull() != null && 
                                   price.toDoubleOrNull()!! > 0
                
                currentState.copy(
                    expensePrice = price,
                    isPriceValid = isPriceValid,
                    isFormValid = currentState.isNameValid && isPriceValid
                )
            }
        }
    }

    private fun saveExpense() {
        val currentFormState = _formState.value
        
        if (!currentFormState.isFormValid) {
            _uiState.value = AddExpenseUiState.Error("Please fill in all required fields correctly")
            return
        }

        viewModelScope.launch {
            _uiState.value = AddExpenseUiState.Loading
            
            try {
                // Simple date string for now - can be enhanced later
                val currentDate = "2024-01-01T12:00:00" // TODO: Use proper date/time
                
                val expense = Expense(
                    name = currentFormState.expenseName.trim(),
                    description = currentFormState.expenseDescription.trim(),
                    price = currentFormState.expensePrice.toDouble(),
                    date = currentDate
                )
                
                addExpenseUseCase(expense)
                    .onSuccess {
                        _uiState.value = AddExpenseUiState.Success
                        resetForm()
                    }
                    .onFailure { exception ->
                        _uiState.value = AddExpenseUiState.Error(
                            message = exception.message ?: "Failed to save expense"
                        )
                    }
            } catch (e: Exception) {
                _uiState.value = AddExpenseUiState.Error(
                    message = e.message ?: "An unexpected error occurred"
                )
            }
        }
    }

    private fun resetForm() {
        _formState.value = AddExpenseFormState()
        _uiState.value = AddExpenseUiState.Idle
    }

    private fun dismissError() {
        _uiState.value = AddExpenseUiState.Idle
    }
}