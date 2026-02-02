package com.marquis.zorroexpense.domain.model

data class Expense(
    val documentId: String = "",
    val listId: String = "", // Reference to ExpenseList
    val description: String = "",
    val name: String = "",
    val price: Double = 0.0,
    val date: String = "",
    val category: Category = Category(),
    val paidBy: User = User(), // User object of who paid
    val splitDetails: List<SplitDetail> = emptyList(), // List of split details with users and amounts
    val isFromRecurring: Boolean = false, // True if this expense was created from a recurring pattern
    // Recurring expense fields
    val isRecurring: Boolean = false,
    val recurrenceType: RecurrenceType = RecurrenceType.NONE,
    val recurrenceDay: Int? = null, // Day of month for monthly recurrence (1-31)
    val nextOccurrenceDate: String? = null, // Next scheduled date for recurring expense
    val originalExpenseId: String? = null, // Links recurring instances to their template
    val isScheduled: Boolean = false, // True for future occurrences that haven't happened yet
    // Recurrence limit fields
    val recurrenceLimit: Int? = null, // Maximum number of occurrences (null = unlimited)
    val recurrenceCount: Int = 0, // How many times this expense has occurred
)
