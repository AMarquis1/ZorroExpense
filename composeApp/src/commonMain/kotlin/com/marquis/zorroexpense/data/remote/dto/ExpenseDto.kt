package com.marquis.zorroexpense.data.remote.dto

// Common interface for platform-specific ExpenseDto implementations
interface ExpenseDto {
    val documentId: String
    val description: String
    val name: String
    val price: Double
    val date: Any?
    val category: Any?
    val paidBy: Any?
    val splitWith: List<Any>
    val isFromRecurring: Boolean
}
