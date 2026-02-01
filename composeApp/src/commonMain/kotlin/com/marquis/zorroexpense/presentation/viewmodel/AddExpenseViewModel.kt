package com.marquis.zorroexpense.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.marquis.zorroexpense.domain.model.Category
import com.marquis.zorroexpense.domain.model.Expense
import com.marquis.zorroexpense.domain.model.RecurrenceType
import com.marquis.zorroexpense.domain.model.SplitDetail
import com.marquis.zorroexpense.domain.model.SplitMethod
import com.marquis.zorroexpense.domain.model.User
import com.marquis.zorroexpense.domain.usecase.AddExpenseUseCase
import com.marquis.zorroexpense.domain.usecase.GetCategoriesUseCase
import com.marquis.zorroexpense.domain.usecase.UpdateExpenseUseCase
import com.marquis.zorroexpense.presentation.state.AddExpenseFormState
import com.marquis.zorroexpense.presentation.state.AddExpenseUiEvent
import com.marquis.zorroexpense.presentation.state.AddExpenseUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.number
import kotlinx.datetime.plus

class AddExpenseViewModel(
    private val userId: String,
    private val listId: String,
    private val addExpenseUseCase: AddExpenseUseCase,
    private val updateExpenseUseCase: UpdateExpenseUseCase,
    private val getCategoriesUseCase: GetCategoriesUseCase,
    private val expenseToEdit: Expense? = null,
) : ViewModel() {
    private val _uiState = MutableStateFlow<AddExpenseUiState>(AddExpenseUiState.Idle)
    val uiState: StateFlow<AddExpenseUiState> = _uiState.asStateFlow()

    private val _formState = MutableStateFlow(AddExpenseFormState())
    val formState: StateFlow<AddExpenseFormState> = _formState.asStateFlow()

    private val _categories = MutableStateFlow<List<Category>>(emptyList())
    val categories: StateFlow<List<Category>> = _categories.asStateFlow()

    /** Whether the ViewModel is in edit mode (editing existing expense) or add mode (creating new) */
    val isEditMode: Boolean = expenseToEdit != null

    /** The document ID of the expense being edited, if in edit mode */
    private val editExpenseDocumentId: String? = expenseToEdit?.documentId

    init {
        loadCategories()
        expenseToEdit?.let { initializeFormForEdit(it) }
    }

    /**
     * Initialize the form with values from an existing expense for editing
     */
    private fun initializeFormForEdit(expense: Expense) {
        // Build split maps from existing split details
        val percentageSplits = mutableMapOf<String, Float>()
        val numberSplits = mutableMapOf<String, Float>()

        val totalPrice = expense.price
        expense.splitDetails.forEach { splitDetail ->
            val userId = splitDetail.user.userId
            numberSplits[userId] = splitDetail.amount.toFloat()
            if (totalPrice > 0) {
                percentageSplits[userId] = ((splitDetail.amount / totalPrice) * 100).toFloat()
            }
        }

        _formState.update {
            AddExpenseFormState(
                expenseName = expense.name,
                expenseDescription = expense.description,
                expensePrice = expense.price.toString(),
                selectedCategory = expense.category,
                selectedPaidByUser = expense.paidBy,
                selectedSplitWithUsers = expense.splitDetails.map { it.user },
                selectedDate = expense.date,
                percentageSplits = percentageSplits,
                numberSplits = numberSplits,
                splitMethod = SplitMethod.NUMBER, // Default to number since we have actual amounts
                isNameValid = true,
                isPriceValid = true,
                isCategoryValid = true,
                isPaidByValid = true,
                isFormValid = true,
                // Don't copy recurring settings - those are for new expenses only
                isRecurring = false,
                recurrenceType = RecurrenceType.NONE,
                recurrenceDay = null,
                recurrenceLimit = null,
                futureOccurrences = emptyList(),
            )
        }
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
            is AddExpenseUiEvent.DateChanged -> updateDate(event.date)
            is AddExpenseUiEvent.RecurringToggled -> updateRecurring(event.isRecurring)
            is AddExpenseUiEvent.RecurrenceTypeChanged -> updateRecurrenceType(event.type)
            is AddExpenseUiEvent.RecurrenceLimitChanged -> updateRecurrenceLimit(event.limit)
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

                // Recalculate splits when price changes
                val (updatedPercentageSplits, updatedNumberSplits) =
                    calculateEqualSplits(
                        currentState.selectedSplitWithUsers,
                        price,
                    )

                // Create state with new price AND new splits before validating
                val updatedState =
                    currentState.copy(
                        expensePrice = price,
                        isPriceValid = isPriceValid,
                        percentageSplits = updatedPercentageSplits,
                        numberSplits = updatedNumberSplits,
                    )

                // Validate with the fully updated state (including new splits)
                updatedState.copy(
                    isFormValid = validateForm(updatedState),
                )
            }
        }
    }

    private fun saveExpense() {
        if (isEditMode) {
            updateExpense()
        } else {
            addNewExpense()
        }
    }

    /**
     * Update an existing expense (edit mode)
     */
    private fun updateExpense() {
        val currentFormState = _formState.value

        if (!currentFormState.isFormValid) {
            _uiState.value = AddExpenseUiState.Error("Please fill in all required fields correctly")
            return
        }

        viewModelScope.launch {
            _uiState.value = AddExpenseUiState.Loading

            try {
                val splitDetails = createSplitDetails(currentFormState)
                val updatedExpense = Expense(
                    documentId = editExpenseDocumentId ?: "",
                    name = currentFormState.expenseName.trim(),
                    description = currentFormState.expenseDescription.trim(),
                    price = currentFormState.expensePrice.toDouble(),
                    date = currentFormState.selectedDate,
                    category = requireNotNull(currentFormState.selectedCategory) { "Category must be selected" },
                    paidBy = requireNotNull(currentFormState.selectedPaidByUser) { "Paid by user must be selected" },
                    splitDetails = splitDetails,
                    isFromRecurring = false,
                    isRecurring = false,
                    recurrenceType = RecurrenceType.NONE,
                    recurrenceDay = null,
                    nextOccurrenceDate = null,
                    isScheduled = false,
                    recurrenceLimit = null,
                    recurrenceCount = 0,
                )

                updateExpenseUseCase(userId, updatedExpense)
                    .onSuccess {
                        _uiState.value = AddExpenseUiState.Success(listOf(updatedExpense))
                    }
                    .onFailure { exception ->
                        _uiState.value = AddExpenseUiState.Error(
                            message = "Failed to update expense: ${exception.message ?: "Unknown error"}"
                        )
                    }
            } catch (e: Exception) {
                _uiState.value = AddExpenseUiState.Error(
                    message = e.message ?: "An unexpected error occurred"
                )
            }
        }
    }

    /**
     * Add a new expense (add mode)
     */
    private fun addNewExpense() {
        val currentFormState = _formState.value

        if (!currentFormState.isFormValid) {
            _uiState.value = AddExpenseUiState.Error("Please fill in all required fields correctly")
            return
        }

        viewModelScope.launch {
            _uiState.value = AddExpenseUiState.Loading

            try {
                // Generate all expense dates based on recurrence settings
                val expenseDates =
                    if (currentFormState.isRecurring && currentFormState.recurrenceLimit != null) {
                        generateAllRecurringDates(
                            startDate = currentFormState.selectedDate,
                            recurrenceType = currentFormState.recurrenceType,
                            recurrenceDay = currentFormState.recurrenceDay,
                            recurrenceLimit = currentFormState.recurrenceLimit,
                        )
                    } else {
                        listOf(currentFormState.selectedDate)
                    }

                // Create split details based on custom amounts or equal split
                val splitDetails = createSplitDetails(currentFormState)

                // Create expense objects for each date (without recurrence metadata)
                val expenses =
                    expenseDates.map { date ->
                        Expense(
                            name = currentFormState.expenseName.trim(),
                            description = currentFormState.expenseDescription.trim(),
                            price = currentFormState.expensePrice.toDouble(),
                            date = date,
                            category = requireNotNull(currentFormState.selectedCategory) { "Category must be selected" },
                            paidBy = requireNotNull(currentFormState.selectedPaidByUser) { "Paid by user must be selected" },
                            splitDetails = splitDetails,
                            isFromRecurring = currentFormState.isRecurring && expenseDates.size > 1,
                            listId = this@AddExpenseViewModel.listId,
                            // Remove recurrence metadata - each expense is standalone
                            isRecurring = false,
                            recurrenceType = RecurrenceType.NONE,
                            recurrenceDay = null,
                            nextOccurrenceDate = null,
                            isScheduled = false,
                            recurrenceLimit = null,
                            recurrenceCount = 0,
                        )
                    }

                // Save all expenses and track saved ones with temp documentIds
                val savedExpenses = mutableListOf<Expense>()
                for ((index, expense) in expenses.withIndex()) {
                    addExpenseUseCase(userId, expense)
                        .onSuccess {
                            // Add with a temporary documentId for local display
                            val tempDocId = "temp_${Clock.System.now().toEpochMilliseconds()}_$index"
                            savedExpenses.add(expense.copy(documentId = tempDocId))
                        }.onFailure { exception ->
                            _uiState.value =
                                AddExpenseUiState.Error(
                                    message = "Failed to save expense ${savedExpenses.size + 1} of ${expenses.size}: ${exception.message ?: "Unknown error"}",
                                )
                            return@launch
                        }
                }

                // All expenses saved successfully
                if (savedExpenses.size == expenses.size) {
                    _uiState.value = AddExpenseUiState.Success(savedExpenses)
                    // Note: Don't call resetForm() here - let the screen handle navigation first
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
            state.isPaidByValid &&
            validateSplitAmounts(state)

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
            val updatedUsers = currentState.selectedSplitWithUsers.filter { it.userId != user.userId }
            val (updatedPercentageSplits, updatedNumberSplits) = calculateEqualSplits(updatedUsers, currentState.expensePrice)

            currentState.copy(
                selectedSplitWithUsers = updatedUsers,
                percentageSplits = updatedPercentageSplits,
                numberSplits = updatedNumberSplits,
            )
        }
    }

    private fun updateDate(date: String) {
        _formState.update { currentState ->
            // Update recurrence day if recurring and type is monthly
            val newRecurrenceDay =
                if (currentState.isRecurring && currentState.recurrenceType == RecurrenceType.MONTHLY) {
                    try {
                        LocalDate.parse(date).day
                    } catch (e: Exception) {
                        currentState.recurrenceDay ?: 1
                    }
                } else {
                    currentState.recurrenceDay
                }

            val updatedState =
                currentState.copy(
                    selectedDate = date,
                    recurrenceDay = newRecurrenceDay,
                )

            // Recalculate future occurrences if recurring is enabled
            val futureOccurrences =
                if (updatedState.isRecurring) {
                    calculateFutureOccurrences(
                        date,
                        updatedState.recurrenceType,
                        updatedState.recurrenceDay,
                        updatedState.recurrenceLimit,
                    )
                } else {
                    emptyList()
                }

            updatedState.copy(futureOccurrences = futureOccurrences)
        }
    }

    private fun updateRecurring(isRecurring: Boolean) {
        _formState.update { currentState ->
            // Extract day of month from selected date when enabling recurring
            val dayOfMonth =
                if (isRecurring) {
                    try {
                        LocalDate.parse(currentState.selectedDate).day
                    } catch (e: Exception) {
                        1
                    }
                } else {
                    null
                }

            val updatedState =
                currentState.copy(
                    isRecurring = isRecurring,
                    recurrenceType = if (isRecurring) RecurrenceType.MONTHLY else RecurrenceType.NONE,
                    recurrenceDay = dayOfMonth,
                    recurrenceLimit = if (isRecurring && currentState.recurrenceLimit == null) 6 else currentState.recurrenceLimit,
                )

            // Calculate future occurrences if recurring is enabled
            val futureOccurrences =
                if (isRecurring) {
                    calculateFutureOccurrences(
                        updatedState.selectedDate,
                        updatedState.recurrenceType,
                        updatedState.recurrenceDay,
                        updatedState.recurrenceLimit,
                    )
                } else {
                    emptyList()
                }

            updatedState.copy(futureOccurrences = futureOccurrences)
        }
    }

    private fun updateRecurrenceType(type: RecurrenceType) {
        _formState.update { currentState ->
            val updatedState =
                currentState.copy(
                    recurrenceType = type,
                    recurrenceDay = if (type == RecurrenceType.MONTHLY) 1 else null,
                )

            // Recalculate future occurrences
            val futureOccurrences =
                if (currentState.isRecurring) {
                    calculateFutureOccurrences(
                        updatedState.selectedDate,
                        type,
                        updatedState.recurrenceDay,
                        updatedState.recurrenceLimit,
                    )
                } else {
                    emptyList()
                }

            updatedState.copy(futureOccurrences = futureOccurrences)
        }
    }

    private fun updateRecurrenceLimit(limit: Int?) {
        _formState.update { currentState ->
            val updatedState = currentState.copy(recurrenceLimit = limit)

            // Recalculate future occurrences
            val futureOccurrences =
                if (currentState.isRecurring) {
                    calculateFutureOccurrences(
                        updatedState.selectedDate,
                        updatedState.recurrenceType,
                        updatedState.recurrenceDay,
                        limit,
                    )
                } else {
                    emptyList()
                }

            updatedState.copy(futureOccurrences = futureOccurrences)
        }
    }

    private fun getDaysInMonth(
        year: Int,
        month: Int,
    ): Int =
        when (month) {
            1, 3, 5, 7, 8, 10, 12 -> 31
            4, 6, 9, 11 -> 30
            2 -> if (year % 4 == 0 && (year % 100 != 0 || year % 400 == 0)) 29 else 28
            else -> 31
        }

    private fun calculateNextOccurrence(
        currentDate: String,
        recurrenceType: RecurrenceType,
        recurrenceDay: Int?,
    ): String? =
        try {
            val date = LocalDate.parse(currentDate)
            when (recurrenceType) {
                RecurrenceType.DAILY -> date.plus(1, DateTimeUnit.DAY).toString()
                RecurrenceType.WEEKLY -> date.plus(1, DateTimeUnit.WEEK).toString()
                RecurrenceType.MONTHLY -> {
                    val nextMonth = date.plus(1, DateTimeUnit.MONTH)
                    val dayToUse =
                        recurrenceDay?.coerceAtMost(
                            getDaysInMonth(nextMonth.year, nextMonth.month.number),
                        ) ?: 1
                    try {
                        LocalDate(nextMonth.year, nextMonth.month, dayToUse).toString()
                    } catch (_: Exception) {
                        nextMonth.toString()
                    }
                }
                RecurrenceType.YEARLY -> date.plus(1, DateTimeUnit.YEAR).toString()
                RecurrenceType.NONE -> null
            }
        } catch (e: Exception) {
            null
        }

    /**
     * Generate all dates for recurring expenses including the initial date
     */
    private fun generateAllRecurringDates(
        startDate: String,
        recurrenceType: RecurrenceType,
        recurrenceDay: Int?,
        recurrenceLimit: Int,
    ): List<String> {
        if (recurrenceType == RecurrenceType.NONE || recurrenceLimit <= 0) return emptyList()

        return try {
            val date = LocalDate.parse(startDate)
            val allDates = mutableListOf<String>()
            var currentDate = date

            // Add the initial date as the first occurrence
            allDates.add(currentDate.toString())

            // Generate remaining occurrences
            repeat(recurrenceLimit - 1) {
                currentDate =
                    when (recurrenceType) {
                        RecurrenceType.DAILY -> currentDate.plus(1, DateTimeUnit.DAY)
                        RecurrenceType.WEEKLY -> currentDate.plus(1, DateTimeUnit.WEEK)
                        RecurrenceType.MONTHLY -> {
                            val nextMonth = currentDate.plus(1, DateTimeUnit.MONTH)
                            val dayToUse =
                                recurrenceDay?.coerceAtMost(
                                    getDaysInMonth(nextMonth.year, nextMonth.month.number),
                                ) ?: currentDate.day
                            try {
                                LocalDate(nextMonth.year, nextMonth.month, dayToUse)
                            } catch (_: Exception) {
                                nextMonth
                            }
                        }
                        RecurrenceType.YEARLY -> currentDate.plus(1, DateTimeUnit.YEAR)
                        RecurrenceType.NONE -> currentDate // This shouldn't happen but added for completeness
                    }
                allDates.add(currentDate.toString())
            }

            allDates
        } catch (e: Exception) {
            emptyList()
        }
    }

    private fun calculateFutureOccurrences(
        startDate: String,
        recurrenceType: RecurrenceType,
        recurrenceDay: Int?,
        recurrenceLimit: Int? = null,
        count: Int = 5,
    ): List<String> {
        if (recurrenceType == RecurrenceType.NONE) return emptyList()

        return try {
            val date = LocalDate.parse(startDate)
            val occurrences = mutableListOf<String>()
            var currentDate = date

            // Determine how many occurrences to show for preview
            val maxOccurrences =
                when {
                    recurrenceLimit != null -> minOf(recurrenceLimit - 1, count) // Subtract 1 because the first occurrence is already happening
                    else -> count
                }

            if (maxOccurrences <= 0) return emptyList()

            repeat(maxOccurrences) {
                currentDate =
                    when (recurrenceType) {
                        RecurrenceType.DAILY -> currentDate.plus(1, DateTimeUnit.DAY)
                        RecurrenceType.WEEKLY -> currentDate.plus(1, DateTimeUnit.WEEK)
                        RecurrenceType.MONTHLY -> {
                            val nextMonth = currentDate.plus(1, DateTimeUnit.MONTH)
                            val dayToUse =
                                recurrenceDay?.coerceAtMost(
                                    getDaysInMonth(nextMonth.year, nextMonth.month.number),
                                ) ?: 1
                            try {
                                LocalDate(nextMonth.year, nextMonth.month, dayToUse)
                            } catch (_: Exception) {
                                nextMonth
                            }
                        }
                        RecurrenceType.YEARLY -> currentDate.plus(1, DateTimeUnit.YEAR)
                        RecurrenceType.NONE -> return emptyList()
                    }
                occurrences.add(currentDate.toString())
            }

            occurrences
        } catch (e: Exception) {
            emptyList()
        }
    }

    private fun createSplitDetails(formState: AddExpenseFormState): List<SplitDetail> {
        return when (formState.splitMethod) {
            SplitMethod.NUMBER -> {
                // Use number splits (custom amounts)
                formState.selectedSplitWithUsers.mapNotNull { user ->
                    val amount = formState.numberSplits[user.userId]?.toDouble()
                    if (amount != null && amount > 0) {
                        SplitDetail(user = user, amount = amount)
                    } else {
                        null
                    }
                }
            }
            SplitMethod.PERCENTAGE -> {
                // Use percentage splits
                val totalPrice = formState.expensePrice.toDoubleOrNull() ?: 0.0
                formState.selectedSplitWithUsers.mapNotNull { user ->
                    val percentage = formState.percentageSplits[user.userId]?.toDouble()
                    if (percentage != null && percentage > 0) {
                        val amount = (totalPrice * percentage) / 100.0
                        SplitDetail(user = user, amount = amount)
                    } else {
                        null
                    }
                }
            }
        }
    }

    private fun validateSplitAmounts(formState: AddExpenseFormState): Boolean {
        // If no users are selected for splitting, consider it valid (form not complete yet)
        if (formState.selectedSplitWithUsers.isEmpty()) {
            return true
        }
        
        return when (formState.splitMethod) {
            SplitMethod.NUMBER -> {
                val totalPrice = formState.expensePrice.toDoubleOrNull() ?: return false
                val totalSplitAmount = formState.numberSplits.values.sum().toDouble()
                
                // If no split amounts are set yet, consider it valid (equal splits will be calculated)
                if (formState.numberSplits.isEmpty()) return true
                
                // Allow small rounding differences (within 0.01)
                kotlin.math.abs(totalPrice - totalSplitAmount) < 0.01
            }
            SplitMethod.PERCENTAGE -> {
                val totalPercentage = formState.percentageSplits.values.sum()
                
                // If no split percentages are set yet, consider it valid (equal splits will be calculated)
                if (formState.percentageSplits.isEmpty()) return true
                
                // Allow small rounding differences (within 0.01%)
                kotlin.math.abs(totalPercentage - 100.0f) < 0.01f
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
