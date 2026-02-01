package com.marquis.zorroexpense.data.repository

import com.marquis.zorroexpense.data.remote.dto.ExpenseListDto
import com.marquis.zorroexpense.data.remote.dto.WasmExpenseListDto
import com.marquis.zorroexpense.domain.model.ExpenseList

actual fun ExpenseList.toDto(): ExpenseListDto =
    WasmExpenseListDto(
        listId = listId,
        name = name,
        createdBy = createdBy,
        memberIds = members,
        shareCode = shareCode,
        createdAt = createdAt,
        isArchived = isArchived,
        categoryIds = categories.map { it.documentId }
    )
