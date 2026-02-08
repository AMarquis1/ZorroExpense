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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.marquis.zorroexpense.components.CategoryIconCircle
import com.marquis.zorroexpense.components.ProfileAvatar
import com.marquis.zorroexpense.domain.model.Category
import com.marquis.zorroexpense.domain.model.ExpenseList
import com.marquis.zorroexpense.domain.model.User
import com.marquis.zorroexpense.presentation.components.AddCategoryButton
import com.marquis.zorroexpense.presentation.components.bottomsheets.CategorySelectionMultiBottomSheet
import com.marquis.zorroexpense.presentation.state.ExpenseListDetailUiEvent
import com.marquis.zorroexpense.presentation.state.ExpenseListDetailUiState
import com.marquis.zorroexpense.presentation.state.ListDetailMode
import com.marquis.zorroexpense.presentation.viewmodel.ExpenseListDetailViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpenseListDetailScreen(
    viewModel: ExpenseListDetailViewModel,
    onBackClick: () -> Unit,
    onListDeleted: () -> Unit = {},
) {
    val uiState by viewModel.uiState.collectAsState()
    val allCategories by viewModel.allCategories.collectAsState()
    val categoryBottomSheetState = rememberModalBottomSheetState()

    Scaffold(
        contentWindowInsets = WindowInsets.statusBars,
        topBar = {
            TopAppBar(
                title = {
                    val title = when (uiState) {
                        is ExpenseListDetailUiState.Success -> {
                            when ((uiState as ExpenseListDetailUiState.Success).mode) {
                                ListDetailMode.VIEW -> "List Details"
                                ListDetailMode.EDIT -> "Edit List"
                                ListDetailMode.ADD -> "New List"
                            }
                        }
                        else -> "List Details"
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
                        if (currentState is ExpenseListDetailUiState.Success &&
                            currentState.mode == ListDetailMode.EDIT
                        ) {
                            // Cancel edit mode
                            viewModel.onEvent(ExpenseListDetailUiEvent.CancelEdit)
                        } else {
                            onBackClick()
                        }
                    }) {
                        val icon = if (uiState is ExpenseListDetailUiState.Success &&
                            (uiState as ExpenseListDetailUiState.Success).mode != ListDetailMode.VIEW
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
                    if (uiState is ExpenseListDetailUiState.Success) {
                        val successState = uiState as ExpenseListDetailUiState.Success

                        when (successState.mode) {
                            ListDetailMode.VIEW -> {
                                // Edit button
                                IconButton(
                                    onClick = {
                                        viewModel.onEvent(ExpenseListDetailUiEvent.EnterEditMode)
                                    },
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Edit,
                                        contentDescription = "Edit List",
                                    )
                                }
                                // Delete button
                                IconButton(
                                    onClick = {
                                        viewModel.onEvent(ExpenseListDetailUiEvent.DeleteList)
                                    },
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Delete,
                                        contentDescription = "Delete List",
                                    )
                                }
                            }
                            ListDetailMode.EDIT, ListDetailMode.ADD -> {
                                // Save button
                                if (successState.isSaving) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.padding(12.dp),
                                        strokeWidth = 2.dp,
                                    )
                                } else {
                                    IconButton(
                                        onClick = {
                                            viewModel.onEvent(ExpenseListDetailUiEvent.SaveChanges)
                                        },
                                        enabled = successState.editedName.isNotBlank(),
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
            is ExpenseListDetailUiState.Loading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator()
                }
            }
            is ExpenseListDetailUiState.Success -> {
                val successState = uiState as ExpenseListDetailUiState.Success
                ExpenseListDetailContent(
                    expenseList = successState.expenseList,
                    mode = successState.mode,
                    editedName = successState.editedName,
                    editedCategories = successState.editedCategories,
                    editedMembers = successState.editedMembers,
                    currentUserId = viewModel.currentUserId,
                    onNameChange = { viewModel.onEvent(ExpenseListDetailUiEvent.UpdateName(it)) },
                    onAddCategoryClick = { viewModel.onEvent(ExpenseListDetailUiEvent.AddCategoryClicked) },
                    onRemoveCategory = { viewModel.onEvent(ExpenseListDetailUiEvent.RemoveCategory(it)) },
                    onRemoveMember = { viewModel.onEvent(ExpenseListDetailUiEvent.RemoveMember(it)) },
                    modifier = Modifier.padding(paddingValues),
                )

                if (successState.showDeleteDialog) {
                    DeleteListConfirmationDialog(
                        listName = successState.expenseList.name,
                        onConfirm = { viewModel.onEvent(ExpenseListDetailUiEvent.ConfirmDelete) },
                        onDismiss = { viewModel.onEvent(ExpenseListDetailUiEvent.CancelDelete) },
                    )
                }

                if (successState.showDeleteMemberDialog && successState.memberToDelete != null) {
                    DeleteMemberConfirmationDialog(
                        memberName = successState.memberToDelete.name,
                        onConfirm = { viewModel.onEvent(ExpenseListDetailUiEvent.ConfirmDeleteMember) },
                        onDismiss = { viewModel.onEvent(ExpenseListDetailUiEvent.CancelDeleteMember) },
                    )
                }

                if (successState.showCategoryBottomSheet) {
                    CategorySelectionMultiBottomSheet(
                        categories = allCategories,
                        selectedCategories = successState.editedCategories,
                        onCategoryToggled = { category ->
                            viewModel.onEvent(ExpenseListDetailUiEvent.CategoryToggled(category))
                        },
                        onDismiss = {
                            viewModel.onEvent(ExpenseListDetailUiEvent.DismissCategoryBottomSheet)
                        },
                        bottomSheetState = categoryBottomSheetState,
                    )
                }
            }
            is ExpenseListDetailUiState.Deleted -> {
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
            is ExpenseListDetailUiState.Error -> {
                val errorState = uiState as ExpenseListDetailUiState.Error
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
    expenseList: ExpenseList,
    mode: ListDetailMode,
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
    val isEditable = mode != ListDetailMode.VIEW
    val displayCategories = if (isEditable) editedCategories else expenseList.categories
    val displayMembers = if (isEditable) editedMembers else expenseList.members

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
                text = expenseList.name.ifEmpty { "Unnamed List" },
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
                    // Add button first (only in EDIT/ADD mode)
                    if (isEditable) {
                        item {
                            AddCategoryButton(
                                onClick = onAddCategoryClick,
                                label = "Modify",
                                size = 48.dp,
                            )
                        }
                    }

                    // Categories
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
        if (mode == ListDetailMode.VIEW && expenseList.shareCode.isNotEmpty()) {
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
                            text = expenseList.shareCode,
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
        if (mode == ListDetailMode.VIEW) {
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
                    if (expenseList.createdBy.isNotEmpty()) {
                        MetadataRow(
                            label = "Created by",
                            value = findMemberName(expenseList, expenseList.createdBy),
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    // Created at
                    if (expenseList.createdAt.isNotEmpty()) {
                        MetadataRow(
                            label = "Created",
                            value = formatTimestamp(expenseList.createdAt),
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    // Last modified
                    if (expenseList.lastModified.isNotEmpty()) {
                        MetadataRow(
                            label = "Last modified",
                            value = formatTimestamp(expenseList.lastModified),
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

private fun findMemberName(expenseList: ExpenseList, userId: String): String {
    val member = expenseList.members.find { it.userId == userId }
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
    listName: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Delete List?",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
            )
        },
        text = {
            Text(
                text = "Are you sure you want to delete \"$listName\"? This will also delete all expenses in this list. This action cannot be undone.",
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
