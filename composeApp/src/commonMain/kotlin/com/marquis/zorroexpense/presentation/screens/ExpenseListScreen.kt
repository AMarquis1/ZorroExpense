package com.marquis.zorroexpense.presentation.screens

import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
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
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
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
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.marquis.zorroexpense.components.CategoryFilterRow
import com.marquis.zorroexpense.components.CategoryFilterRowSkeleton
import com.marquis.zorroexpense.components.EmptyState
import com.marquis.zorroexpense.components.ErrorState
import com.marquis.zorroexpense.components.ExpenseCardSkeleton
import com.marquis.zorroexpense.components.ExpenseCardWithDate
import com.marquis.zorroexpense.components.MonthSeparator
import com.marquis.zorroexpense.components.MonthSeparatorSkeleton
import com.marquis.zorroexpense.components.getMonthYear
import com.marquis.zorroexpense.domain.model.Category
import com.marquis.zorroexpense.domain.model.Expense
import com.marquis.zorroexpense.platform.pullToRefreshBox
import com.marquis.zorroexpense.presentation.components.CustomDeleteSnackbar
import com.marquis.zorroexpense.presentation.components.DebtSummaryBar
import com.marquis.zorroexpense.presentation.components.DebtSummaryBarSkeleton
import com.marquis.zorroexpense.presentation.constants.DeleteConstants
import com.marquis.zorroexpense.presentation.state.ExpenseListUiEvent
import com.marquis.zorroexpense.presentation.state.ExpenseListUiState
import com.marquis.zorroexpense.presentation.state.SortOption
import com.marquis.zorroexpense.presentation.viewmodel.ExpenseListViewModel
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.compose.resources.painterResource
import zorroexpense.composeapp.generated.resources.Res
import zorroexpense.composeapp.generated.resources.zorro_header
import kotlin.math.roundToInt

/**
 * Utility function to check if an expense date is in the future
 */
@OptIn(kotlin.time.ExperimentalTime::class)
private fun isFutureExpense(expenseDate: String): Boolean =
    try {
        val today =
            kotlin.time.Clock.System
                .now()
                .toLocalDateTime(TimeZone.currentSystemDefault())
                .date
        val expenseLocalDate = LocalDate.parse(expenseDate.substringBefore("T")) // Handle both ISO format and date-only
        expenseLocalDate > today
    } catch (e: Exception) {
        false // If parsing fails, treat as not future
    }

/**
 * Upcoming expenses separator component with chevron toggle
 */
