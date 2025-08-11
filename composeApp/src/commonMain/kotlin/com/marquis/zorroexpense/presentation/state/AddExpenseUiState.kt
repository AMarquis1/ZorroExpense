package com.marquis.zorroexpense.presentation.state

import com.marquis.zorroexpense.domain.model.Category
import com.marquis.zorroexpense.domain.model.SplitMethod
import com.marquis.zorroexpense.domain.model.User

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
    val selectedCategory: Category? = null,
    val selectedPaidByUser: User? = null,
    val selectedSplitWithUsers: List<User> = emptyList(),
    val splitMethod: SplitMethod = SplitMethod.PERCENTAGE,
    val percentageSplits: Map<String, Float> = emptyMap(),
    val numberSplits: Map<String, Float> = emptyMap(),
    val isNameValid: Boolean = false,
    val isPriceValid: Boolean = false,
    val isCategoryValid: Boolean = false,
    val isPaidByValid: Boolean = false,
    val isFormValid: Boolean = false
)