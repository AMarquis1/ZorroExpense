package com.marquis.zorroexpense.data.remote.dto

import com.marquis.zorroexpense.domain.model.Expense

/**
 * Extension function to convert Expense domain model to ExpenseDto for Firestore
 * This is a common interface - platform-specific implementations will handle the actual mapping
 */
expect fun Expense.toDto(): ExpenseDto
