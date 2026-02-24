package com.marquis.zorroexpense.presentation.components

import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.runtime.Composable
import com.marquis.zorroexpense.domain.model.Group

/**
 * Cross-platform swipeable expense list card wrapper
 * Android: Implements SwipeToDismissBox with swipe gestures
 * iOS/Web: Falls back to simple card without swipe
 */
@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
expect fun SwipeableGroupCard(
    list: Group,
    sharedTransitionScope: SharedTransitionScope,
    animatedContentScope: AnimatedContentScope,
    onClick: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
)
