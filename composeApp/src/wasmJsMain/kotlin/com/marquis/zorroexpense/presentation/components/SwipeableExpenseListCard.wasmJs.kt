package com.marquis.zorroexpense.presentation.components

import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.runtime.Composable
import com.marquis.zorroexpense.domain.model.Group
import com.marquis.zorroexpense.presentation.screens.ExpenseListCard

/**
 * WASM (Web) implementation - SwipeToDismissBox is not available on Web in Compose Multiplatform
 * Falls back to card with visible edit and delete buttons
 */
@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
actual fun SwipeableGroupCard(
    list: Group,
    sharedTransitionScope: SharedTransitionScope,
    animatedContentScope: AnimatedContentScope,
    onClick: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
) {
    ExpenseListCard(
        list = list,
        sharedTransitionScope = sharedTransitionScope,
        animatedContentScope = animatedContentScope,
        onClick = onClick,
        onDelete = onDelete,
        isSwipeable = false,
    )
}
