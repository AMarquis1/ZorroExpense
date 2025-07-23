package com.marquis.zorroexpense.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.marquis.zorroexpense.AppConfig
import com.marquis.zorroexpense.Expense
import com.marquis.zorroexpense.FirestoreService
import com.marquis.zorroexpense.MockExpenseData
import com.marquis.zorroexpense.components.EmptyState
import com.marquis.zorroexpense.components.ErrorState
import com.marquis.zorroexpense.components.ExpenseCard
import zorroexpense.composeapp.generated.resources.Res
import zorroexpense.composeapp.generated.resources.sarah

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpenseListScreen(
    onExpenseClick: (Expense) -> Unit,
    onAddExpense: () -> Unit = {}
) {
    var expenses by remember { mutableStateOf<List<Expense>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var searchQuery by remember { mutableStateOf("") }
    var showConfigMenu by remember { mutableStateOf(false) }
    var sortBy by remember { mutableStateOf(SortOption.DATE_DESC) }
    
    // Scroll state for FAB and TopAppBar behavior
    val listState = rememberLazyListState()
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()
    var isFabExpanded by remember { mutableStateOf(true) }

    // Filter expenses based on search query
    val filteredExpenses by remember {
        derivedStateOf {
            if (searchQuery.isBlank()) {
                expenses.sortedWith(sortBy.comparator)
            } else {
                expenses.filter { expense ->
                    expense.name.contains(searchQuery, ignoreCase = true) ||
                    expense.description.contains(searchQuery, ignoreCase = true) ||
                    expense.price.toString().contains(searchQuery)
                }.sortedWith(sortBy.comparator)
            }
        }
    }

    val loadExpenses: suspend () -> Unit = {
        isLoading = true
        errorMessage = null
        
        if (AppConfig.USE_MOCK_DATA) {
            MockExpenseData.getMockExpenses()
                .onSuccess { expenseList ->
                    expenses = expenseList
                }
                .onFailure { exception ->
                    errorMessage = exception.message ?: "Unknown error occurred"
                }
        } else {
            FirestoreService().getExpenses()
                .onSuccess { expenseList ->
                    expenses = expenseList
                }
                .onFailure { exception ->
                    errorMessage = exception.message ?: "Unknown error occurred"
                }
        }
        
        isLoading = false
    }

    LaunchedEffect(Unit) {
        loadExpenses()
    }
    
    // Monitor scroll behavior for FAB expansion
    LaunchedEffect(listState) {
        var previousFirstVisibleItemIndex = 0
        var previousFirstVisibleItemScrollOffset = 0
        
        snapshotFlow { 
            listState.firstVisibleItemIndex to listState.firstVisibleItemScrollOffset 
        }.collect { (currentIndex, currentOffset) ->
            // Determine scroll direction
            val isScrollingDown = if (currentIndex != previousFirstVisibleItemIndex) {
                currentIndex > previousFirstVisibleItemIndex
            } else {
                currentOffset > previousFirstVisibleItemScrollOffset
            }
            
            // Update FAB expanded state based on scroll direction
            isFabExpanded = !isScrollingDown
            
            // Update previous values
            previousFirstVisibleItemIndex = currentIndex
            previousFirstVisibleItemScrollOffset = currentOffset
        }
    }

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            TopAppBar(
                scrollBehavior = scrollBehavior,
                windowInsets =  WindowInsets(0, 0, 0, 0),
                title = {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(
                                top = with(LocalDensity.current) {
                                    WindowInsets.statusBars.getTop(this).toDp()
                                }
                            ),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Search field taking most of the width
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            placeholder = { 
                                Text(
                                    "Search expenses...",
                                    style = MaterialTheme.typography.bodyMedium
                                ) 
                            },
                            leadingIcon = {
                                Icon(
                                    Icons.Default.Search,
                                    contentDescription = "Search"
                                )
                            },
                            trailingIcon = {
                                if (searchQuery.isNotEmpty()) {
                                    IconButton(
                                        onClick = { searchQuery = "" }
                                    ) {
                                        Icon(
                                            Icons.Default.Close,
                                            contentDescription = "Clear search"
                                        )
                                    }
                                }
                            },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            shape = RoundedCornerShape(25.dp)
                        )
                        
                        // Config wheel button
                        Box {
                            IconButton(
                                onClick = { showConfigMenu = true }
                            ) {
                                Icon(
                                    Icons.Default.Settings,
                                    contentDescription = "Settings",
                                    tint = MaterialTheme.colorScheme.onSurface
                                )
                            }
                            
                            DropdownMenu(
                                expanded = showConfigMenu,
                                onDismissRequest = { showConfigMenu = false }
                            ) {
                                Text(
                                    text = "Sort by",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                                )
                                
                                SortOption.entries.forEach { option ->
                                    DropdownMenuItem(
                                        text = {
                                            Text(
                                                text = option.displayName,
                                                color = if (sortBy == option) 
                                                    MaterialTheme.colorScheme.primary 
                                                else 
                                                    MaterialTheme.colorScheme.onSurface
                                            )
                                        },
                                        onClick = {
                                            sortBy = option
                                            showConfigMenu = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onAddExpense,
                expanded = isFabExpanded,
                icon = {
                    Icon(
                        Icons.Default.Add,
                        contentDescription = "Add expense"
                    )
                },
                text = {
                    Text("Add Expense")
                },
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                elevation = FloatingActionButtonDefaults.elevation(
                    defaultElevation = 6.dp,
                    pressedElevation = 8.dp
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            when {
                errorMessage != null -> {
                    ErrorState(
                        title = "Error loading expenses",
                        message = errorMessage!!
                    )
                }
                
                filteredExpenses.isEmpty() && !isLoading -> {
                    if (searchQuery.isNotEmpty()) {
                        EmptyState(
                            icon = "🔍",
                            title = "No matching expenses",
                            description = "Try adjusting your search query to find expenses."
                        )
                    } else {
                        EmptyState(
                            icon = "💸",
                            title = "No expenses found",
                            description = "Start tracking your expenses by adding some data to your Firestore collection."
                        )
                    }
                }
                
                else -> {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(
                            top = paddingValues.calculateTopPadding() + 8.dp,
                            bottom = with(LocalDensity.current) { 
                                WindowInsets.navigationBars.getBottom(this).toDp() + 8.dp
                            }
                        )
                    ) {
                        // Display search results count if searching
                        if (searchQuery.isNotEmpty()) {
                            item {
                                Text(
                                    text = "${filteredExpenses.size} expense(s) found",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                                )
                            }
                        }
                        
                        items(filteredExpenses) { expense ->
                            ExpenseCard(
                                expense = expense,
                                profileImage = Res.drawable.sarah,
                                onCardClick = { onExpenseClick(expense) }
                            )
                        }
                    }
                }
            }
        }
    }
}

enum class SortOption(val displayName: String, val comparator: Comparator<Expense>) {
    DATE_DESC("Date (Newest first)", compareByDescending { it.date }),
    DATE_ASC("Date (Oldest first)", compareBy { it.date }),
    PRICE_DESC("Price (Highest first)", compareByDescending { it.price }),
    PRICE_ASC("Price (Lowest first)", compareBy { it.price }),
    NAME_ASC("Name (A-Z)", compareBy { it.name.lowercase() }),
    NAME_DESC("Name (Z-A)", compareByDescending { it.name.lowercase() })
}