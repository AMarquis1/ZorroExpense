package com.marquis.zorroexpense.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Error
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
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
    // Note: This effect runs every time the screen enters composition
    LaunchedEffect(Unit) {
        viewModel.refreshLists()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
    ) {
        // Header
        Text(
            text = "My Expense Lists",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(16.dp)
        )

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
                        onCreateNewList = onCreateNewList
                    )
                }

                ExpenseListsUiState.Empty -> {
                    EmptyState(onCreateNewList = onCreateNewList)
                }

                is ExpenseListsUiState.Error -> {
                    ErrorState(
                        message = (uiState as ExpenseListsUiState.Error).message,
                        onRetry = { viewModel.onEvent(ExpenseListsUiEvent.RetryLoad) }
                    )
                }
            }
        }
    }
}

@Composable
private fun LoadingState() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}

@Composable
private fun SuccessState(
    lists: List<ExpenseList>,
    onListSelected: (ExpenseList) -> Unit,
    onCreateNewList: () -> Unit,
) {
    Column(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(lists) { list ->
                ExpenseListCard(
                    list = list,
                    onClick = { onListSelected(list) }
                )
            }
        }

        // Create new list button
        Button(
            onClick = onCreateNewList,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .height(48.dp),
            shape = MaterialTheme.shapes.medium
        ) {
            Icon(
                imageVector = Icons.Filled.Add,
                contentDescription = "Create new list",
                modifier = Modifier.padding(end = 8.dp)
            )
            Text("Create New List")
        }
    }
}

@Composable
private fun ExpenseListCard(
    list: ExpenseList,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = list.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "${list.members.size} member${if (list.members.size != 1) "s" else ""}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                if (list.isArchived) {
                    Text(
                        text = "Archived",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}

@Composable
private fun EmptyState(onCreateNewList: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Filled.Add,
            contentDescription = "No lists",
            modifier = Modifier
                .width(64.dp)
                .height(64.dp),
            tint = MaterialTheme.colorScheme.outline
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "No Expense Lists Yet",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Create a new expense list to get started",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(
            onClick = onCreateNewList,
            modifier = Modifier.height(48.dp)
        ) {
            Icon(
                imageVector = Icons.Filled.Add,
                contentDescription = "Create new list",
                modifier = Modifier.padding(end = 8.dp)
            )
            Text("Create New List")
        }
    }
}

@Composable
private fun ErrorState(
    message: String,
    onRetry: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Filled.Error,
            contentDescription = "Error",
            modifier = Modifier
                .width(64.dp)
                .height(64.dp),
            tint = MaterialTheme.colorScheme.error
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Error Loading Lists",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.error
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(
            onClick = onRetry,
            modifier = Modifier.height(48.dp)
        ) {
            Text("Retry")
        }
    }
}
