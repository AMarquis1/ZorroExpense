package com.marquis.zorroexpense.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.unit.dp
import com.marquis.zorroexpense.domain.model.Group
import com.marquis.zorroexpense.presentation.screens.ExpenseListCard
import kotlinx.coroutines.launch

/**
 * Android-specific implementation with SwipeToDismissBox
 * Swipe left (EndToStart) to delete
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
actual fun SwipeableGroupCard(
    list: Group,
    onClick: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
) {
    val coroutineScope = rememberCoroutineScope()
    val dismissState = rememberSwipeToDismissBoxState(
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
                else -> Unit
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
        else -> {}
    }
}
