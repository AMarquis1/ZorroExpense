package com.marquis.zorroexpense.presentation.components

import androidx.compose.runtime.Composable
import com.marquis.zorroexpense.domain.model.Group
import com.marquis.zorroexpense.presentation.screens.ExpenseListCard

/**
 * iOS implementation - SwipeToDismissBox is not available on iOS in Compose Multiplatform
 * Falls back to card with visible edit and delete buttons
 */
@Composable
actual fun SwipeableGroupCard(
    list: Group,
    onClick: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
) {
    ExpenseListCard(
        list = list,
        onClick = onClick,
        onDelete = onDelete,
        isSwipeable = false,
    )
}
