package com.marquis.zorroexpense.presentation.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.marquis.zorroexpense.components.CategoryIconCircle
import com.marquis.zorroexpense.components.ProfileAvatar
import com.marquis.zorroexpense.domain.model.Category
import com.marquis.zorroexpense.domain.model.Group
import com.marquis.zorroexpense.domain.model.User
import com.marquis.zorroexpense.presentation.components.AddCategoryButton
import com.marquis.zorroexpense.presentation.components.bottomsheets.CategorySelectionMultiBottomSheet
import com.marquis.zorroexpense.presentation.state.GroupDetailUiEvent
import com.marquis.zorroexpense.presentation.state.GroupDetailUiState
import com.marquis.zorroexpense.presentation.state.GroupDetailMode
import com.marquis.zorroexpense.presentation.viewmodel.GroupDetailViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroupDetailScreen(
    viewModel: GroupDetailViewModel,
    onBackClick: () -> Unit,
    onListDeleted: () -> Unit = {},
) {
    val uiState by viewModel.uiState.collectAsState()
    val allCategories by viewModel.allCategories.collectAsState()
    val bottomSheetState = rememberModalBottomSheetState()
    val coroutineScope = rememberCoroutineScope()

    Scaffold(
        contentWindowInsets = WindowInsets.statusBars,
        topBar = {
            TopAppBar(
                title = {
                    val title = when (uiState) {
                        is GroupDetailUiState.Success -> {
                            when ((uiState as GroupDetailUiState.Success).mode) {
                                GroupDetailMode.VIEW -> "Group Details"
                                GroupDetailMode.EDIT -> "Edit group"
                                GroupDetailMode.ADD -> "New Group"
                            }
                        }
                        else -> "Group Details"
                    }
                    Text(
                        text = title,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                    )
                },
                navigationIcon = {
                    IconButton(onClick = {
                        val currentState = uiState
                        if (currentState is GroupDetailUiState.Success &&
                            currentState.mode == GroupDetailMode.EDIT
                        ) {
                            viewModel.onEvent(GroupDetailUiEvent.CancelEdit)
                        } else {
                            onBackClick()
                        }
                    }) {
                        val icon = if (uiState is GroupDetailUiState.Success &&
                            (uiState as GroupDetailUiState.Success).mode != GroupDetailMode.VIEW
                        ) {
                            Icons.Default.Close
                        } else {
                            Icons.AutoMirrored.Filled.ArrowBack
                        }
                        Icon(
                            imageVector = icon,
                            contentDescription = "Back",
                        )
                    }
                },
                actions = {
                    if (uiState is GroupDetailUiState.Success) {
                        val successState = uiState as GroupDetailUiState.Success

                        when (successState.mode) {
                            GroupDetailMode.VIEW -> {
                                IconButton(
                                    onClick = {
                                        viewModel.onEvent(GroupDetailUiEvent.EnterEditMode)
                                    },
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Edit,
                                        contentDescription = "Edit groups",
                                    )
                                }
                                IconButton(
                                    onClick = {
                                        viewModel.onEvent(GroupDetailUiEvent.DeleteGroup)
                                    },
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Delete,
                                        contentDescription = "Delete Group",
                                    )
                                }
                            }
                            GroupDetailMode.EDIT, GroupDetailMode.ADD -> {
                                // Save button
                                if (successState.isSaving) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.padding(12.dp),
                                        strokeWidth = 2.dp,
                                    )
                                } else {
                                    IconButton(
                                        onClick = {
                                            viewModel.onEvent(GroupDetailUiEvent.SaveChanges)
                                        },
                                        enabled = successState.editedName.isNotBlank() &&
                                            (successState.mode != GroupDetailMode.ADD || successState.editedCategories.isNotEmpty()),
                                    ) {
                                        Icon(
                                            imageVector = Icons.Filled.Check,
                                            contentDescription = "Save",
                                        )
                                    }
                                }
                            }
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface,
                    navigationIconContentColor = MaterialTheme.colorScheme.onSurface,
                    actionIconContentColor = MaterialTheme.colorScheme.onSurface,
                ),
            )
        },
    ) { paddingValues ->
        when (uiState) {
            is GroupDetailUiState.Loading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator()
                }
            }
            is GroupDetailUiState.Success -> {
                val successState = uiState as GroupDetailUiState.Success
                ExpenseListDetailContent(
                    group = successState.group,
                    mode = successState.mode,
                    editedName = successState.editedName,
                    editedCategories = successState.editedCategories,
                    editedMembers = successState.editedMembers,
                    currentUserId = viewModel.currentUserId,
                    onNameChange = { viewModel.onEvent(GroupDetailUiEvent.UpdateName(it)) },
                    onAddCategoryClick = { viewModel.onEvent(GroupDetailUiEvent.AddCategoryClicked) },
                    onRemoveCategory = { viewModel.onEvent(GroupDetailUiEvent.RemoveCategory(it)) },
                    onRemoveMember = { viewModel.onEvent(GroupDetailUiEvent.RemoveMember(it)) },
                    modifier = Modifier.padding(paddingValues),
                )

                if (successState.showDeleteDialog) {
                    DeleteListConfirmationDialog(
                        groupName = successState.group.name,
                        onConfirm = { viewModel.onEvent(GroupDetailUiEvent.ConfirmDelete) },
                        onDismiss = { viewModel.onEvent(GroupDetailUiEvent.CancelDelete) },
                    )
                }

                if (successState.showDeleteMemberDialog && successState.memberToDelete != null) {
                    DeleteMemberConfirmationDialog(
                        memberName = successState.memberToDelete.name,
                        onConfirm = { viewModel.onEvent(GroupDetailUiEvent.ConfirmDeleteMember) },
                        onDismiss = { viewModel.onEvent(GroupDetailUiEvent.CancelDeleteMember) },
                    )
                }

                if (successState.showCategoryBottomSheet) {
                    CategorySelectionMultiBottomSheet(
                        sheetState = bottomSheetState,
                        categories = allCategories,
                        selectedCategories = successState.editedCategories,
                        onCategoryToggled = { category ->
                            viewModel.onEvent(GroupDetailUiEvent.CategoryToggled(category))
                        },
                        onDismiss = {
                            coroutineScope.launch {
                                bottomSheetState.hide()
                                viewModel.onEvent(GroupDetailUiEvent.DismissCategoryBottomSheet)
                            }
                        },
                    )
                }
            }
            is GroupDetailUiState.Deleted -> {
                LaunchedEffect(Unit) {
                    onListDeleted()
                }

                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = "List deleted successfully",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
            }
            is GroupDetailUiState.Error -> {
                val errorState = uiState as GroupDetailUiState.Error
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = "Error: ${errorState.message}",
                        color = MaterialTheme.colorScheme.error,
                    )
                }
            }
        }
    }
}

