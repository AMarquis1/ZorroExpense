package com.marquis.zorroexpense.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.marquis.zorroexpense.domain.model.Category
import com.marquis.zorroexpense.domain.model.Expense
import com.marquis.zorroexpense.domain.usecase.CalculateDebtsUseCase
import com.marquis.zorroexpense.domain.usecase.DeleteExpenseUseCase
import com.marquis.zorroexpense.domain.usecase.GetCategoriesUseCase
import com.marquis.zorroexpense.domain.usecase.GetExpensesByListIdUseCase
import com.marquis.zorroexpense.presentation.state.ExpenseListUiEvent
import com.marquis.zorroexpense.presentation.state.ExpenseListUiState
import com.marquis.zorroexpense.presentation.state.SortOption
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

class ExpenseListViewModel(
    private val userId: String,
    private val listId: String,
    private val getExpensesByListIdUseCase: GetExpensesByListIdUseCase,
    private val getCategoriesUseCase: GetCategoriesUseCase,
    private val deleteExpenseUseCase: DeleteExpenseUseCase,
    private val calculateDebtsUseCase: CalculateDebtsUseCase,
    private var onExpenseClick: (Expense) -> Unit = {},
    private var onAddExpenseClick: () -> Unit = {},
) : ViewModel() {
    private val _uiState = MutableStateFlow<ExpenseListUiState>(ExpenseListUiState.Loading)
    val uiState: StateFlow<ExpenseListUiState> = _uiState.asStateFlow()

    private val _availableCategories = MutableStateFlow<List<Category>>(emptyList())
    val availableCategories: StateFlow<List<Category>> = _availableCategories.asStateFlow()

    private var hasLoadedOnce = false

    /**
     * Utility function to check if an expense date is in the future
     */
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
     * Calculate debts only for current/past expenses (not future ones)
     */
    private fun calculateDebtsFromExpenses(expenses: List<Expense>): List<com.marquis.zorroexpense.domain.model.DebtSummary> {
        val currentExpenses = expenses.filter { !isFutureExpense(it.date) }
        return calculateDebtsUseCase(currentExpenses)
    }

    init {
        // Check if we already have data loaded, if not load from cache or fetch
        val currentState = _uiState.value
        if (currentState !is ExpenseListUiState.Success || !currentState.hasInitiallyLoaded) {
            loadExpenses(isRefresh = false)
        }
        hasLoadedOnce = true
    }

    fun updateCallbacks(
        onExpenseClick: (Expense) -> Unit,
        onAddExpenseClick: () -> Unit,
    ) {
        this.onExpenseClick = onExpenseClick
        this.onAddExpenseClick = onAddExpenseClick
    }

    /**
     * Ensure data is loaded when returning from navigation.
     * Uses cache if available, only fetches from network if cache is empty or expired.
     */
    fun ensureDataLoaded() {
        val currentState = _uiState.value
        // Only load if we don't have data or haven't loaded initially
        if (currentState !is ExpenseListUiState.Success || !currentState.hasInitiallyLoaded) {
            loadExpenses(isRefresh = false)
        }
    }

    fun onEvent(event: ExpenseListUiEvent) {
        when (event) {
            is ExpenseListUiEvent.LoadExpenses -> loadExpenses(isRefresh = false)
            is ExpenseListUiEvent.RefreshExpenses -> loadExpenses(isRefresh = true)
            is ExpenseListUiEvent.SearchQueryChanged -> updateSearchQuery(event.query)
            is ExpenseListUiEvent.SearchExpandedChanged -> updateSearchExpanded(event.isExpanded)
            is ExpenseListUiEvent.CategoryToggled -> toggleCategory(event.category)
            is ExpenseListUiEvent.SortOptionChanged -> updateSortOption(event.sortOption)
            is ExpenseListUiEvent.MonthToggleCollapsed -> toggleMonth(event.monthYear)
            is ExpenseListUiEvent.FabExpandedChanged -> updateFabExpanded(event.isExpanded)
            is ExpenseListUiEvent.ExpenseClicked -> handleExpenseClick(event.expense)
            is ExpenseListUiEvent.AddExpenseClicked -> handleAddExpenseClick()
            is ExpenseListUiEvent.PendingDeleteExpense -> markForPendingDeletion(event.expenseId)
            is ExpenseListUiEvent.UndoDeleteExpense -> undoDeleteExpense(event.expenseId)
            is ExpenseListUiEvent.ConfirmDeleteExpense -> confirmDeleteExpense(event.expenseId)
            is ExpenseListUiEvent.ToggleUpcomingExpenses -> toggleUpcomingExpenses()
        }
    }

    private fun loadExpenses(isRefresh: Boolean = false) {
        viewModelScope.launch {
            val currentState = _uiState.value

            // For initial load (no existing data), show loading state
            // For refresh OR reload with existing data, keep UI state visible and set isRefreshing = true
            // This prevents flickering of collapsed/expanded months during navigation
            if (currentState is ExpenseListUiState.Success) {
                _uiState.value = currentState.copy(isRefreshing = true)
            } else if (!isRefresh && hasLoadedOnce) {
                // On navigation back to list: keep the previous success state visible while fetching fresh data
                // This provides instant UI feedback from cache without showing loading state
                // The repository's cache-first strategy ensures data loads from cache immediately
                // We'll update the data once fresh data arrives
            } else if (!isRefresh) {
                // Only show Loading on very first load when we have no data yet
                _uiState.value = ExpenseListUiState.Loading
            }

            // Load both expenses and categories
            // Always load by listId since it's required
            val expensesResult = getExpensesByListIdUseCase(listId)
            val categoriesResult = getCategoriesUseCase()

            if (expensesResult.isSuccess && categoriesResult.isSuccess) {
                val expenses = expensesResult.getOrThrow()
                val categories = categoriesResult.getOrThrow()

                // Update available categories for the UI
                _availableCategories.value = categories

                // Preserve existing UI state (especially collapsedMonths) from previous state
                // This ensures user's collapsed/expanded preferences survive reloads
                val existingState =
                    if (currentState is ExpenseListUiState.Success) {
                        currentState
                    } else {
                        ExpenseListUiState.Success()
                    }

                val selectedCats =
                    if (isRefresh && currentState is ExpenseListUiState.Success) {
                        currentState.selectedCategories
                    } else {
                        categories.toSet() // Use all categories for initial load
                    }

                // Preserve pending deletions when we have existing state
                // This ensures items being deleted stay hidden during any reload
                val preservedPendingDeletions =
                    if (currentState is ExpenseListUiState.Success) {
                        currentState.pendingDeletions
                    } else {
                        emptySet()
                    }

                // When refreshing with pending deletions, we need to merge the new expenses
                // with any expenses that are pending deletion (so they can be restored)
                val finalExpenses =
                    if (isRefresh && currentState is ExpenseListUiState.Success && preservedPendingDeletions.isNotEmpty()) {
                        // Get expenses that are pending deletion from current state
                        val pendingExpenses =
                            currentState.expenses.filter { expense ->
                                preservedPendingDeletions.contains(expense.documentId)
                            }
                        // Merge new expenses with pending expenses, avoiding duplicates
                        (expenses + pendingExpenses).distinctBy { it.documentId }
                    } else {
                        expenses
                    }

                val debtSummaries = calculateDebtsFromExpenses(finalExpenses)

                val newState =
                    ExpenseListUiState.Success(
                        expenses = finalExpenses,
                        filteredExpenses = finalExpenses,
                        selectedCategories = selectedCats,
                        searchQuery = existingState.searchQuery,
                        isSearchExpanded = existingState.isSearchExpanded,
                        sortOption = existingState.sortOption,
                        collapsedMonths = existingState.collapsedMonths,
                        isFabExpanded = existingState.isFabExpanded,
                        isRefreshing = false,
                        hasInitiallyLoaded = true,
                        pendingDeletions = preservedPendingDeletions,
                        showUpcomingExpenses = existingState.showUpcomingExpenses,
                        debtSummaries = debtSummaries,
                    )

                // Apply filtering with current filters
                _uiState.value =
                    newState.copy(
                        filteredExpenses =
                            filterExpenses(
                                expenses = finalExpenses,
                                searchQuery = newState.searchQuery,
                                selectedCategories = newState.selectedCategories,
                                sortOption = newState.sortOption,
                                pendingDeletions = preservedPendingDeletions,
                            ),
                    )
            } else {
                val error = expensesResult.exceptionOrNull() ?: categoriesResult.exceptionOrNull()
                if (isRefresh && currentState is ExpenseListUiState.Success) {
                    // If refresh fails, keep existing data but show error somehow
                    // For now, just stop refreshing
                    _uiState.value = currentState.copy(isRefreshing = false)
                } else {
                    _uiState.value =
                        ExpenseListUiState.Error(
                            message = error?.message ?: "Failed to load data",
                        )
                }
            }
        }
    }

    private fun updateSearchQuery(query: String) {
        val currentState = _uiState.value
        if (currentState is ExpenseListUiState.Success) {
            _uiState.update {
                currentState.copy(
                    searchQuery = query,
                    filteredExpenses =
                        filterExpenses(
                            expenses = currentState.expenses,
                            searchQuery = query,
                            selectedCategories = currentState.selectedCategories,
                            sortOption = currentState.sortOption,
                            pendingDeletions = currentState.pendingDeletions,
                        ),
                )
            }
        }
    }

    private fun updateSearchExpanded(isExpanded: Boolean) {
        val currentState = _uiState.value
        if (currentState is ExpenseListUiState.Success) {
            _uiState.update {
                currentState.copy(
                    isSearchExpanded = isExpanded,
                    searchQuery = if (!isExpanded) "" else currentState.searchQuery,
                )
            }
        }
    }

    private fun toggleCategory(category: Category) {
        val currentState = _uiState.value
        if (currentState is ExpenseListUiState.Success) {
            val newSelectedCategories =
                if (currentState.selectedCategories.contains(category)) {
                    currentState.selectedCategories - category
                } else {
                    currentState.selectedCategories + category
                }

            _uiState.update {
                currentState.copy(
                    selectedCategories = newSelectedCategories,
                    filteredExpenses =
                        filterExpenses(
                            expenses = currentState.expenses,
                            searchQuery = currentState.searchQuery,
                            selectedCategories = newSelectedCategories,
                            sortOption = currentState.sortOption,
                            pendingDeletions = currentState.pendingDeletions,
                        ),
                )
            }
        }
    }

    private fun updateSortOption(sortOption: SortOption) {
        val currentState = _uiState.value
        if (currentState is ExpenseListUiState.Success) {
            _uiState.update {
                currentState.copy(
                    sortOption = sortOption,
                    filteredExpenses =
                        filterExpenses(
                            expenses = currentState.expenses,
                            searchQuery = currentState.searchQuery,
                            selectedCategories = currentState.selectedCategories,
                            sortOption = sortOption,
                            pendingDeletions = currentState.pendingDeletions,
                        ),
                )
            }
        }
    }

    private fun toggleMonth(monthYear: String) {
        val currentState = _uiState.value
        if (currentState is ExpenseListUiState.Success) {
            val newCollapsedMonths =
                if (currentState.collapsedMonths.contains(monthYear)) {
                    currentState.collapsedMonths - monthYear
                } else {
                    currentState.collapsedMonths + monthYear
                }

            _uiState.update {
                currentState.copy(collapsedMonths = newCollapsedMonths)
            }
        }
    }

    private fun updateFabExpanded(isExpanded: Boolean) {
        val currentState = _uiState.value
        if (currentState is ExpenseListUiState.Success) {
            _uiState.update {
                currentState.copy(isFabExpanded = isExpanded)
            }
        }
    }

    private fun handleExpenseClick(expense: Expense) {
        onExpenseClick(expense)
    }

    private fun handleAddExpenseClick() {
        onAddExpenseClick()
    }

    private fun filterExpenses(
        expenses: List<Expense>,
        searchQuery: String,
        selectedCategories: Set<Category>,
        sortOption: SortOption,
        pendingDeletions: Set<String> = emptySet(),
    ): List<Expense> {
        // First, exclude expenses pending deletion
        var filtered =
            expenses.filter { expense ->
                !pendingDeletions.contains(expense.documentId)
            }

        filtered =
            if (selectedCategories.isNotEmpty()) {
                filtered.filter { expense ->
                    selectedCategories.any { selectedCategory ->
                        // First try documentId comparison for more reliable matching
                        if (selectedCategory.documentId.isNotBlank() && expense.category.documentId.isNotBlank()) {
                            selectedCategory.documentId == expense.category.documentId
                        } else {
                            // Fallback to name-based comparison for backward compatibility
                            selectedCategory.name == expense.category.name
                        }
                    }
                }
            } else {
                emptyList()
            }

        // Apply search filter
        if (searchQuery.isNotBlank()) {
            filtered =
                filtered.filter { expense ->
                    expense.name.contains(searchQuery, ignoreCase = true) ||
                        expense.description.contains(searchQuery, ignoreCase = true) ||
                        expense.price.toString().contains(searchQuery)
                }
        }

        // Apply sorting
        filtered =
            when (sortOption) {
                SortOption.DATE_DESC -> filtered.sortedByDescending { it.date }
                SortOption.DATE_ASC -> filtered.sortedBy { it.date }
                SortOption.PRICE_DESC -> filtered.sortedByDescending { it.price }
                SortOption.PRICE_ASC -> filtered.sortedBy { it.price }
                SortOption.NAME_ASC -> filtered.sortedBy { it.name.lowercase() }
                SortOption.NAME_DESC -> filtered.sortedByDescending { it.name.lowercase() }
            }

        return filtered
    }

    private fun markForPendingDeletion(expenseId: String) {
        val currentState = _uiState.value
        if (currentState is ExpenseListUiState.Success) {
            _uiState.update {
                val newPendingDeletions = currentState.pendingDeletions + expenseId
                val newFilteredExpenses =
                    filterExpenses(
                        expenses = currentState.expenses,
                        searchQuery = currentState.searchQuery,
                        selectedCategories = currentState.selectedCategories,
                        sortOption = currentState.sortOption,
                        pendingDeletions = newPendingDeletions,
                    )
                currentState.copy(
                    pendingDeletions = newPendingDeletions,
                    filteredExpenses = newFilteredExpenses,
                )
            }
        }
    }

    private fun undoDeleteExpense(expenseId: String) {
        val currentState = _uiState.value
        if (currentState is ExpenseListUiState.Success) {
            _uiState.update {
                val newPendingDeletions = currentState.pendingDeletions - expenseId
                val newFilteredExpenses =
                    filterExpenses(
                        expenses = currentState.expenses,
                        searchQuery = currentState.searchQuery,
                        selectedCategories = currentState.selectedCategories,
                        sortOption = currentState.sortOption,
                        pendingDeletions = newPendingDeletions,
                    )
                currentState.copy(
                    pendingDeletions = newPendingDeletions,
                    filteredExpenses = newFilteredExpenses,
                )
            }
        }
    }

    private fun confirmDeleteExpense(expenseId: String) {
        viewModelScope.launch {
            deleteExpenseUseCase(listId, expenseId).fold(
                onSuccess = {
                    // Remove from pending deletions and expenses list
                    // Use the current state from the update block to avoid race conditions
                    _uiState.update { state ->
                        if (state is ExpenseListUiState.Success) {
                            val newExpenses = state.expenses.filter { it.documentId != expenseId }
                            val newPendingDeletions = state.pendingDeletions - expenseId
                            val debtSummaries = calculateDebtsFromExpenses(newExpenses)
                            state.copy(
                                expenses = newExpenses,
                                pendingDeletions = newPendingDeletions,
                                filteredExpenses =
                                    filterExpenses(
                                        expenses = newExpenses,
                                        searchQuery = state.searchQuery,
                                        selectedCategories = state.selectedCategories,
                                        sortOption = state.sortOption,
                                        pendingDeletions = newPendingDeletions,
                                    ),
                                debtSummaries = debtSummaries,
                            )
                        } else {
                            state
                        }
                    }
                },
                onFailure = { _ ->
                    // If actual deletion fails, remove from pending but keep the expense visible
                    _uiState.update { state ->
                        if (state is ExpenseListUiState.Success) {
                            val newPendingDeletions = state.pendingDeletions - expenseId
                            state.copy(
                                pendingDeletions = newPendingDeletions,
                                filteredExpenses =
                                    filterExpenses(
                                        expenses = state.expenses,
                                        searchQuery = state.searchQuery,
                                        selectedCategories = state.selectedCategories,
                                        sortOption = state.sortOption,
                                        pendingDeletions = newPendingDeletions,
                                    ),
                            )
                        } else {
                            state
                        }
                    }
                },
            )
        }
    }

    private fun toggleUpcomingExpenses() {
        val currentState = _uiState.value
        if (currentState is ExpenseListUiState.Success) {
            _uiState.update {
                currentState.copy(showUpcomingExpenses = !currentState.showUpcomingExpenses)
            }
        }
    }

    /**
     * Add expenses locally without network call.
     * Used for instant UI update after adding new expenses.
     */
    fun addExpensesLocally(newExpenses: List<Expense>) {
        val currentState = _uiState.value
        if (currentState is ExpenseListUiState.Success) {
            val updatedExpenses = currentState.expenses + newExpenses
            val debtSummaries = calculateDebtsFromExpenses(updatedExpenses)

            _uiState.update {
                currentState.copy(
                    expenses = updatedExpenses,
                    filteredExpenses =
                        filterExpenses(
                            expenses = updatedExpenses,
                            searchQuery = currentState.searchQuery,
                            selectedCategories = currentState.selectedCategories,
                            sortOption = currentState.sortOption,
                            pendingDeletions = currentState.pendingDeletions,
                        ),
                    debtSummaries = debtSummaries,
                )
            }
        }
    }

    /**
     * Update an existing expense in the local list after editing.
     * Used for instant UI update after editing an expense.
     */
    fun updateExpenseLocally(updatedExpense: Expense) {
        _uiState.update { state ->
            if (state is ExpenseListUiState.Success) {
                val updatedExpenses =
                    state.expenses.map { expense ->
                        if (expense.documentId == updatedExpense.documentId) {
                            updatedExpense
                        } else {
                            expense
                        }
                    }
                val debtSummaries = calculateDebtsFromExpenses(updatedExpenses)
                state.copy(
                    expenses = updatedExpenses,
                    filteredExpenses =
                        filterExpenses(
                            expenses = updatedExpenses,
                            searchQuery = state.searchQuery,
                            selectedCategories = state.selectedCategories,
                            sortOption = state.sortOption,
                            pendingDeletions = state.pendingDeletions,
                        ),
                    debtSummaries = debtSummaries,
                )
            } else {
                state
            }
        }
    }
}
