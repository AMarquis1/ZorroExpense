package com.marquis.zorroexpense.presentation.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

/**
 * iOS implementation of Speed Dial FAB using custom component.
 *
 * Uses custom animations for better compatibility across iOS versions.
 */
@Composable
actual fun SpeedDialFab(
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    items: List<SpeedDialFabItem>,
    mainIcon: ImageVector,
    mainLabel: String,
    fabExpanded: Boolean,
) {
    CustomSpeedDialFab(
        expanded = expanded,
        onExpandedChange = onExpandedChange,
        items = items,
        mainIcon = mainIcon,
        mainLabel = mainLabel,
        fabExpanded = fabExpanded,
    )
}

/**
 * Custom Speed Dial FAB component with Material Design 3 pattern.
 *
 * Features:
 * - Main FAB rotates 45° when expanded (+ becomes ×)
 * - Mini FABs slide up with stagger animation
 * - Semi-transparent scrim overlay appears behind to dismiss menu
 * - Automatically closes when scrolling
 */
@Composable
private fun CustomSpeedDialFab(
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    items: List<SpeedDialFabItem>,
    mainIcon: ImageVector,
    mainLabel: String,
    fabExpanded: Boolean,
) {
    // Animate main FAB rotation
    val rotationDegrees by animateFloatAsState(
        targetValue = if (expanded) 45f else 0f,
        animationSpec = tween(300, easing = FastOutSlowInEasing),
        label = "fab_rotation",
    )

    Box {
        // Scrim overlay - clickable to dismiss
        if (expanded) {
            Box(
                modifier =
                    Modifier
                        .matchParentSize()
                        .background(Color.Black.copy(alpha = 0.3f))
                        .clickable(
                            interactionSource = MutableInteractionSource(),
                            indication = null,
                            onClick = { onExpandedChange(false) },
                        ),
            )
        }

        // Stack of mini FABs and main FAB
        Column(
            modifier = Modifier.align(Alignment.BottomEnd),
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            // Mini FABs (display above main FAB)
            items.forEachIndexed { index, item ->
                AnimatedVisibility(
                    visible = expanded,
                    enter =
                        fadeIn(tween(200, delayMillis = index * 50)) +
                            slideInVertically(
                                initialOffsetY = { 40 },
                                animationSpec = tween(200, delayMillis = index * 50),
                            ),
                    exit =
                        fadeOut(tween(150)) +
                            slideOutVertically(
                                targetOffsetY = { 40 },
                                animationSpec = tween(150),
                            ),
                ) {
                    SmallFloatingActionButton(
                        onClick = {
                            item.onClick()
                            onExpandedChange(false)
                        },
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                        elevation =
                            FloatingActionButtonDefaults.elevation(
                                defaultElevation = 8.dp,
                                pressedElevation = 12.dp,
                            ),
                    ) {
                        Icon(
                            imageVector = item.icon,
                            contentDescription = item.contentDescription,
                            modifier = Modifier.size(20.dp),
                        )
                    }
                }
            }

            // Main FAB with rotation animation
            ExtendedFloatingActionButton(
                onClick = { onExpandedChange(!expanded) },
                expanded = fabExpanded,
                icon = {
                    Icon(
                        imageVector = mainIcon,
                        contentDescription = "Toggle menu",
                        modifier = Modifier.graphicsLayer { rotationZ = rotationDegrees },
                    )
                },
                text = {
                    if (fabExpanded && !expanded) {
                        Text("Add")
                    }
                },
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                elevation =
                    FloatingActionButtonDefaults.elevation(
                        defaultElevation = 8.dp,
                        pressedElevation = 16.dp,
                    ),
            )
        }
    }
}
