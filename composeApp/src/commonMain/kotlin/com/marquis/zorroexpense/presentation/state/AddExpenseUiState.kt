package com.marquis.zorroexpense.presentation.state

import com.marquis.zorroexpense.domain.model.Category
import com.marquis.zorroexpense.domain.model.RecurrenceType
import com.marquis.zorroexpense.domain.model.SplitMethod
import com.marquis.zorroexpense.domain.model.User
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.ExperimentalTime

sealed class AddExpenseUiState {
    object Idle : AddExpenseUiState()

    object Loading : AddExpenseUiState()

    data class Success(
        val savedExpenses: List<com.marquis.zorroexpense.domain.model.Expense>,
    ) : AddExpenseUiState()

    data class Error(
        val message: String,
    ) : AddExpenseUiState()
}

@OptIn(ExperimentalTime::class)
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
    val isFormValid: Boolean = false,
    // Date and recurring fields
    val selectedDate: String =
        kotlin.time.Clock.System
            .now()
            .toLocalDateTime(TimeZone.currentSystemDefault())
            .date
            .toString(),
    val isRecurring: Boolean = false,
    val recurrenceType: RecurrenceType = RecurrenceType.NONE,
    val recurrenceDay: Int? = null,
    val futureOccurrences: List<String> = emptyList(), // List of future occurrence dates for preview
    // Recurrence limit field (always required when recurring is enabled)
    val recurrenceLimit: Int? = null, // Maximum number of occurrences (required for recurring expenses)
)
