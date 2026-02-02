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
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.outlined.FolderOpen
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.marquis.zorroexpense.components.ProfileAvatar
import com.marquis.zorroexpense.domain.model.ExpenseList
import com.marquis.zorroexpense.presentation.state.ExpenseListsUiEvent
import com.marquis.zorroexpense.presentation.state.ExpenseListsUiState
import com.marquis.zorroexpense.presentation.viewmodel.ExpenseListsViewModel

@Composable
fun ExpenseListsScreen(
    viewModel: ExpenseListsViewModel,
    onListSelected: (listId: String) -> Unit = {},
    onCreateNewList: () -> Unit = {},
) {
    val uiState by viewModel.uiState.collectAsState()

    // Refresh lists when screen is displayed
    LaunchedEffect(Unit) {
        viewModel.refreshLists()
    }

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
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .background(MaterialTheme.colorScheme.surface),
        ) {
            // Modern gradient header
            ModernHeader()

            // Content based on state
            Box(modifier = Modifier.fillMaxSize()) {
                when (uiState) {
                    ExpenseListsUiState.Loading -> {
                        LoadingState()
                    }

                    is ExpenseListsUiState.Success -> {
                        SuccessState(
                            lists = (uiState as ExpenseListsUiState.Success).lists,
                            onListSelected = { list ->
                                viewModel.onEvent(ExpenseListsUiEvent.SelectList(list.listId))
                                onListSelected(list.listId)
                            },
                        )
                    }

                    ExpenseListsUiState.Empty -> {
                        EmptyState(onCreateNewList = onCreateNewList)
                    }

                    is ExpenseListsUiState.Error -> {
                        ErrorState(
                            message = (uiState as ExpenseListsUiState.Error).message,
                            onRetry = { viewModel.onEvent(ExpenseListsUiEvent.RetryLoad) },
                        )
                    }
                }
            }
        }
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
                ),
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
                ExpenseListCard(
                    list = list,
                    onClick = { onListSelected(list) },
                )
            }
        }
    }
}

@Composable
private fun ExpenseListCard(
    list: ExpenseList,
    onClick: () -> Unit,
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
                containerColor =
                    if (list.isArchived) {
                        MaterialTheme.colorScheme.surfaceContainerLow
                    } else {
                        MaterialTheme.colorScheme.surfaceContainer
                    },
            ),
        elevation =
            CardDefaults.cardElevation(
                defaultElevation = if (isPressed) 2.dp else 6.dp,
            ),
        shape = RoundedCornerShape(16.dp),
    ) {
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                // Icon and content
                Row(
                    modifier = Modifier.weight(1f),
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

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = list.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy((-8).dp),
                        ) {
                            // Display member avatars
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

                            Spacer(modifier = Modifier.width(8.dp))

                            if (list.isArchived) {
                                Text(
                                    text = "Archived",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.error,
                                    fontWeight = FontWeight.SemiBold,
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptyState(onCreateNewList: () -> Unit) {
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
