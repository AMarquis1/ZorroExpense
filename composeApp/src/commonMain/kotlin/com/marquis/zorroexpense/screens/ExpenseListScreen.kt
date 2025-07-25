package com.marquis.zorroexpense.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Search
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.layout.layout
import kotlin.math.roundToInt
import org.jetbrains.compose.resources.painterResource
import zorroexpense.composeapp.generated.resources.Res
import zorroexpense.composeapp.generated.resources.zorro_header
import com.marquis.zorroexpense.AppConfig
import com.marquis.zorroexpense.Category
import com.marquis.zorroexpense.Expense
import com.marquis.zorroexpense.FirestoreService
import com.marquis.zorroexpense.MockExpenseData
import com.marquis.zorroexpense.components.CategoryFilterRow
import com.marquis.zorroexpense.components.EmptyState
import com.marquis.zorroexpense.components.ErrorState
import com.marquis.zorroexpense.components.ExpenseCardWithDate
import com.marquis.zorroexpense.components.MonthSeparator
import com.marquis.zorroexpense.components.getMonthYear

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
    var isSearchExpanded by remember { mutableStateOf(false) }
    var showConfigMenu by remember { mutableStateOf(false) }
    val searchFocusRequester = remember { FocusRequester() }
    var sortBy by remember { mutableStateOf(SortOption.DATE_DESC) }
    var collapsedMonths by remember { mutableStateOf(setOf<String>()) }
    var selectedCategories by remember { mutableStateOf(MockExpenseData.allCategories.toSet()) }

    val listState = rememberLazyListState()
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()
    var isFabExpanded by remember { mutableStateOf(true) }

    val filteredExpenses by remember(expenses, searchQuery, sortBy, selectedCategories) {
        derivedStateOf {
            var filtered = expenses
            
            // Apply category filter - show nothing if no categories selected
            filtered = filtered.filter { expense ->
                selectedCategories.contains(expense.category)
            }
            
            // Apply search filter
            if (searchQuery.isNotBlank()) {
                filtered = filtered.filter { expense ->
                    expense.name.contains(searchQuery, ignoreCase = true) ||
                    expense.description.contains(searchQuery, ignoreCase = true) ||
                    expense.price.toString().contains(searchQuery)
                }
            }
            
            filtered.sortedWith(sortBy.comparator)
        }
    }

    val groupedExpenses by remember {
        derivedStateOf {
            filteredExpenses.groupBy { expense -> getMonthYear(expense.date) }
                .toList()
                .sortedByDescending { (monthYear, _) -> monthYear }
                .toMap()
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
    
    // Focus search field when expanded
    LaunchedEffect(isSearchExpanded) {
        if (isSearchExpanded) {
            searchFocusRequester.requestFocus()
        }
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
                windowInsets = WindowInsets(0, 0, 0, 0),
                title = {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .layout { measurable, constraints ->
                                val paddingCompensation = 16.dp.toPx().roundToInt()
                                val adjustedConstraints = constraints.copy(
                                    maxWidth = constraints.maxWidth + paddingCompensation * 2
                                )
                                val placeable = measurable.measure(adjustedConstraints)
                                layout(placeable.width, placeable.height) {
                                    placeable.place(-paddingCompensation, 0)
                                }
                            }
                    ) {
                        // Background image - true full width with custom layout compensation
                        Image(
                            painter = painterResource(Res.drawable.zorro_header),
                            contentDescription = "Zorro header background",
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(110.dp),
                            contentScale = ContentScale.Crop
                        )
                    }
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
                            if (isSearchExpanded) {
                                // Expanded search field
                                OutlinedTextField(
                                    value = searchQuery,
                                    onValueChange = { searchQuery = it },
                                    placeholder = {
                                        Text(
                                            "Search expenses...",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = androidx.compose.ui.graphics.Color.Gray
                                        )
                                    },
                                    leadingIcon = {
                                        Icon(
                                            Icons.Default.Search,
                                            contentDescription = "Search",
                                            tint = androidx.compose.ui.graphics.Color.Gray
                                        )
                                    },
                                    trailingIcon = {
                                        IconButton(
                                            onClick = {
                                                searchQuery = ""
                                                isSearchExpanded = false
                                            }
                                        ) {
                                            Icon(
                                                Icons.Default.Close,
                                                contentDescription = "Close search",
                                                tint = androidx.compose.ui.graphics.Color.Gray
                                            )
                                        }
                                    },
                                    colors = androidx.compose.material3.OutlinedTextFieldDefaults.colors(
                                        focusedContainerColor = androidx.compose.ui.graphics.Color.White,
                                        unfocusedContainerColor = androidx.compose.ui.graphics.Color.White,
                                        focusedTextColor = androidx.compose.ui.graphics.Color.Black,
                                        unfocusedTextColor = androidx.compose.ui.graphics.Color.Black,
                                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                                        unfocusedBorderColor = androidx.compose.ui.graphics.Color.Gray
                                    ),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(bottom = 16.dp)
                                        .focusRequester(searchFocusRequester),
                                    singleLine = true,
                                    shape = RoundedCornerShape(25.dp)
                                )
                            } else {
                                // Collapsed state: App title + search icon + filter icon
                                Text(
                                    text = "Zorro Expense",
                                    style = MaterialTheme.typography.headlineSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = androidx.compose.ui.graphics.Color.White,
                                    modifier = Modifier.weight(1f)
                                )

                                // Search icon button
                                IconButton(
                                    onClick = {
                                        isSearchExpanded = true
                                    }
                                ) {
                                    Icon(
                                        Icons.Default.Search,
                                        contentDescription = "Search",
                                        tint = androidx.compose.ui.graphics.Color.White,
                                        modifier = Modifier.size(28.dp)
                                    )
                                }

                                // Filter dropdown button
                                Box {
                                    IconButton(
                                        onClick = { showConfigMenu = true }
                                    ) {
                                        Icon(
                                            Icons.Default.FilterList,
                                            contentDescription = "Filter and sort",
                                            tint = androidx.compose.ui.graphics.Color.White,
                                            modifier = Modifier.size(28.dp)
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
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = androidx.compose.ui.graphics.Color.Transparent,
                        titleContentColor = androidx.compose.ui.graphics.Color.White
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
                    defaultElevation = 8.dp,
                    pressedElevation = 16.dp
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
                    // Show category filter even when empty, but not in error state
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(
                            top = paddingValues.calculateTopPadding() + 8.dp,
                            bottom = 72.dp
                        )
                    ) {
                        // Category filter row
                        item {
                            CategoryFilterRow(
                                selectedCategories = selectedCategories,
                                onCategoryToggle = { category ->
                                    selectedCategories = if (selectedCategories.contains(category)) {
                                        selectedCategories - category
                                    } else {
                                        selectedCategories + category
                                    }
                                },
                            )
                        }
                        
                        // Empty state message
                        item {
                            if (searchQuery.isNotEmpty()) {
                                EmptyState(
                                    icon = "🔍",
                                    title = "No matching expenses",
                                    description = "Try adjusting your search query to find expenses."
                                )
                            } else if (selectedCategories.isEmpty()) {
                                EmptyState(
                                    icon = "📂",
                                    title = "No categories selected",
                                    description = "Select categories above to view expenses."
                                )
                            } else {
                                EmptyState(
                                    icon = "💸",
                                    title = "No expenses found",
                                    description = "Start tracking your expenses by adding some data to your Firestore collection."
                                )
                            }
                        }
                    }
                }
                
                else -> {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(
                            top = paddingValues.calculateTopPadding() + 8.dp,
                            bottom = with(LocalDensity.current) { 
                                WindowInsets.navigationBars.getBottom(this).toDp() + 72.dp // 56dp FAB + 16dp spacing
                            }
                        )
                    ) {
                        // Category filter row - always first item
                        item {
                            CategoryFilterRow(
                                selectedCategories = selectedCategories,
                                onCategoryToggle = { category ->
                                    selectedCategories = if (selectedCategories.contains(category)) {
                                        selectedCategories - category
                                    } else {
                                        selectedCategories + category
                                    }
                                },
                            )
                        }
                        
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
                        
                        // Display grouped expenses with month separators
                        groupedExpenses.forEach { entry ->
                            val monthYear = entry.key
                            val expensesInMonth = entry.value
                            item(key = "header_$monthYear") {
                                MonthSeparator(
                                    month = monthYear,
                                    isCollapsed = collapsedMonths.contains(monthYear),
                                    onToggleCollapsed = {
                                        collapsedMonths = if (collapsedMonths.contains(monthYear)) {
                                            collapsedMonths - monthYear
                                        } else {
                                            collapsedMonths + monthYear
                                        }
                                    }
                                )
                            }
                            
                            // Animated visibility for expenses
                            items(
                                items = expensesInMonth,
                                key = { expense: Expense -> "expense_${expense.date}_${expense.name}_${expense.price}" }
                            ) { expense: Expense ->
                                AnimatedVisibility(
                                    visible = !collapsedMonths.contains(monthYear),
                                    enter = fadeIn(
                                        animationSpec = tween(durationMillis = 300)
                                    ) + slideInVertically(
                                        animationSpec = tween(durationMillis = 300),
                                        initialOffsetY = { -it / 2 }
                                    ),
                                    exit = fadeOut(
                                        animationSpec = tween(durationMillis = 200)
                                    ) + slideOutVertically(
                                        animationSpec = tween(durationMillis = 200),
                                        targetOffsetY = { -it / 2 }
                                    )
                                ) {
                                    ExpenseCardWithDate(
                                        expense = expense,
                                        onCardClick = { onExpenseClick(expense) }
                                    )
                                }
                            }
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