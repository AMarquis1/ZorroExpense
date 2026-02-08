package com.marquis.zorroexpense.presentation.components

import androidx.compose.runtime.Composable
import com.marquis.zorroexpense.domain.model.Group

/**
 * Cross-platform swipeable expense list card wrapper
 * Android: Implements SwipeToDismissBox with swipe gestures
 * iOS/Web: Falls back to simple card without swipe
 */
@Composable
expect fun SwipeableExpenseListCard(
    list: Group,
    onClick: () -> Unit,
    onDelete: () -> Unit,
)
