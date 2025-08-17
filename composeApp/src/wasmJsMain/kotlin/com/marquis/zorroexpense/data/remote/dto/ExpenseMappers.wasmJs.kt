package com.marquis.zorroexpense.data.remote.dto

import com.marquis.zorroexpense.domain.model.Expense

/**
 * WASM-specific implementation to convert Expense domain model to WasmExpenseDto
 */
actual fun Expense.toDto(): ExpenseDto {
    return WasmExpenseDto(
        documentId = this.documentId,
        name = this.name,
        description = this.description,
        price = this.price,
        date = this.date, // WASM uses string dates directly
        categoryId = this.category.documentId,
        paidById = this.paidBy.userId,
        splitDetailsDto = this.splitDetails.map { splitDetail ->
            WasmSplitDetailDto(
                userId = splitDetail.user.userId,
                amount = splitDetail.amount
            )
        },
    )
}
