package com.marquis.zorroexpense.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.marquis.zorroexpense.presentation.state.CreateExpenseListUiEvent
import com.marquis.zorroexpense.presentation.state.CreateExpenseListUiState
import com.marquis.zorroexpense.presentation.viewmodel.CreateExpenseListViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateExpenseListScreen(
    viewModel: CreateExpenseListViewModel,
    onBackClick: () -> Unit = {},
    onListCreated: (listId: String, listName: String) -> Unit = { _, _ -> },
) {
    val uiState by viewModel.uiState.collectAsState()
    val listName by viewModel.listName.collectAsState()
    val availableCategories by viewModel.availableCategories.collectAsState()
    val selectedCategories by viewModel.selectedCategories.collectAsState()
    val isEditMode = viewModel.isEditMode

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    // Show error as snackbar
    if (uiState is CreateExpenseListUiState.Error) {
        val message = (uiState as CreateExpenseListUiState.Error).message
        scope.launch {
            snackbarHostState.showSnackbar(message)
            viewModel.onEvent(CreateExpenseListUiEvent.ClearError)
        }
    }

    // Navigate on success
    if (uiState is CreateExpenseListUiState.Success) {
        val state = uiState as CreateExpenseListUiState.Success
        onListCreated(state.listId, state.listName)
        onBackClick()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isEditMode) "Edit List" else "Create New List") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                        )
                    }
                },
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { paddingValues ->
        if (uiState is CreateExpenseListUiState.Loading) {
            // Loading state
            Column(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                CircularProgressIndicator()
                Spacer(modifier = Modifier.height(16.dp))
                Text(if (isEditMode) "Updating list..." else "Creating list...")
            }
        } else {
            // Form
            Column(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .background(MaterialTheme.colorScheme.surface),
            ) {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    // List name input
                    item {
                        Column {
                            Text(
                                text = "List Name",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.SemiBold,
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            TextField(
                                value = listName,
                                onValueChange = { newName ->
                                    viewModel.onEvent(CreateExpenseListUiEvent.ListNameChanged(newName))
                                },
                                modifier = Modifier.fillMaxWidth(),
                                placeholder = { Text("e.g., Vacation Trip") },
                                singleLine = true,
                            )
                        }
                    }

                    // Categories selection
                    if (availableCategories.isNotEmpty()) {
                        item {
                            Text(
                                text = "Categories",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.SemiBold,
                            )
                        }

                        items(availableCategories) { category ->
                            Row(
                                modifier =
                                    Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            val newSelected = selectedCategories.toMutableSet()
                                            if (newSelected.contains(category.documentId)) {
                                                newSelected.remove(category.documentId)
                                            } else {
                                                newSelected.add(category.documentId)
                                            }
                                            viewModel.onEvent(
                                                CreateExpenseListUiEvent.CategoriesSelected(newSelected.toList()),
                                            )
                                        }.padding(vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Checkbox(
                                    checked = selectedCategories.contains(category.documentId),
                                    onCheckedChange = { isChecked ->
                                        val newSelected = selectedCategories.toMutableSet()
                                        if (isChecked) {
                                            newSelected.add(category.documentId)
                                        } else {
                                            newSelected.remove(category.documentId)
                                        }
                                        viewModel.onEvent(
                                            CreateExpenseListUiEvent.CategoriesSelected(newSelected.toList()),
                                        )
                                    },
                                )
                                Spacer(modifier = Modifier.padding(8.dp))
                                Text(
                                    text = category.name,
                                    style = MaterialTheme.typography.bodyMedium,
                                )
                            }
                        }
                    }

                    item {
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }

                // Create/Update button
                Button(
                    onClick = { viewModel.onEvent(CreateExpenseListUiEvent.CreateList) },
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                            .height(48.dp),
                    enabled = listName.isNotBlank() && uiState !is CreateExpenseListUiState.Loading,
                ) {
                    Icon(
                        imageVector = Icons.Filled.Check,
                        contentDescription = if (isEditMode) "Update" else "Create",
                        modifier = Modifier.padding(end = 8.dp),
                    )
                    Text(if (isEditMode) "Update List" else "Create List")
                }
            }
        }
    }
}
