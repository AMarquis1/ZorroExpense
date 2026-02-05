package com.marquis.zorroexpense.presentation.screens

import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.marquis.zorroexpense.components.CategoryIconCircle
import com.marquis.zorroexpense.components.ProfileAvatar
import com.marquis.zorroexpense.domain.model.Expense
import com.marquis.zorroexpense.presentation.constants.DeleteConstants
import com.marquis.zorroexpense.presentation.state.ExpenseDetailUiEvent
import com.marquis.zorroexpense.presentation.state.ExpenseDetailUiState
import zorroexpense.composeapp.generated.resources.Res
import zorroexpense.composeapp.generated.resources.alex
import zorroexpense.composeapp.generated.resources.sarah

@OptIn(ExperimentalMaterial3Api::class, ExperimentalSharedTransitionApi::class)
@Composable
fun ExpenseDetailScreen(
    expense: Expense,
    onBackClick: () -> Unit,
    onExpenseDeleted: (expenseName: String) -> Unit = {},
    onEditExpense: (Expense) -> Unit = {},
    sharedTransitionScope: SharedTransitionScope,
    animatedContentScope: AnimatedContentScope,
) {
    // Create ViewModel with the expense
    val viewModel =
        remember {
            com.marquis.zorroexpense.di.AppModule
                .provideExpenseDetailViewModel(expense)
        }
    val uiState by viewModel.uiState.collectAsState()
    Scaffold(
        contentWindowInsets = WindowInsets.statusBars,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Expense Details",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = { onEditExpense(expense) },
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Edit,
                            contentDescription = "Edit Expense",
                        )
                    }
                    IconButton(
                        onClick = {
                            viewModel.onEvent(ExpenseDetailUiEvent.DeleteExpense)
                        },
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Delete,
                            contentDescription = "Delete Expense",
                        )
                    }
                },
                colors =
                    TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                        titleContentColor = MaterialTheme.colorScheme.onSurface,
                        navigationIconContentColor = MaterialTheme.colorScheme.onSurface,
                        actionIconContentColor = MaterialTheme.colorScheme.onSurface,
                    ),
            )
        },
    ) { paddingValues ->
        // Handle different UI states
        when (uiState) {
            is ExpenseDetailUiState.Loading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    Text("Loading...")
                }
            }
            is ExpenseDetailUiState.Success -> {
                val successState = uiState as ExpenseDetailUiState.Success
                ExpenseDetailContent(
                    expense = successState.expense,
                    paddingValues = paddingValues,
                    sharedTransitionScope = sharedTransitionScope,
                    animatedContentScope = animatedContentScope,
                )

                // Show delete confirmation dialog
                if (successState.showDeleteDialog) {
                    DeleteConfirmationDialog(
                        onConfirm = { viewModel.onEvent(ExpenseDetailUiEvent.ConfirmDelete) },
                        onDismiss = { viewModel.onEvent(ExpenseDetailUiEvent.CancelDelete) },
                    )
                }
            }
            is ExpenseDetailUiState.Deleted -> {
                // Show success state briefly, then navigate back
                LaunchedEffect(Unit) {
                    onExpenseDeleted(expense.name)
                }

                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = "Expense deleted successfully",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
            }
            is ExpenseDetailUiState.Error -> {
                val errorState = uiState as ExpenseDetailUiState.Error
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

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
private fun ExpenseDetailContent(
    expense: Expense,
    paddingValues: PaddingValues,
    sharedTransitionScope: SharedTransitionScope,
    animatedContentScope: AnimatedContentScope,
) {
    with(sharedTransitionScope) {
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(paddingValues)
                    .padding(20.dp),
        ) {
            // Clean header section with category icon
            Row(
                modifier =
                    Modifier
                        .fillMaxWidth(),
                verticalAlignment = Alignment.Top,
            ) {
                // Category icon with shared element transition
                if (expense.category.icon.isNotEmpty()) {
                    CategoryIconCircle(
                        category = expense.category,
                        size = 60.dp,
                        modifier =
                            with(sharedTransitionScope) {
                                Modifier
                                    .align(Alignment.CenterVertically)
                                    .sharedElement(
                                        sharedContentState =
                                            rememberSharedContentState(
                                                key = "category-${expense.category.name}-${expense.name}-${expense.date}",
                                            ),
                                        animatedVisibilityScope = animatedContentScope,
                                    )
                            },
                    )

                    Spacer(modifier = Modifier.width(16.dp))
                }

                Column(
                    modifier = Modifier.weight(1f),
                ) {
                    // Expense name - primary emphasis
                    Text(
                        text = expense.name.ifEmpty { "Unnamed Expense" },
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    // Date - secondary emphasis
                    Text(
                        text = formatDate(expense.date),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                // Price - dramatic emphasis
                Card(
                    colors =
                        CardDefaults.cardColors(
                            containerColor =
                                expense.category.color
                                    .takeIf { it.isNotEmpty() }
                                    ?.let { parseHexColor(it) }
                                    ?: MaterialTheme.colorScheme.primaryContainer,
                        ),
                    shape = RoundedCornerShape(12.dp),
                ) {
                    Box(
                        modifier = Modifier.padding(12.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = "$${formatPrice(expense.price)}",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Buyer information
            val buyer = expense.paidBy
            if (buyer.userId.isNotEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors =
                        CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                        ),
                    shape = RoundedCornerShape(16.dp),
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                    ) {
                        Text(
                            text = "Paid by",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(bottom = 12.dp),
                        )

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            ProfileAvatar(
                                name = buyer.name,
                                size = 48.dp,
                                userProfile = buyer.profileImage,
                                backgroundColor = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary,
                            )

                            Spacer(modifier = Modifier.width(16.dp))

                            Text(
                                text = buyer.name,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurface,
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }

            if (expense.splitDetails.isNotEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors =
                        CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                        ),
                    shape = RoundedCornerShape(16.dp),
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                    ) {
                        Text(
                            text = "Split with",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(bottom = 12.dp),
                        )

                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            items(expense.splitDetails) { splitDetail ->
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                ) {
                                    ProfileAvatar(
                                        name = splitDetail.user.name,
                                        size = 56.dp,
                                        userProfile = splitDetail.user.profileImage,
                                        backgroundColor = MaterialTheme.colorScheme.secondary,
                                        contentColor = MaterialTheme.colorScheme.onSecondary,
                                    )

                                    Spacer(modifier = Modifier.height(8.dp))

                                    Text(
                                        text = splitDetail.user.name,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Medium,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        textAlign = TextAlign.Center,
                                    )

                                    // Use the actual split amount
                                    val splitAmount = splitDetail.amount
                                    Text(
                                        text = "$${formatPrice(splitAmount)}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        textAlign = TextAlign.Center,
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }

            // Category info
            if (expense.category.name.isNotEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors =
                        CardDefaults.cardColors(
                            containerColor = parseHexColor(expense.category.color).copy(alpha = 0.1f),
                        ),
                    shape = RoundedCornerShape(16.dp),
                ) {
                    Row(
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = "Category",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )

                        Spacer(modifier = Modifier.weight(1f))

                        Text(
                            text = expense.category.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = parseHexColor(expense.category.color),
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }

            // Description section
            if (expense.description.isNotEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors =
                        CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                        ),
                    shape = RoundedCornerShape(16.dp),
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                    ) {
                        Text(
                            text = "Description",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(bottom = 8.dp),
                        )

                        Text(
                            text = expense.description,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            lineHeight = MaterialTheme.typography.bodyLarge.lineHeight,
                        )
                    }
                }
            } else {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors =
                        CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f),
                        ),
                    shape = RoundedCornerShape(16.dp),
                ) {
                    Box(
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .padding(20.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = "No description available",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                            fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                        )
                    }
                }
            }
        }
    }
}

// Utility functions
private fun formatPrice(price: Double): String =
    if (price == price.toInt().toDouble()) {
        price.toInt().toString()
    } else {
        val rounded = (price * 100).toInt() / 100.0
        val decimalPart = ((rounded * 100) % 100).toInt()
        val wholePart = rounded.toInt()
        "$wholePart.${decimalPart.toString().padStart(2, '0')}"
    }

private fun formatDate(timestamp: String): String {
    if (timestamp.isBlank()) {
        return "No date"
    }

    try {
        val dateStr = timestamp.substringBefore("T")
        val parts = dateStr.split("-")
        if (parts.size >= 3) {
            val year = parts[0]
            val month = parts[1].toIntOrNull() ?: 1
            val day = parts[2].toIntOrNull() ?: 1

            val monthNames =
                arrayOf(
                    "January",
                    "February",
                    "March",
                    "April",
                    "May",
                    "June",
                    "July",
                    "August",
                    "September",
                    "October",
                    "November",
                    "December",
                )

            val monthName = if (month in 1..12) monthNames[month - 1] else "January"
            return "$monthName $day, $year"
        }
    } catch (_: Exception) {
        // Fall back to simple format
    }

    return timestamp.substringBefore("T").takeIf { it.isNotBlank() } ?: timestamp
}

private fun parseHexColor(hexColor: String): Color =
    try {
        val cleanHex = hexColor.removePrefix("#")
        val colorInt = cleanHex.toLong(16)
        Color(colorInt or 0xFF000000) // Add alpha if not present
    } catch (_: Exception) {
        Color(0xFF6200EE) // Default purple color
    }

@Composable
private fun DeleteConfirmationDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = DeleteConstants.DELETE_CONFIRMATION_TITLE,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
            )
        },
        text = {
            Text(
                text = DeleteConstants.DELETE_CONFIRMATION_MESSAGE,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors =
                    androidx.compose.material3.ButtonDefaults.buttonColors(
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
