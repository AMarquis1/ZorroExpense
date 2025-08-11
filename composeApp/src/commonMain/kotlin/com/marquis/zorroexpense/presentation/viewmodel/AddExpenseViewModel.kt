package com.marquis.zorroexpense.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.marquis.zorroexpense.domain.model.Category
import com.marquis.zorroexpense.domain.model.Expense
import com.marquis.zorroexpense.domain.model.SplitMethod
import com.marquis.zorroexpense.domain.model.User
import com.marquis.zorroexpense.domain.usecase.AddExpenseUseCase
import com.marquis.zorroexpense.domain.usecase.GetCategoriesUseCase
import com.marquis.zorroexpense.presentation.state.AddExpenseFormState
import com.marquis.zorroexpense.presentation.state.AddExpenseUiEvent
import com.marquis.zorroexpense.presentation.state.AddExpenseUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock

class AddExpenseViewModel(
    private val addExpenseUseCase: AddExpenseUseCase,
    private val getCategoriesUseCase: GetCategoriesUseCase,
) : ViewModel() {
    private val _uiState = MutableStateFlow<AddExpenseUiState>(AddExpenseUiState.Idle)
    val uiState: StateFlow<AddExpenseUiState> = _uiState.asStateFlow()

    private val _formState = MutableStateFlow(AddExpenseFormState())
    val formState: StateFlow<AddExpenseFormState> = _formState.asStateFlow()

    private val _categories = MutableStateFlow<List<Category>>(emptyList())
    val categories: StateFlow<List<Category>> = _categories.asStateFlow()

    init {
        loadCategories()
    }

    fun onEvent(event: AddExpenseUiEvent) {
        when (event) {
            is AddExpenseUiEvent.NameChanged -> updateName(event.name)
            is AddExpenseUiEvent.DescriptionChanged -> updateDescription(event.description)
            is AddExpenseUiEvent.PriceChanged -> updatePrice(event.price)
            is AddExpenseUiEvent.CategoryChanged -> updateCategory(event.category)
            is AddExpenseUiEvent.PaidByChanged -> updatePaidBy(event.user)
            is AddExpenseUiEvent.SplitWithChanged -> updateSplitWith(event.users)
            is AddExpenseUiEvent.SplitMethodChanged -> updateSplitMethod(event.method)
            is AddExpenseUiEvent.PercentageSplitsChanged -> updatePercentageSplits(event.splits)
            is AddExpenseUiEvent.NumberSplitsChanged -> updateNumberSplits(event.splits)
            is AddExpenseUiEvent.PercentageChanged -> updateSinglePercentage(event.userId, event.percentage)
            is AddExpenseUiEvent.NumberChanged -> updateSingleNumber(event.userId, event.amount)
            is AddExpenseUiEvent.ResetToEqualSplits -> resetToEqualSplits()
            is AddExpenseUiEvent.RemoveUserFromSplit -> removeUserFromSplit(event.user)
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
                isFormValid = validateForm(currentState.copy(expenseName = name, isNameValid = isNameValid)),
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
                val isPriceValid =
                    price.isNotBlank() &&
                        price.toDoubleOrNull() != null &&
                        price.toDoubleOrNull()!! > 0

                val updatedState =
                    currentState.copy(
                        expensePrice = price,
                        isPriceValid = isPriceValid,
                    )

                // Recalculate splits when price changes
                val (updatedPercentageSplits, updatedNumberSplits) =
                    calculateEqualSplits(
                        updatedState.selectedSplitWithUsers,
                        price,
                    )

                updatedState.copy(
                    percentageSplits = updatedPercentageSplits,
                    numberSplits = updatedNumberSplits,
                    isFormValid = validateForm(updatedState),
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
                // Generate current timestamp in ISO format
                val currentInstant = Clock.System.now()
                val currentDate = currentInstant.toString()

                val expense =
                    Expense(
                        name = currentFormState.expenseName.trim(),
                        description = currentFormState.expenseDescription.trim(),
                        price = currentFormState.expensePrice.toDouble(),
                        date = currentDate,
                        category = currentFormState.selectedCategory ?: Category(),
                        paidBy = currentFormState.selectedPaidByUser ?: User(),
                        splitWith = currentFormState.selectedSplitWithUsers,
                    )

                addExpenseUseCase(expense)
                    .onSuccess {
                        _uiState.value = AddExpenseUiState.Success
                        resetForm()
                    }.onFailure { exception ->
                        _uiState.value =
                            AddExpenseUiState.Error(
                                message = exception.message ?: "Failed to save expense",
                            )
                    }
            } catch (e: Exception) {
                _uiState.value =
                    AddExpenseUiState.Error(
                        message = e.message ?: "An unexpected error occurred",
                    )
            }
        }
    }

    private fun resetForm() {
        _formState.value = AddExpenseFormState()
        _uiState.value = AddExpenseUiState.Idle
    }

    private fun updateCategory(category: Category) {
        _formState.update { currentState ->
            val updatedState =
                currentState.copy(
                    selectedCategory = category,
                    isCategoryValid = true,
                )
            updatedState.copy(isFormValid = validateForm(updatedState))
        }
    }

    private fun updatePaidBy(user: User) {
        _formState.update { currentState ->
            // Automatically add the payer to split with if not already included
            val updatedSplitWithUsers =
                if (!currentState.selectedSplitWithUsers.any { it.userId == user.userId }) {
                    currentState.selectedSplitWithUsers + user
                } else {
                    currentState.selectedSplitWithUsers
                }

            val updatedState =
                currentState.copy(
                    selectedPaidByUser = user,
                    selectedSplitWithUsers = updatedSplitWithUsers,
                    isPaidByValid = true,
                )

            // Recalculate splits with new users
            val (updatedPercentageSplits, updatedNumberSplits) = calculateEqualSplits(updatedSplitWithUsers, updatedState.expensePrice)

            updatedState.copy(
                percentageSplits = updatedPercentageSplits,
                numberSplits = updatedNumberSplits,
                isFormValid = validateForm(updatedState),
            )
        }
    }

    private fun updateSplitWith(users: List<User>) {
        _formState.update { currentState ->
            val updatedState = currentState.copy(selectedSplitWithUsers = users)
            // Recalculate splits when users change
            val (updatedPercentageSplits, updatedNumberSplits) = calculateEqualSplits(users, updatedState.expensePrice)
            updatedState.copy(
                percentageSplits = updatedPercentageSplits,
                numberSplits = updatedNumberSplits,
            )
        }
    }

    private fun updateSplitMethod(method: SplitMethod) {
        _formState.update { currentState ->
            val updatedState = currentState.copy(splitMethod = method)
            // Recalculate splits when method changes
            val (updatedPercentageSplits, updatedNumberSplits) =
                calculateEqualSplits(
                    updatedState.selectedSplitWithUsers,
                    updatedState.expensePrice,
                )
            updatedState.copy(
                percentageSplits = updatedPercentageSplits,
                numberSplits = updatedNumberSplits,
            )
        }
    }

    private fun updatePercentageSplits(splits: Map<String, Float>) {
        _formState.update { currentState ->
            // Update percentage splits and recalculate number splits
            val totalExpense = currentState.expensePrice.toDoubleOrNull() ?: 0.0
            val updatedNumberSplits =
                if (totalExpense > 0) {
                    splits.mapValues { (_, percentage) ->
                        ((percentage / 100f) * totalExpense).toFloat()
                    }
                } else {
                    currentState.numberSplits
                }

            currentState.copy(
                percentageSplits = splits,
                numberSplits = updatedNumberSplits,
            )
        }
    }

    private fun updateNumberSplits(splits: Map<String, Float>) {
        _formState.update { currentState ->
            // Update number splits and recalculate percentage splits
            val totalExpense = currentState.expensePrice.toDoubleOrNull() ?: 0.0
            val updatedPercentageSplits =
                if (totalExpense > 0) {
                    splits.mapValues { (_, amount) ->
                        ((amount / totalExpense) * 100f).toFloat()
                    }
                } else {
                    currentState.percentageSplits
                }

            currentState.copy(
                numberSplits = splits,
                percentageSplits = updatedPercentageSplits,
            )
        }
    }

    private fun validateForm(state: AddExpenseFormState): Boolean =
        state.isNameValid &&
            state.isPriceValid &&
            state.isCategoryValid &&
            state.isPaidByValid

    private fun calculateEqualSplits(
        users: List<User>,
        priceString: String,
    ): Pair<Map<String, Float>, Map<String, Float>> {
        if (users.isEmpty()) return Pair(emptyMap(), emptyMap())

        val totalExpense = priceString.toDoubleOrNull() ?: 0.0
        val equalPercentage = 100f / users.size
        val equalAmount = if (totalExpense > 0) (totalExpense / users.size).toFloat() else 0f

        val percentageSplits = users.associate { it.userId to equalPercentage }
        val numberSplits = users.associate { it.userId to equalAmount }

        return Pair(percentageSplits, numberSplits)
    }

    private fun balancePercentageSplits(
        userId: String,
        percentage: Float,
        currentSplits: Map<String, Float>,
        allUsers: List<User>,
    ): Map<String, Float> {
        val updatedSplits = currentSplits + (userId to percentage)

        val remainingPercentage = 100f - percentage
        val otherUsers = allUsers.filter { it.userId != userId }

        return if (otherUsers.isNotEmpty()) {
            val equalShare = remainingPercentage / otherUsers.size
            updatedSplits + otherUsers.associate { it.userId to equalShare }
        } else {
            updatedSplits
        }
    }

    private fun balanceNumberSplits(
        userId: String,
        amount: Float,
        currentSplits: Map<String, Float>,
        allUsers: List<User>,
        totalExpense: Double,
    ): Map<String, Float> {
        val updatedSplits = currentSplits + (userId to amount)

        val remainingAmount = totalExpense.toFloat() - amount
        val otherUsers = allUsers.filter { it.userId != userId }

        return if (otherUsers.isNotEmpty() && remainingAmount > 0) {
            val equalShare = remainingAmount / otherUsers.size
            updatedSplits + otherUsers.associate { it.userId to equalShare }
        } else {
            updatedSplits
        }
    }

    private fun updateSinglePercentage(
        userId: String,
        percentage: Float,
    ) {
        _formState.update { currentState ->
            val balancedSplits =
                balancePercentageSplits(
                    userId,
                    percentage,
                    currentState.percentageSplits,
                    currentState.selectedSplitWithUsers,
                )

            val totalExpense = currentState.expensePrice.toDoubleOrNull() ?: 0.0
            val updatedNumberSplits =
                if (totalExpense > 0) {
                    balancedSplits.mapValues { (_, percentageValue) ->
                        ((percentageValue / 100f) * totalExpense).toFloat()
                    }
                } else {
                    currentState.numberSplits
                }

            currentState.copy(
                percentageSplits = balancedSplits,
                numberSplits = updatedNumberSplits,
            )
        }
    }

    private fun updateSingleNumber(
        userId: String,
        amount: Float,
    ) {
        _formState.update { currentState ->
            val totalExpense = currentState.expensePrice.toDoubleOrNull() ?: 0.0
            val balancedSplits =
                balanceNumberSplits(
                    userId,
                    amount,
                    currentState.numberSplits,
                    currentState.selectedSplitWithUsers,
                    totalExpense,
                )

            val updatedPercentageSplits =
                if (totalExpense > 0) {
                    balancedSplits.mapValues { (_, amountValue) ->
                        ((amountValue / totalExpense) * 100f).toFloat()
                    }
                } else {
                    currentState.percentageSplits
                }

            currentState.copy(
                numberSplits = balancedSplits,
                percentageSplits = updatedPercentageSplits,
            )
        }
    }

    private fun resetToEqualSplits() {
        _formState.update { currentState ->
            val (updatedPercentageSplits, updatedNumberSplits) =
                calculateEqualSplits(
                    currentState.selectedSplitWithUsers,
                    currentState.expensePrice,
                )

            currentState.copy(
                percentageSplits = updatedPercentageSplits,
                numberSplits = updatedNumberSplits,
            )
        }
    }

    private fun removeUserFromSplit(user: User) {
        _formState.update { currentState ->
            // Only remove if not the payer
            if (user.userId != currentState.selectedPaidByUser?.userId) {
                val updatedUsers = currentState.selectedSplitWithUsers.filter { it.userId != user.userId }
                val (updatedPercentageSplits, updatedNumberSplits) = calculateEqualSplits(updatedUsers, currentState.expensePrice)

                currentState.copy(
                    selectedSplitWithUsers = updatedUsers,
                    percentageSplits = updatedPercentageSplits,
                    numberSplits = updatedNumberSplits,
                )
            } else {
                currentState
            }
        }
    }

    private fun loadCategories() {
        viewModelScope.launch {
            getCategoriesUseCase().fold(
                onSuccess = { categories ->
                    _categories.value = categories
                },
                onFailure = { exception ->
                    // Silently fail for categories loading, keep empty list
                    // Could add error state later if needed
                    _categories.value = emptyList()
                },
            )
        }
    }

    private fun dismissError() {
        _uiState.value = AddExpenseUiState.Idle
    }
}