@Composable
private fun UpcomingExpensesSeparator(
    showUpcomingExpenses: Boolean,
    onToggleUpcomingExpenses: () -> Unit,
    expenseCount: Int,
    modifier: Modifier = Modifier,
) {
    // Interaction source for press feedback
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    // Animate the arrow rotation
    val rotationAngle by animateFloatAsState(
        targetValue = if (showUpcomingExpenses) 0f else -90f,
        animationSpec = tween(durationMillis = 300),
        label = "arrow_rotation",
    )

    // Animate press scale
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        animationSpec = tween(durationMillis = 100),
        label = "press_scale",
    )

    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                }.clickable(
                    interactionSource = interactionSource,
                    indication = null, // We handle the feedback with scale animation
                ) { onToggleUpcomingExpenses() }
                .padding(horizontal = 16.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // Left line
        Box(
            modifier =
                Modifier
                    .weight(1f)
                    .height(1.dp)
                    .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)),
        )

        // Label with count
        Text(
            text = "Upcoming expenses ($expenseCount)",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(horizontal = 12.dp),
        )

        // Right line
        Box(
            modifier =
                Modifier
                    .weight(1f)
                    .height(1.dp)
                    .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)),
        )

        // Animated collapse/expand indicator
        Text(
            text = "▼",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.primary,
            modifier =
                Modifier
                    .padding(start = 12.dp)
                    .graphicsLayer {
                        rotationZ = rotationAngle
                    },
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalSharedTransitionApi::class)
@Composable
fun ExpenseListScreen(
    viewModel: ExpenseListViewModel,
    sharedTransitionScope: SharedTransitionScope,
    animatedContentScope: AnimatedContentScope,
    deletedExpenseName: String? = null,
    onUndoDelete: () -> Unit = {},
    onConfirmDelete: () -> Unit = {},
    onDeleteFlowComplete: () -> Unit = {},
    updatedExpenseName: String? = null,
    onUpdateFlowComplete: () -> Unit = {},
) {
    val uiState by viewModel.uiState.collectAsState()
    val availableCategories by viewModel.availableCategories.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }

    // Handle delete snackbar with integrated timer
    // This LaunchedEffect manages both the snackbar display AND the auto-delete timer
    LaunchedEffect(deletedExpenseName) {
        if (deletedExpenseName != null) {
            // Show snackbar and wait for result
            val result =
                snackbarHostState.showSnackbar(
                    message = "Expense \"$deletedExpenseName\" has been deleted",
                    actionLabel = DeleteConstants.UNDO_BUTTON_TEXT,
                    duration = DeleteConstants.SNACKBAR_DURATION,
                )

            when (result) {
                SnackbarResult.ActionPerformed -> {
                    // User clicked UNDO
                    onUndoDelete()
                    onDeleteFlowComplete()
                }
                SnackbarResult.Dismissed -> {
                    // Snackbar was dismissed (by timeout or swipe) - confirm delete
                    onConfirmDelete()
                    onDeleteFlowComplete()
                }
            }
        }
    }

    // Auto-dismiss snackbar after delay (since we're using Indefinite duration)
    LaunchedEffect(deletedExpenseName) {
        if (deletedExpenseName != null) {
            kotlinx.coroutines.delay(DeleteConstants.AUTO_DELETE_DELAY_MS)
            // This will trigger the Dismissed result above
            snackbarHostState.currentSnackbarData?.dismiss()
        }
    }

    // Handle update snackbar - simple notification without undo
    LaunchedEffect(updatedExpenseName) {
        if (updatedExpenseName != null) {
            snackbarHostState.showSnackbar(
                message = "\"$updatedExpenseName\" has been updated",
                duration = SnackbarDuration.Short,
            )
            onUpdateFlowComplete()
        }
    }

    // Local UI state for things not managed by ViewModel
    var showConfigMenu by remember { mutableStateOf(false) }
    val searchFocusRequester = remember { FocusRequester() }

    val listState = rememberLazyListState()
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()
    var isFabExpanded by remember { mutableStateOf(true) }

    // Extract state values from the current UI state
    val currentState = uiState
    val expenses = if (currentState is ExpenseListUiState.Success) currentState.expenses else emptyList()
    val filteredExpenses = if (currentState is ExpenseListUiState.Success) currentState.filteredExpenses else emptyList()
    val searchQuery = if (currentState is ExpenseListUiState.Success) currentState.searchQuery else ""
    val isSearchExpanded = if (currentState is ExpenseListUiState.Success) currentState.isSearchExpanded else false
    val selectedCategories = if (currentState is ExpenseListUiState.Success) currentState.selectedCategories else emptySet()
    val sortBy = if (currentState is ExpenseListUiState.Success) currentState.sortOption else SortOption.DATE_DESC
    val collapsedMonths = if (currentState is ExpenseListUiState.Success) currentState.collapsedMonths else emptySet()
    val isLoading = currentState is ExpenseListUiState.Loading
    val isRefreshing = if (currentState is ExpenseListUiState.Success) currentState.isRefreshing else false
    val showUpcoming = if (currentState is ExpenseListUiState.Success) currentState.showUpcomingExpenses else true
    val errorMessage = if (currentState is ExpenseListUiState.Error) currentState.message else null

    // Separate current/past and future expenses
    val (currentExpenses, futureExpenses) =
        remember(filteredExpenses) {
            filteredExpenses.partition { expense -> !isFutureExpense(expense.date) }
        }

    val groupedCurrentExpenses =
        remember(currentExpenses) {
            currentExpenses
                .groupBy { expense -> getMonthYear(expense.date) }
                .toList()
                .sortedByDescending { (_, expenses) ->
                    // Sort by the most recent date in each month group
                    expenses.maxOfOrNull { it.date } ?: ""
                }.associate { (monthYear, expenses) ->
                    // Sort expenses within each month by date descending (most recent first)
                    monthYear to expenses.sortedByDescending { it.date }
                }
        }

    val groupedFutureExpenses =
        remember(futureExpenses) {
            futureExpenses
                .groupBy { expense -> getMonthYear(expense.date) }
                .toList()
                .sortedBy { (_, expenses) ->
                    // Sort by the earliest date in each month group (ascending for future)
                    expenses.minOfOrNull { it.date } ?: ""
                }.associate { (monthYear, expenses) ->
                    // Sort expenses within each month by date ascending (soonest first)
                    monthYear to expenses.sortedBy { it.date }
                }
        }

    LaunchedEffect(isSearchExpanded) {
        if (isSearchExpanded) {
            searchFocusRequester.requestFocus()
        }
    }

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

            // Update FAB expanded state based on scroll direction
            isFabExpanded = !isScrollingDown

            // Update previous values
            previousFirstVisibleItemIndex = currentIndex
            previousFirstVisibleItemScrollOffset = currentOffset
        }
    }

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState) { snackbarData ->
                CustomDeleteSnackbar(snackbarData = snackbarData)
            }
        },
        topBar = {
            TopAppBar(
                scrollBehavior = scrollBehavior,
                windowInsets = WindowInsets(0, 0, 0, 0),
                title = {
                    Box(
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .layout { measurable, constraints ->
                                    val paddingCompensation = 16.dp.toPx().roundToInt()
                                    val adjustedConstraints =
                                        constraints.copy(
                                            maxWidth = constraints.maxWidth + paddingCompensation * 2,
                                        )
                                    val placeable = measurable.measure(adjustedConstraints)
                                    layout(placeable.width, placeable.height) {
                                        placeable.place(-paddingCompensation, 0)
                                    }
                                },
                    ) {
                        // Background image - true full width with custom layout compensation
                        Image(
                            painter = painterResource(Res.drawable.zorro_header),
                            contentDescription = "Zorro header background",
                            modifier =
                                Modifier
                                    .fillMaxWidth()
                                    .height(
                                        60.dp +
                                            with(LocalDensity.current) {
                                                WindowInsets.statusBars.getTop(this).toDp()
                                            },
                                    ),
                            contentScale = ContentScale.Crop,
                        )
                    }
                    Row(
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .padding(
                                    top =
                                        with(LocalDensity.current) {
                                            WindowInsets.statusBars.getTop(this).toDp()
                                        },
                                ),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        if (isSearchExpanded) {
                            // Expanded search field
                            OutlinedTextField(
                                value = searchQuery,
                                onValueChange = { query -> viewModel.onEvent(ExpenseListUiEvent.SearchQueryChanged(query)) },
                                placeholder = {
                                    Text(
                                        "Search expenses...",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = androidx.compose.ui.graphics.Color.Gray,
                                    )
                                },
                                leadingIcon = {
                                    Icon(
                                        Icons.Default.Search,
                                        contentDescription = "Search",
                                        tint = androidx.compose.ui.graphics.Color.Gray,
                                    )
                                },
                                trailingIcon = {
                                    IconButton(
                                        onClick = {
                                            viewModel.onEvent(ExpenseListUiEvent.SearchQueryChanged(""))
                                            viewModel.onEvent(ExpenseListUiEvent.SearchExpandedChanged(false))
                                        },
                                    ) {
                                        Icon(
                                            Icons.Default.Close,
                                            contentDescription = "Close search",
                                            tint = androidx.compose.ui.graphics.Color.Gray,
                                        )
                                    }
                                },
                                colors =
                                    androidx.compose.material3.OutlinedTextFieldDefaults.colors(
                                        focusedContainerColor = androidx.compose.ui.graphics.Color.White,
                                        unfocusedContainerColor = androidx.compose.ui.graphics.Color.White,
                                        focusedTextColor = androidx.compose.ui.graphics.Color.Black,
                                        unfocusedTextColor = androidx.compose.ui.graphics.Color.Black,
                                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                                        unfocusedBorderColor = androidx.compose.ui.graphics.Color.Gray,
                                    ),
                                modifier =
                                    Modifier
                                        .fillMaxWidth()
                                        .padding(bottom = 16.dp)
                                        .focusRequester(searchFocusRequester),
                                singleLine = true,
                                shape = RoundedCornerShape(25.dp),
                            )
                        } else {
                            // Collapsed state: App title + search icon + filter icon
                            Text(
                                text = "Zorro Expense",
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold,
                                color = androidx.compose.ui.graphics.Color.White,
                                modifier = Modifier.weight(1f),
                            )

                            // Search icon button
                            IconButton(
                                onClick = {
                                    viewModel.onEvent(ExpenseListUiEvent.SearchExpandedChanged(true))
                                },
                            ) {
                                Icon(
                                    Icons.Default.Search,
                                    contentDescription = "Search",
                                    tint = androidx.compose.ui.graphics.Color.White,
                                    modifier = Modifier.size(28.dp),
                                )
                            }

                            // Filter dropdown button
                            Box {
                                IconButton(
                                    onClick = { showConfigMenu = true },
                                ) {
                                    Icon(
                                        Icons.Default.FilterList,
                                        contentDescription = "Filter and sort",
                                        tint = androidx.compose.ui.graphics.Color.White,
                                        modifier = Modifier.size(28.dp),
                                    )
                                }

                                DropdownMenu(
                                    expanded = showConfigMenu,
                                    onDismissRequest = { showConfigMenu = false },
                                ) {
                                    Text(
                                        text = "Sort by",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                                    )

                                    SortOption.entries.forEach { option ->
                                        DropdownMenuItem(
                                            text = {
                                                Text(
                                                    text = option.displayName,
                                                    color =
                                                        if (sortBy == option) {
                                                            MaterialTheme.colorScheme.primary
                                                        } else {
                                                            MaterialTheme.colorScheme.onSurface
                                                        },
                                                )
                                            },
                                            onClick = {
                                                viewModel.onEvent(ExpenseListUiEvent.SortOptionChanged(option))
                                                showConfigMenu = false
                                            },
                                        )
                                    }
                                }
                            }
                        }
                    }
                },
                colors =
                    TopAppBarDefaults.topAppBarColors(
                        containerColor = androidx.compose.ui.graphics.Color.Transparent,
                        titleContentColor = androidx.compose.ui.graphics.Color.White,
                    ),
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { viewModel.onEvent(ExpenseListUiEvent.AddExpenseClicked) },
                expanded = isFabExpanded,
                icon = {
                    Icon(
                        Icons.Default.Add,
                        contentDescription = "Add expense",
                    )
                },
                text = {
                    Text("Add Expense")
                },
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                elevation =
                    FloatingActionButtonDefaults.elevation(
                        defaultElevation = 8.dp,
                        pressedElevation = 16.dp,
                    ),
            )
        },
    ) { paddingValues ->
        pullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = { viewModel.onEvent(ExpenseListUiEvent.RefreshExpenses) },
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(top = paddingValues.calculateTopPadding()),
        ) {
            when {
                errorMessage != null -> {
                    ErrorState(
                        title = "Error loading expenses",
                        message = errorMessage,
                    )
                }

                isLoading -> {
                    // Show shimmer skeleton placeholders while loading
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding =
                            PaddingValues(
                                top = 8.dp,
                                bottom = 72.dp,
                            ),
                    ) {
                        // Debt summary skeleton
                        item {
                            DebtSummaryBarSkeleton()
                        }

                        // Category filter skeleton
                        item {
                            CategoryFilterRowSkeleton()
                        }

                        // Month separator skeleton
                        item {
                            MonthSeparatorSkeleton()
                        }

                        // Expense card skeletons
                        items(5) {
                            ExpenseCardSkeleton()
                        }
                    }
                }

                filteredExpenses.isEmpty() && !isLoading -> {
                    // Show category filter even when empty, but not in error state
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding =
                            PaddingValues(
                                top = 8.dp,
                                bottom = 72.dp,
                            ),
                    ) {
                        // Debt summary bar - first item if there are debts to show
                        if (currentState is ExpenseListUiState.Success && currentState.debtSummaries.isNotEmpty()) {
                            item {
                                DebtSummaryBar(
                                    debtSummaries = currentState.debtSummaries,
                                )
                            }
                        }

                        // Category filter row
                        item {
                            CategoryFilterRow(
                                categories = availableCategories,
                                selectedCategories = selectedCategories,
                                onCategoryToggle = { category: Category ->
                                    viewModel.onEvent(ExpenseListUiEvent.CategoryToggled(category))
                                },
                            )
                        }

                        // Empty state message
                        item {
                            if (searchQuery.isNotEmpty()) {
                                EmptyState(
                                    icon = "🔍",
                                    title = "No matching expenses",
                                    description = "Try adjusting your search query to find expenses.",
                                )
                            } else if (selectedCategories.isNotEmpty()) {
                                EmptyState(
                                    icon = "📂",
                                    title = "No expenses in selected categories",
                                    description = "Try selecting different categories or add expenses to these categories.",
                                )
                            } else {
                                EmptyState(
                                    icon = "💸",
                                    title = "No expenses found",
                                    description = "Start tracking your expenses by adding some data.",
                                )
                            }
                        }
                    }
                }

                else -> {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier.fillMaxSize(),
                        contentPadding =
                            PaddingValues(
                                top = 8.dp,
                                bottom =
                                    with(LocalDensity.current) {
                                        WindowInsets.navigationBars.getBottom(this).toDp() + 72.dp // 56dp FAB + 16dp spacing
                                    },
                            ),
                    ) {
                        // Debt summary bar - first item if there are debts to show
                        if (currentState is ExpenseListUiState.Success && currentState.debtSummaries.isNotEmpty()) {
                            item {
                                DebtSummaryBar(
                                    debtSummaries = currentState.debtSummaries,
                                )
                            }
                        }

                        // Category filter row - always after debt summary
                        item {
                            CategoryFilterRow(
                                categories = availableCategories,
                                selectedCategories = selectedCategories,
                                onCategoryToggle = { category: Category ->
                                    viewModel.onEvent(ExpenseListUiEvent.CategoryToggled(category))
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
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                                )
                            }
                        }

                        // Add upcoming expenses separator if there are future expenses (AT TOP)
                        if (futureExpenses.isNotEmpty()) {
                            item(key = "upcoming_separator") {
                                UpcomingExpensesSeparator(
                                    showUpcomingExpenses = showUpcoming,
                                    onToggleUpcomingExpenses = { viewModel.onEvent(ExpenseListUiEvent.ToggleUpcomingExpenses) },
                                    expenseCount = futureExpenses.size,
                                )
                            }
                        }

                        // Display future expenses with month separators (AT TOP, only if toggle is on)
                        if (showUpcoming) {
                            groupedFutureExpenses.forEach { entry ->
                                val monthYear = entry.key
                                val expensesInMonth = entry.value
                                item(key = "header_future_$monthYear") {
                                    MonthSeparator(
                                        month = monthYear,
                                        isCollapsed = collapsedMonths.contains(monthYear),
                                        onToggleCollapsed = {
                                            viewModel.onEvent(ExpenseListUiEvent.MonthToggleCollapsed(monthYear))
                                        },
                                    )
                                }

                                // Animated visibility for future expenses
                                items(
                                    items = expensesInMonth,
                                    key = { expense: Expense -> "expense_future_${expense.documentId}" },
                                ) { expense: Expense ->
                                    AnimatedVisibility(
                                        visible = !collapsedMonths.contains(monthYear),
                                        enter =
                                            fadeIn(
                                                animationSpec = tween(durationMillis = 300),
                                            ) +
                                                slideInVertically(
                                                    animationSpec = tween(durationMillis = 300),
                                                    initialOffsetY = { -it / 2 },
                                                ),
                                        exit =
                                            fadeOut(
                                                animationSpec = tween(durationMillis = 200),
                                            ) +
                                                slideOutVertically(
                                                    animationSpec = tween(durationMillis = 200),
                                                    targetOffsetY = { -it / 2 },
                                                ),
                                    ) {
                                        ExpenseCardWithDate(
                                            expense = expense,
                                            onCardClick = { viewModel.onEvent(ExpenseListUiEvent.ExpenseClicked(expense)) },
                                            sharedTransitionScope = sharedTransitionScope,
                                            animatedContentScope = animatedContentScope,
                                        )
                                    }
                                }
                            }
                        } // End of if (showUpcoming)

                        // Display current/past expenses with month separators (BELOW upcoming expenses)
                        groupedCurrentExpenses.forEach { entry ->
                            val monthYear = entry.key
                            val expensesInMonth = entry.value
                            item(key = "header_current_$monthYear") {
                                MonthSeparator(
                                    month = monthYear,
                                    isCollapsed = collapsedMonths.contains(monthYear),
                                    onToggleCollapsed = {
                                        viewModel.onEvent(ExpenseListUiEvent.MonthToggleCollapsed(monthYear))
                                    },
                                )
                            }

                            // Animated visibility for current expenses
                            items(
                                items = expensesInMonth,
                                key = { expense: Expense -> "expense_current_${expense.documentId}" },
                            ) { expense: Expense ->
                                AnimatedVisibility(
                                    visible = !collapsedMonths.contains(monthYear),
                                    enter =
                                        fadeIn(
                                            animationSpec = tween(durationMillis = 300),
                                        ) +
                                            slideInVertically(
                                                animationSpec = tween(durationMillis = 300),
                                                initialOffsetY = { -it / 2 },
                                            ),
                                    exit =
                                        fadeOut(
                                            animationSpec = tween(durationMillis = 200),
                                        ) +
                                            slideOutVertically(
                                                animationSpec = tween(durationMillis = 200),
                                                targetOffsetY = { -it / 2 },
                                            ),
                                ) {
                                    ExpenseCardWithDate(
                                        expense = expense,
                                        onCardClick = { viewModel.onEvent(ExpenseListUiEvent.ExpenseClicked(expense)) },
                                        sharedTransitionScope = sharedTransitionScope,
                                        animatedContentScope = animatedContentScope,
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
