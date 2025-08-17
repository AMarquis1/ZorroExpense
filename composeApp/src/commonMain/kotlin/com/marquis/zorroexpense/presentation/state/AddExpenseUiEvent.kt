package com.marquis.zorroexpense.presentation.state

import com.marquis.zorroexpense.domain.model.Category
import com.marquis.zorroexpense.domain.model.RecurrenceType
import com.marquis.zorroexpense.domain.model.SplitMethod
import com.marquis.zorroexpense.domain.model.User

sealed class AddExpenseUiEvent {
    data class NameChanged(
        val name: String,
    ) : AddExpenseUiEvent()

    data class DescriptionChanged(
        val description: String,
    ) : AddExpenseUiEvent()

    data class PriceChanged(
        val price: String,
    ) : AddExpenseUiEvent()

    data class CategoryChanged(
        val category: Category,
    ) : AddExpenseUiEvent()

    data class PaidByChanged(
        val user: User,
    ) : AddExpenseUiEvent()

    data class SplitWithChanged(
        val users: List<User>,
    ) : AddExpenseUiEvent()

    data class SplitMethodChanged(
        val method: SplitMethod,
    ) : AddExpenseUiEvent()

    data class PercentageSplitsChanged(
        val splits: Map<String, Float>,
    ) : AddExpenseUiEvent()

    data class NumberSplitsChanged(
        val splits: Map<String, Float>,
    ) : AddExpenseUiEvent()

    // Advanced split calculation events
    data class PercentageChanged(
        val userId: String,
        val percentage: Float,
    ) : AddExpenseUiEvent()

    data class NumberChanged(
        val userId: String,
        val amount: Float,
    ) : AddExpenseUiEvent()

    object ResetToEqualSplits : AddExpenseUiEvent()

    data class RemoveUserFromSplit(
        val user: User,
    ) : AddExpenseUiEvent()

    object SaveExpense : AddExpenseUiEvent()

    object ResetForm : AddExpenseUiEvent()

    object DismissError : AddExpenseUiEvent()

    // Date and recurring events
    data class DateChanged(
        val date: String,
    ) : AddExpenseUiEvent()

    data class RecurringToggled(
        val isRecurring: Boolean,
    ) : AddExpenseUiEvent()

    data class RecurrenceTypeChanged(
        val type: RecurrenceType,
    ) : AddExpenseUiEvent()

    // Recurrence limit event
    data class RecurrenceLimitChanged(
        val limit: Int?,
    ) : AddExpenseUiEvent()
}
