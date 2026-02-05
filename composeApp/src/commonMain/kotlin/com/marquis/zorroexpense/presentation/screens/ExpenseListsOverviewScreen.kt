package com.marquis.zorroexpense.presentation.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.outlined.FolderOpen
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.marquis.zorroexpense.components.ProfileAvatar
import com.marquis.zorroexpense.domain.model.ExpenseList
import com.marquis.zorroexpense.platform.pullToRefreshBox
import com.marquis.zorroexpense.presentation.components.SwipeableExpenseListCard
import com.marquis.zorroexpense.presentation.components.bottomsheets.formatDateForDisplay
import com.marquis.zorroexpense.presentation.state.ExpenseListsUiEvent
import com.marquis.zorroexpense.presentation.state.ExpenseListsUiState
import com.marquis.zorroexpense.presentation.viewmodel.ExpenseListsOverviewViewModel

@Composable
internal fun ExpenseListsOverviewScreen(
    viewModel: ExpenseListsOverviewViewModel,
    onListSelected: (listId: String, listName: String) -> Unit = { _, _ -> },
    onCreateNewList: () -> Unit = {},
) {
    val uiState by viewModel.uiState.collectAsState()

    // Pull-to-refresh state
    val isRefreshing = (uiState as? ExpenseListsUiState.Success)?.isRefreshing ?: false
    val showDeleteDialog = (uiState as? ExpenseListsUiState.Success)?.showDeleteDialog ?: false
    val listToDelete = (uiState as? ExpenseListsUiState.Success)?.listToDelete

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = onCreateNewList,
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
            ) {
                Icon(Icons.Filled.Add, contentDescription = "Create new list")
            }
        },
    ) { paddingValues ->
        pullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = { viewModel.onEvent(ExpenseListsUiEvent.RefreshLists) },
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.surface),
            ) {
                // Modern gradient header
                ModernHeader()


                // Content based on state
                Box(modifier = Modifier.fillMaxSize()) {
                    when (uiState) {
                        is ExpenseListsUiState.Loading -> {
                            LoadingState()
                        }

                        is ExpenseListsUiState.Success -> {
                            val successState = uiState as ExpenseListsUiState.Success
                            Column(modifier = Modifier.fillMaxSize()) {
                                // Show refresh indicator when refreshing
                                if (successState.isRefreshing) {
                                    LinearProgressIndicator(
                                        modifier = Modifier.fillMaxWidth(),
                                        color = MaterialTheme.colorScheme.primary,
                                    )
                                }
                                SuccessState(
                                    lists = successState.lists,
                                    onListSelected = { list ->
                                        viewModel.onEvent(ExpenseListsUiEvent.SelectList(list.listId))
                                        onListSelected(list.listId, list.name)
                                    },
                                    onDeleteList = { list ->
                                        viewModel.onEvent(ExpenseListsUiEvent.DeleteList(list))
                                    },
                                )
                            }
                        }

                        is ExpenseListsUiState.Empty -> {
                            EmptyState(onCreateNewList = onCreateNewList)
                        }

                        is ExpenseListsUiState.Error -> {
                            val errorState = uiState as ExpenseListsUiState.Error
                            if (errorState.cachedLists != null) {
                                // Show cached lists with error message
                                ErrorStateWithCache(
                                    message = errorState.message,
                                    lists = errorState.cachedLists,
                                    onRetry = { viewModel.onEvent(ExpenseListsUiEvent.RetryLoad) },
                                    onListSelected = { list ->
                                        viewModel.onEvent(ExpenseListsUiEvent.SelectList(list.listId))
                                        onListSelected(list.listId, list.name)
                                    },
                                    onDeleteList = { list ->
                                        viewModel.onEvent(ExpenseListsUiEvent.DeleteList(list))
                                    },
                                )
                            } else {
                                ErrorState(
                                    message = errorState.message,
                                    onRetry = { viewModel.onEvent(ExpenseListsUiEvent.RetryLoad) },
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Delete confirmation dialog
    if (showDeleteDialog && listToDelete != null) {
        DeleteExpenseListDialog(
            listName = listToDelete.name,
            onConfirm = {
                viewModel.onEvent(ExpenseListsUiEvent.ConfirmDelete)
            },
            onDismiss = {
                viewModel.onEvent(ExpenseListsUiEvent.CancelDelete)
            },
        )
    }
}

/**
 * Modern gradient header with title and subtitle
 */
@Composable
private fun ModernHeader() {
    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .background(
                    brush =
                        Brush.verticalGradient(
                            colors =
                                listOf(
                                    MaterialTheme.colorScheme.primary,
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.8f),
                                ),
                        ),
                )
                .padding(vertical = 24.dp, horizontal = 20.dp),
    ) {
        Text(
            text = "Zorro Expense",
            style = MaterialTheme.typography.displaySmall,
            fontWeight = FontWeight.Bold,
            color = androidx.compose.ui.graphics.Color.White,
            fontSize = 28.sp,
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Manage your expense lists",
            style = MaterialTheme.typography.bodyLarge,
            color = androidx.compose.ui.graphics.Color.White.copy(alpha = 0.9f),
        )
    }
}

@Composable
private fun LoadingState() {
    Box(
        modifier =
            Modifier
                .fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            CircularProgressIndicator(
                modifier = Modifier.width(48.dp),
                color = MaterialTheme.colorScheme.primary,
                strokeWidth = 4.dp,
            )
            Spacer(modifier = Modifier.height(20.dp))
            Text(
                text = "Loading your lists...",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun SuccessState(
    lists: List<ExpenseList>,
    onListSelected: (ExpenseList) -> Unit,
    onDeleteList: (ExpenseList) -> Unit = {},
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 80.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        items(
            items = lists,
            key = { it.listId },
        ) { list ->
            val index = lists.indexOf(list)
            AnimatedVisibility(
                visible = true,
                enter =
                    fadeIn(
                        animationSpec = tween(durationMillis = 400, delayMillis = index * 60),
                    ) +
                        slideInVertically(
                            animationSpec = tween(durationMillis = 400, delayMillis = index * 60),
                            initialOffsetY = { 40 },
                        ),
            ) {
                SwipeableExpenseListCard(
                    list = list,
                    onClick = { onListSelected(list) },
                    onDelete = { onDeleteList(list) },
                )
            }
        }
    }
}

@Composable
internal fun ExpenseListCard(
    list: ExpenseList,
    onClick: () -> Unit,
    onDelete: () -> Unit = {},
    isSwipeable: Boolean = false,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    // Animate scale and elevation on press
    val scale by androidx.compose.animation.core.animateFloatAsState(
        targetValue = if (isPressed) 0.97f else 1f,
        animationSpec = spring(dampingRatio = 0.8f, stiffness = 300f),
        label = "card_scale",
    )

    Card(
        modifier =
            Modifier
                .fillMaxWidth()
                .scale(scale)
                .clickable(
                    interactionSource = interactionSource,
                    indication = null,
                    onClick = onClick,
                ),
        colors =
            CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainer,
            ),
        elevation =
            CardDefaults.cardElevation(
                defaultElevation = if (isPressed) 2.dp else 6.dp,
            ),
        shape = RoundedCornerShape(16.dp),
    ) {
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            // Folder icon with gradient background
            Box(
                modifier =
                    Modifier
                        .width(48.dp)
                        .height(48.dp)
                        .background(
                            brush =
                                Brush.linearGradient(
                                    colors =
                                        listOf(
                                            MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
                                            MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                                        ),
                                ),
                            shape = RoundedCornerShape(12.dp),
                        ),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Outlined.FolderOpen,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.scale(1.2f),
                )
            }

            // Title and last modified date (center section)
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = list.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                )
                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    val formattedTime = formatDateForDisplay(list.lastModified)
                    Text(
                        text = "Last Modified: ",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )

                    Text(
                        text = formattedTime,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }

            }

            // Member avatars and action buttons (right section)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                // Display member avatars
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy((-8).dp),
                ) {
                    list.members.take(4).forEach { member ->
                        ProfileAvatar(
                            name = member.name,
                            size = 32.dp,
                            userProfile = member.profileImage,
                            modifier =
                                Modifier
                                    .size(32.dp)
                                    .background(
                                        color = MaterialTheme.colorScheme.surface,
                                        shape = CircleShape,
                                    ),
                        )
                    }

                    // Show +N if more than 4 members
                    if (list.members.size > 4) {
                        Box(
                            modifier =
                                Modifier
                                    .size(32.dp)
                                    .background(
                                        color = MaterialTheme.colorScheme.tertiaryContainer,
                                        shape = CircleShape,
                                    ),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                text = "+${list.members.size - 4}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onTertiaryContainer,
                                fontWeight = FontWeight.SemiBold,
                            )
                        }
                    }
                }

                // Action buttons (edit and delete) - hidden on Android when using swipe
                if (!isSwipeable) {
                    // Delete button
                    IconButton(
                        onClick = {
                            onDelete()
                        },
                        modifier = Modifier.size(32.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Delete,
                            contentDescription = "Delete list",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(18.dp),
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun EmptyState(onCreateNewList: () -> Unit) {
    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .background(
                    brush =
                        Brush.verticalGradient(
                            colors =
                                listOf(
                                    MaterialTheme.colorScheme.surface,
                                    MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
                                ),
                        ),
                )
                .padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        // Folder icon with gradient
        Box(
            modifier =
                Modifier
                    .width(80.dp)
                    .height(80.dp)
                    .background(
                        brush =
                            Brush.linearGradient(
                                colors =
                                    listOf(
                                        MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                                        MaterialTheme.colorScheme.primary.copy(alpha = 0.05f),
                                    ),
                            ),
                        shape = RoundedCornerShape(20.dp),
                    ),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Outlined.FolderOpen,
                contentDescription = "No lists",
                modifier = Modifier.scale(2f),
                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f),
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "No Expense Lists Yet",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.SemiBold,
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "Create your first expense list to start tracking",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
        )

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = onCreateNewList,
            modifier =
                Modifier
                    .fillMaxWidth()
                    .height(48.dp),
            shape = RoundedCornerShape(12.dp),
        ) {
            Icon(
                imageVector = Icons.Filled.Add,
                contentDescription = "Create new list",
                modifier = Modifier.padding(end = 8.dp),
            )
            Text("Create New List")
        }
    }
}

@Composable
private fun ErrorStateWithCache(
    message: String,
    lists: List<ExpenseList>,
    onRetry: () -> Unit,
    onListSelected: (ExpenseList) -> Unit,
    onDeleteList: (ExpenseList) -> Unit = {},
) {
    Column(modifier = Modifier.fillMaxSize()) {
        // Error banner at the top
        Box(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.errorContainer)
                    .padding(12.dp),
            contentAlignment = Alignment.Center,
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Icon(
                    imageVector = Icons.Filled.Error,
                    contentDescription = "Error",
                    tint = MaterialTheme.colorScheme.onErrorContainer,
                    modifier = Modifier.size(20.dp),
                )
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Error Loading Updates",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Text(
                        text = message,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                    )
                }
                Button(
                    onClick = onRetry,
                    modifier =
                        Modifier
                            .height(32.dp),
                    shape = RoundedCornerShape(6.dp),
                ) {
                    Text(
                        "Retry",
                        style = MaterialTheme.typography.labelSmall,
                    )
                }
            }
        }

        // Show cached lists below the error banner
        SuccessState(
            lists = lists,
            onListSelected = onListSelected,
            onDeleteList = onDeleteList,
        )
    }
}

