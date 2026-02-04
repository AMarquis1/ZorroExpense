package com.marquis.zorroexpense.presentation.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckBox
import androidx.compose.material.icons.filled.CheckBoxOutlineBlank
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.rememberSwipeToDismissBoxState

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.unit.dp
import com.marquis.zorroexpense.domain.model.ExpenseList
import com.marquis.zorroexpense.presentation.screens.ExpenseListCard
import kotlinx.coroutines.delay

/**
 * Android-specific implementation with SwipeToDismissBox
 * Swipe right (StartToEnd) to edit, swipe left (EndToStart) to delete
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
actual fun SwipeableExpenseListCard(
    list: ExpenseList,
    onClick: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
) {

    val swipeToDismissBoxState = rememberSwipeToDismissBoxState(
        confirmValueChange = {
            when (it) {
                SwipeToDismissBoxValue.StartToEnd -> {
                    onEdit()
                    false // Don't dismiss - keep card visible
                }
                SwipeToDismissBoxValue.EndToStart -> {
                    onDelete()
                    false // Don't dismiss - show confirmation dialog, ViewModel will remove from list
                }
                SwipeToDismissBoxValue.Settled -> false
            }
        }
    )

    // Reset swipe state when dismissed direction changes away from settled
    LaunchedEffect(swipeToDismissBoxState.currentValue) {
        if (swipeToDismissBoxState.currentValue != SwipeToDismissBoxValue.Settled) {
            // Wait a bit for the action to be processed, then reset
            delay(300)
            swipeToDismissBoxState.reset()
        }
    }

    SwipeToDismissBox(
        state = swipeToDismissBoxState,
        backgroundContent = {
            when (swipeToDismissBoxState.dismissDirection) {
                SwipeToDismissBoxValue.StartToEnd -> {
                    Icon(
                        Icons.Default.Edit,
                        contentDescription = "Edit",
                        modifier = Modifier
                            .fillMaxSize()
                            .drawBehind {
                                drawRect(lerp(Color.LightGray, Color.Blue, swipeToDismissBoxState.progress))
                            }
                            .wrapContentSize(Alignment.CenterStart)
                            .padding(12.dp),
                        tint = Color.White
                    )
                }
                SwipeToDismissBoxValue.EndToStart -> {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Remove item",
                        modifier = Modifier
                            .fillMaxSize()
                            .background(lerp(Color.LightGray, Color.Red, swipeToDismissBoxState.progress))
                            .wrapContentSize(Alignment.CenterEnd)
                            .padding(12.dp),
                        tint = Color.White
                    )
                }
                SwipeToDismissBoxValue.Settled -> {}
            }
        }
    ) {
        // Wrapped card content - reuse existing ExpenseListCard but without edit/delete buttons
        ExpenseListCard(
            list = list,
            onClick = onClick,
            onEdit = {}, // Swipe handles edit
            onDelete = {}, // Swipe handles delete
            isSwipeable = true, // Hide buttons when used with swipe
        )
    }
}
