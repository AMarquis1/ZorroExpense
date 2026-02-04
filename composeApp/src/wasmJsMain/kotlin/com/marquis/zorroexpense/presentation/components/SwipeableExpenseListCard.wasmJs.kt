package com.marquis.zorroexpense.presentation.components

import androidx.compose.runtime.Composable
import com.marquis.zorroexpense.domain.model.ExpenseList
import com.marquis.zorroexpense.presentation.screens.ExpenseListCard

/**
 * WASM (Web) implementation - SwipeToDismissBox is not available on Web in Compose Multiplatform
 * Falls back to card with visible edit and delete buttons
 */
@Composable
actual fun SwipeableExpenseListCard(
    list: ExpenseList,
    onClick: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
) {
    ExpenseListCard(
        list = list,
        onClick = onClick,
        onEdit = onEdit,
        onDelete = onDelete,
        isSwipeable = false,
    )
}