@Composable
private fun ErrorState(
    message: String,
    onRetry: () -> Unit,
) {
    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .background(
                    brush =
                        Brush.verticalGradient(
                            colors =
                                listOf(
                                    MaterialTheme.colorScheme.surface,
                                    MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.05f),
                                ),
                        ),
                )
                .padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier =
                Modifier
                    .width(80.dp)
                    .height(80.dp)
                    .background(
                        brush =
                            Brush.linearGradient(
                                colors =
                                    listOf(
                                        MaterialTheme.colorScheme.error.copy(alpha = 0.15f),
                                        MaterialTheme.colorScheme.error.copy(alpha = 0.05f),
                                    ),
                            ),
                        shape = RoundedCornerShape(20.dp),
                    ),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Filled.Error,
                contentDescription = "Error",
                modifier = Modifier.scale(2f),
                tint = MaterialTheme.colorScheme.error,
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Error Loading Lists",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.error,
            fontWeight = FontWeight.SemiBold,
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
        )

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = onRetry,
            modifier =
                Modifier
                    .fillMaxWidth()
                    .height(48.dp),
            shape = RoundedCornerShape(12.dp),
        ) {
            Text("Retry")
        }
    }
}

@Composable
private fun DeleteExpenseListDialog(
    listName: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Delete Expense List")
        },
        text = {
            Text("Are you sure you want to delete \"$listName\"? This action cannot be undone.")
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error,
                ),
            ) {
                Text("Delete")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Cancel")
            }
        },
    )
}