@Composable
private fun ExpenseListDetailContent(
    group: Group,
    mode: GroupDetailMode,
    editedName: String,
    editedCategories: List<Category>,
    editedMembers: List<User>,
    currentUserId: String,
    onNameChange: (String) -> Unit,
    onAddCategoryClick: () -> Unit,
    onRemoveCategory: (Category) -> Unit,
    onRemoveMember: (User) -> Unit,
    modifier: Modifier = Modifier,
) {
    val isEditable = mode != GroupDetailMode.VIEW
    val displayCategories = if (isEditable) editedCategories else group.categories
    val displayMembers = if (isEditable) editedMembers else group.members
    val focusManager = androidx.compose.ui.platform.LocalFocusManager.current

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
    ) {
        if (isEditable) {
            OutlinedTextField(
                value = editedName,
                onValueChange = onNameChange,
                label = { Text("List Name") },
                placeholder = { Text("Enter list name") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
            )
        } else {
            Text(
                text = group.name.ifEmpty { "Unnamed List" },
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
            ),
            shape = RoundedCornerShape(16.dp),
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
            ) {
                Text(
                    text = "Categories",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 12.dp),
                )

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    if (isEditable) {
                        item {
                            AddCategoryButton(
                                onClick = {
                                    focusManager.clearFocus()
                                    onAddCategoryClick()
                                },
                                label = "Modify",
                                size = 48.dp,
                            )
                        }
                    }

                    items(displayCategories) { category ->
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            CategoryIconCircle(
                                category = category,
                                size = 48.dp,
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = category.name,
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurface,
                                textAlign = TextAlign.Center,
                            )
                        }
                    }
                }

                if (displayCategories.isEmpty()) {
                    Text(
                        text = "No categories added yet",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                        modifier = Modifier.padding(top = 8.dp),
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Members section
        if (displayMembers.isNotEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                ),
                shape = RoundedCornerShape(16.dp),
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                ) {
                    Text(
                        text = "Members",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 12.dp),
                    )

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        items(displayMembers) { member ->
                            val canRemove = isEditable && member.userId != currentUserId

                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                            ) {
                                Box {
                                    ProfileAvatar(
                                        name = member.name,
                                        size = 56.dp,
                                        userProfile = member.profileImage,
                                        backgroundColor = MaterialTheme.colorScheme.secondary,
                                        contentColor = MaterialTheme.colorScheme.onSecondary,
                                    )

                                    // Remove button for removable members
                                    if (canRemove) {
                                        Card(
                                            modifier = Modifier
                                                .size(20.dp)
                                                .align(Alignment.TopEnd),
                                            colors = CardDefaults.cardColors(
                                                containerColor = MaterialTheme.colorScheme.error,
                                            ),
                                            shape = CircleShape,
                                            onClick = { onRemoveMember(member) },
                                        ) {
                                            Box(
                                                modifier = Modifier.fillMaxSize(),
                                                contentAlignment = Alignment.Center,
                                            ) {
                                                Icon(
                                                    Icons.Default.Close,
                                                    contentDescription = "Remove ${member.name}",
                                                    tint = MaterialTheme.colorScheme.onError,
                                                    modifier = Modifier.size(12.dp),
                                                )
                                            }
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                Text(
                                    text = member.name.ifEmpty { "Unknown" },
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    textAlign = TextAlign.Center,
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }

        // Share code section (only in VIEW mode and when available)
        if (mode == GroupDetailMode.VIEW && group.shareCode.isNotEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
                ),
                shape = RoundedCornerShape(16.dp),
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(
                        modifier = Modifier.weight(1f),
                    ) {
                        Text(
                            text = "Share Code",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = "Share this code to invite others",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                        )
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                        ),
                        shape = RoundedCornerShape(8.dp),
                    ) {
                        Text(
                            text = group.shareCode,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }

        // Metadata section (only in VIEW mode)
        if (mode == GroupDetailMode.VIEW) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f),
                ),
                shape = RoundedCornerShape(16.dp),
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                ) {
                    Text(
                        text = "Details",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 12.dp),
                    )

                    // Created by
                    if (group.createdBy.isNotEmpty()) {
                        MetadataRow(
                            label = "Created by",
                            value = findMemberName(group, group.createdBy),
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    // Created at
                    if (group.createdAt.isNotEmpty()) {
                        MetadataRow(
                            label = "Created",
                            value = formatTimestamp(group.createdAt),
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    // Last modified
                    if (group.lastModified.isNotEmpty()) {
                        MetadataRow(
                            label = "Last modified",
                            value = formatTimestamp(group.lastModified),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MetadataRow(
    label: String,
    value: String,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}

private fun findMemberName(group: Group, userId: String): String {
    val member = group.members.find { it.userId == userId }
    return member?.name?.ifEmpty { "Unknown" } ?: "Unknown"
}

private fun formatTimestamp(timestamp: String): String {
    if (timestamp.isBlank()) {
        return "Unknown"
    }

    try {
        val dateStr = timestamp.substringBefore("T")
        val parts = dateStr.split("-")
        if (parts.size >= 3) {
            val year = parts[0]
            val month = parts[1].toIntOrNull() ?: 1
            val day = parts[2].toIntOrNull() ?: 1

            val monthNames = arrayOf(
                "Jan", "Feb", "Mar", "Apr", "May", "Jun",
                "Jul", "Aug", "Sep", "Oct", "Nov", "Dec",
            )

            val monthName = if (month in 1..12) monthNames[month - 1] else "Jan"
            return "$monthName $day, $year"
        }
    } catch (_: Exception) {
        // Fall back to simple format
    }

    return timestamp.substringBefore("T").takeIf { it.isNotBlank() } ?: timestamp
}

@Composable
private fun DeleteListConfirmationDialog(
    groupName: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Delete Group?",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
            )
        },
        text = {
            Text(
                text = "Are you sure you want to delete \"$groupName\"? This will also delete all expenses in this group. This action cannot be undone.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error,
                ),
            ) {
                Text(
                    text = "Delete",
                    color = MaterialTheme.colorScheme.onError,
                )
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text(
                    text = "Cancel",
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }
        },
    )
}

@Composable
private fun DeleteMemberConfirmationDialog(
    memberName: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Remove Member?",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
            )
        },
        text = {
            Text(
                text = "Are you sure you want to remove \"$memberName\" from this list?",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error,
                ),
            ) {
                Text(
                    text = "Remove",
                    color = MaterialTheme.colorScheme.onError,
                )
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text(
                    text = "Cancel",
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }
        },
    )
}
