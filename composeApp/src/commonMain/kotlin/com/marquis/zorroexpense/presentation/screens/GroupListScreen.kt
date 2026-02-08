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
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.GroupAdd
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.outlined.FolderOpen
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.marquis.zorroexpense.components.ProfileAvatar
import com.marquis.zorroexpense.domain.model.Group
import com.marquis.zorroexpense.platform.pullToRefreshBox
import com.marquis.zorroexpense.presentation.components.SpeedDialFab
import com.marquis.zorroexpense.presentation.components.SpeedDialFabItem
import com.marquis.zorroexpense.presentation.components.SwipeableGroupCard
import com.marquis.zorroexpense.presentation.components.bottomsheets.formatDateForDisplay
import com.marquis.zorroexpense.presentation.state.GroupListUiEvent
import com.marquis.zorroexpense.presentation.state.GroupListUiState
import com.marquis.zorroexpense.presentation.viewmodel.GroupListViewModel

@Composable
internal fun GroupListScreen(
    viewModel: GroupListViewModel,
    onGroupSelected: (listId: String, listName: String) -> Unit = { _, _ -> },
    onCreateGroup: () -> Unit = {},
    onEditGroup: (group: Group) -> Unit = { _ -> },
) {
    val uiState by viewModel.uiState.collectAsState()

    val isRefreshing = (uiState as? GroupListUiState.Success)?.isRefreshing ?: false
    val showDeleteDialog = (uiState as? GroupListUiState.Success)?.showDeleteDialog ?: false
    val listToDelete = (uiState as? GroupListUiState.Success)?.listToDelete

    val listState = rememberLazyListState()
    var isFabExpanded by remember { mutableStateOf(true) }
    var isFabMenuExpanded by remember { mutableStateOf(false) }
    var showJoinGroupDialog by remember { mutableStateOf(false) }
    var isJoiningGroup by remember { mutableStateOf(false) }

    // Close dialog when join operation completes
    LaunchedEffect(uiState) {
        if (isJoiningGroup && uiState !is GroupListUiState.Loading) {
            showJoinGroupDialog = false
            isJoiningGroup = false
        }
    }

    // FAB scroll behavior: collapse when scrolling down, expand when near top or scrolling up
    // Also auto-close the menu when scrolling
    LaunchedEffect(listState) {
        var previousFirstVisibleItemIndex = 0
        var previousFirstVisibleItemScrollOffset = 0

        snapshotFlow {
            listState.firstVisibleItemIndex to listState.firstVisibleItemScrollOffset
        }.collect { (currentIndex, currentOffset) ->
            val isScrollingDown =
                if (currentIndex != previousFirstVisibleItemIndex) {
                    currentIndex > previousFirstVisibleItemIndex
                } else {
                    currentOffset > previousFirstVisibleItemScrollOffset
                }

            // Auto-expand when near the top (within 3 items) for better UX
            isFabExpanded = if (currentIndex <= 3) {
                true
            } else {
                !isScrollingDown
            }

            // Auto-close menu when scrolling
            if (isScrollingDown) {
                isFabMenuExpanded = false
            }

            // Update previous values for next iteration
            previousFirstVisibleItemIndex = currentIndex
            previousFirstVisibleItemScrollOffset = currentOffset
        }
    }

    Scaffold(
        floatingActionButton = {
            SpeedDialFab(
                expanded = isFabMenuExpanded,
                onExpandedChange = { isFabMenuExpanded = it },
                items = listOf(
                    SpeedDialFabItem(
                        icon = Icons.Default.Share,
                        label = "Join Group",
                        contentDescription = "Join Group",
                        onClick = { showJoinGroupDialog = true },
                    ),
                    SpeedDialFabItem(
                        icon = Icons.Default.Add,
                        label = "Add Group",
                        contentDescription = "Add Group",
                        onClick = { onCreateGroup() },
                    ),
                ),
                fabExpanded = isFabExpanded,
            )
        },
    ) { paddingValues ->
        pullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = { viewModel.onEvent(GroupListUiEvent.RefreshGroups) },
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
                ModernHeader()

                Box(modifier = Modifier.fillMaxSize()) {
                    when (uiState) {
                        is GroupListUiState.Loading -> {
                            LoadingState()
                        }

                        is GroupListUiState.Success -> {
                            val successState = uiState as GroupListUiState.Success
                            Column(modifier = Modifier.fillMaxSize()) {
                                if (successState.isRefreshing) {
                                    LinearProgressIndicator(
                                        modifier = Modifier.fillMaxWidth(),
                                        color = MaterialTheme.colorScheme.primary,
                                    )
                                }
                                SuccessState(
                                    lists = successState.groups,
                                    listState = listState,
                                    onGroupSelected = { group ->
                                        viewModel.onEvent(GroupListUiEvent.SelectGroup(group.listId))
                                        onGroupSelected(group.listId, group.name)
                                    },
                                    onEditGroup = { group ->
                                        onEditGroup(group)
                                    },
                                    onDeleteGroup = { group ->
                                        viewModel.onEvent(GroupListUiEvent.DeleteGroup(group))
                                    },
                                )
                            }
                        }

                        is GroupListUiState.Empty -> {
                            EmptyState(onCreateGroup = onCreateGroup)
                        }

                        is GroupListUiState.Error -> {
                            val errorState = uiState as GroupListUiState.Error
                            if (errorState.cachedLists != null) {
                                ErrorStateWithCache(
                                    message = errorState.message,
                                    lists = errorState.cachedLists,
                                    listState = listState,
                                    onRetry = { viewModel.onEvent(GroupListUiEvent.RetryLoad) },
                                    onGroupSelected = { group ->
                                        viewModel.onEvent(GroupListUiEvent.SelectGroup(group.listId))
                                        onGroupSelected(group.listId, group.name)
                                    },
                                    onEditGroup = { group ->
                                        onEditGroup(group)
                                    },
                                    onDeleteGroup = { group ->
                                        viewModel.onEvent(GroupListUiEvent.DeleteGroup(group))
                                    },
                                )
                            } else {
                                ErrorState(
                                    message = errorState.message,
                                    onRetry = { viewModel.onEvent(GroupListUiEvent.RetryLoad) },
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showJoinGroupDialog) {
        val errorMessage = (uiState as? GroupListUiState.Error)?.message ?: ""

        JoinGroupDialog(
            isLoading = uiState is GroupListUiState.Loading,
            initialErrorMessage = errorMessage,
            onJoin = { shareCode ->
                isJoiningGroup = true
                viewModel.onEvent(GroupListUiEvent.JoinGroup(shareCode))
            },
            onDismiss = {
                showJoinGroupDialog = false
                isJoiningGroup = false
            },
        )
    }

    if (showDeleteDialog && listToDelete != null) {
        DeleteExpenseListDialog(
            listName = listToDelete.name,
            onConfirm = {
                viewModel.onEvent(GroupListUiEvent.ConfirmDelete)
            },
            onDismiss = {
                viewModel.onEvent(GroupListUiEvent.CancelDelete)
            },
        )
    }
}

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
            text = "Manage your groups",
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
    lists: List<Group>,
    listState: androidx.compose.foundation.lazy.LazyListState,
    onGroupSelected: (Group) -> Unit,
    onEditGroup: (Group) -> Unit = {},
    onDeleteGroup: (Group) -> Unit = {},
) {
    LazyColumn(
        state = listState,
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 80.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        items(
            items = lists,
            key = { it.listId },
        ) { group ->
            val index = lists.indexOf(group)
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
                SwipeableGroupCard(
                    list = group,
                    onClick = { onGroupSelected(group) },
                    onEdit = { onEditGroup(group)},
                    onDelete = { onDeleteGroup(group) },
                )
            }
        }
    }
}

@Composable
internal fun ExpenseListCard(
    list: Group,
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
fun EmptyState(onCreateGroup: () -> Unit) {
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
            onClick = onCreateGroup,
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
    lists: List<Group>,
    listState: androidx.compose.foundation.lazy.LazyListState,
    onRetry: () -> Unit,
    onGroupSelected: (Group) -> Unit,
    onEditGroup: (Group) -> Unit = { _ -> },
    onDeleteGroup: (Group) -> Unit = {},
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
            listState = listState,
            onGroupSelected = onGroupSelected,
            onEditGroup = onEditGroup,
            onDeleteGroup = onDeleteGroup,
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
private fun JoinGroupDialog(
    isLoading: Boolean,
    initialErrorMessage: String = "",
    onJoin: (shareCode: String) -> Unit,
    onDismiss: () -> Unit,
) {
    var shareCode by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf(initialErrorMessage) }

    // Update error message when it changes from parent
    LaunchedEffect(initialErrorMessage) {
        if (initialErrorMessage.isNotEmpty()) {
            errorMessage = initialErrorMessage
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Join Group")
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(
                    text = "Enter the share code to join an existing group.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )

                OutlinedTextField(
                    value = shareCode,
                    onValueChange = {
                        shareCode = it
                        errorMessage = ""
                    },
                    placeholder = {
                        Text("Share code")
                    },
                    leadingIcon = {
                        Icon(
                            Icons.Default.Share,
                            contentDescription = null,
                        )
                    },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isLoading,
                )

                if (errorMessage.isNotEmpty()) {
                    Text(
                        text = errorMessage,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(top = 4.dp),
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (shareCode.isNotBlank()) {
                        onJoin(shareCode)
                    } else {
                        errorMessage = "Please enter a share code"
                    }
                },
                enabled = shareCode.isNotBlank() && !isLoading,
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp,
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Text("Join")
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                enabled = !isLoading,
            ) {
                Text("Cancel")
            }
        },
    )
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
            Text("Delete Group")
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
