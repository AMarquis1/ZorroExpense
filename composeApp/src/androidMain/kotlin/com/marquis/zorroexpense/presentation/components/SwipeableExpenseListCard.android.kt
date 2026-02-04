package com.marquis.zorroexpense.presentation.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxDefaults
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.marquis.zorroexpense.domain.model.ExpenseList
import com.marquis.zorroexpense.presentation.screens.ExpenseListCard
import kotlinx.coroutines.launch

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
    val coroutineScope = rememberCoroutineScope()
    val dismissState = rememberSwipeToDismissBoxState(
        // Trigger at 50% - synced with color lerp midpoint
        positionalThreshold = { totalDistance -> totalDistance * 0.5f },
    )

    SwipeToDismissBox(
        state = dismissState,
        onDismiss = { dismissValue ->
            when (dismissValue) {
                SwipeToDismissBoxValue.StartToEnd -> {
                    coroutineScope.launch {
                        dismissState.reset()
                        onEdit()
                    }
                }
                SwipeToDismissBoxValue.EndToStart -> {
                    coroutineScope.launch {
                        dismissState.reset()
                        onDelete()
                    }
                }
                SwipeToDismissBoxValue.Settled -> Unit
            }
        },
        backgroundContent = {
            SwipeBackground(
                dismissDirection = dismissState.dismissDirection,
                progress = dismissState.progress,
            )
        },
    ) {
        ExpenseListCard(
            list = list,
            onClick = onClick,
            onEdit = {},
            onDelete = {},
            isSwipeable = true,
        )
    }
}

@Composable
private fun SwipeBackground(
    dismissDirection: SwipeToDismissBoxValue,
    progress: Float,
) {
    when (dismissDirection) {
        SwipeToDismissBoxValue.StartToEnd -> {
            Icon(
                Icons.Default.Edit,
                contentDescription = "Edit",
                modifier = Modifier
                    .fillMaxSize()
                    .drawBehind {
                        drawRect(lerp(Color.Yellow,Color.LightGray,progress))
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
                    .background(lerp(Color.Red, Color.LightGray, progress))
                    .wrapContentSize(Alignment.CenterEnd)
                    .padding(12.dp),
                tint = Color.White
            )
        }
        SwipeToDismissBoxValue.Settled -> {}
    }
}
