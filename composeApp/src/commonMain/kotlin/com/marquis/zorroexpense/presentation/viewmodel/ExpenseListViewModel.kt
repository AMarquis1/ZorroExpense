package com.marquis.zorroexpense.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.marquis.zorroexpense.domain.model.Category
import com.marquis.zorroexpense.domain.model.Expense
import com.marquis.zorroexpense.domain.model.Group
import com.marquis.zorroexpense.domain.usecase.CalculateDebtsUseCase
import com.marquis.zorroexpense.domain.usecase.DeleteExpenseUseCase
import com.marquis.zorroexpense.domain.usecase.GetExpensesByListIdUseCase
import com.marquis.zorroexpense.domain.usecase.GetExpensePageUseCase
import com.marquis.zorroexpense.domain.usecase.GetGroupByIdUseCase
import com.marquis.zorroexpense.domain.usecase.RefreshExpensesUseCase
import com.marquis.zorroexpense.presentation.state.ExpenseListUiEvent
import com.marquis.zorroexpense.presentation.state.ExpenseListUiState
import com.marquis.zorroexpense.presentation.state.SortOption
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

class ExpenseListViewModel(
    private val listId: String,
    val listName: String = "",
    private val getExpensesByListIdUseCase: GetExpensesByListIdUseCase,
    private val getExpensePageUseCase: GetExpensePageUseCase,
    private val refreshExpensesUseCase: RefreshExpensesUseCase,
    private val deleteExpenseUseCase: DeleteExpenseUseCase,
    private val calculateDebtsUseCase: CalculateDebtsUseCase,
    private val getExpenseListByIdUseCase: GetGroupByIdUseCase,
    private var onExpenseClick: (Expense) -> Unit = {},
    private var onAddExpenseClick: () -> Unit = {},
) : ViewModel() {
    private val _uiState = MutableStateFlow<ExpenseListUiState>(ExpenseListUiState.Loading)
    val uiState: StateFlow<ExpenseListUiState> = _uiState.asStateFlow()

    private val _availableCategories = MutableStateFlow<List<Category>>(emptyList())
    val availableCategories: StateFlow<List<Category>> = _availableCategories.asStateFlow()

    private val _groupMetadata = MutableStateFlow<Group?>(null)
    val groupMetadata: StateFlow<Group?> = _groupMetadata.asStateFlow()

    private var hasLoadedOnce = false
    private var pageLoadJob: Job? = null
    private var pagingGeneration = 0
    private var debtExpenses: List<Expense> = emptyList()
    private var debtSummaries: List<com.marquis.zorroexpense.domain.model.DebtSummary> = emptyList()
    private var debtLoadGeneration = 0

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

    /**
     * Settlement balances require the entire list, unlike the paged expense feed.
     */
    private fun loadDebtSummaries() {
        val generation = ++debtLoadGeneration
        viewModelScope.launch {
            getExpensesByListIdUseCase(listId).onSuccess { expenses ->
                if (generation != debtLoadGeneration) return@onSuccess
                debtExpenses = expenses
                debtSummaries = calculateDebtsFromExpenses(debtExpenses)
                _uiState.update { state ->
                    if (state is ExpenseListUiState.Success) {
                        state.copy(debtSummaries = debtSummaries)
                    } else {
                        state
                    }
                }
            }
        }
    }

    init {
        // Check if we already have data loaded, if not load from cache or fetch
        val currentState = _uiState.value
        if (currentState !is ExpenseListUiState.Success || !currentState.hasInitiallyLoaded) {
            loadExpenses(isRefresh = false)
        }
        hasLoadedOnce = true
        // Load expense list metadata
        loadExpenseListMetadata()
    }

    private fun loadExpenseListMetadata() {
        viewModelScope.launch {
            getExpenseListByIdUseCase(listId).onSuccess { expenseList ->
                _groupMetadata.value = expenseList
                // Update available categories from group metadata
                if (expenseList?.categories?.isNotEmpty() == true) {
                    _availableCategories.value = expenseList.categories
                }
            }
        }
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
            is ExpenseListUiEvent.LoadNextPage,
            is ExpenseListUiEvent.RetryLoadNextPage,
            -> loadNextPage()
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

    private fun loadExpenses(
        isRefresh: Boolean = false,
    ) {
        pageLoadJob?.cancel()
        val generation = ++pagingGeneration
        loadDebtSummaries()
        pageLoadJob = viewModelScope.launch {
            val currentState = _uiState.value
            if (currentState is ExpenseListUiState.Success) {
                _uiState.value = currentState.copy(isRefreshing = true)
            } else if (!isRefresh && hasLoadedOnce) {
                // On navigation back to list: keep the previous success state visible while fetching fresh data
                // This provides instant UI feedback from cache without showing loading state
                // The repository's cache-first strategy ensures data loads from cache immediately
                // We'll update the data once fresh data arrives
            } else if (!isRefresh) {
                _uiState.value = ExpenseListUiState.Loading
            }

            val expensesResult = getExpensePageUseCase(listId, cursor = null)

            if (expensesResult.isSuccess) {
                if (generation != pagingGeneration) return@launch
                val page = expensesResult.getOrThrow()
                val expenses = page.expenses
                val categories =
                    expenses
                        .map { it.category }
                        .distinctBy { category ->
                            category.documentId.ifBlank { category.name }
                        }

                _availableCategories.value = categories

                val existingState =
                    currentState as? ExpenseListUiState.Success ?: ExpenseListUiState.Success()

                val selectedCats =
                    if (isRefresh && currentState is ExpenseListUiState.Success) {
                        currentState.selectedCategories
                    } else {
                        categories.toSet()
                    }
                val preservedPendingDeletions =
                    if (currentState is ExpenseListUiState.Success) {
                        currentState.pendingDeletions
                    } else {
                        emptySet()
                    }

                val finalExpenses =
                    if (isRefresh && currentState is ExpenseListUiState.Success && preservedPendingDeletions.isNotEmpty()) {
                        val pendingExpenses =
                            currentState.expenses.filter { expense ->
                                preservedPendingDeletions.contains(expense.documentId)
                            }
                        (expenses + pendingExpenses).distinctBy { it.documentId }
                    } else {
                        expenses
                    }

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
                        nextCursor = page.nextCursor,
                        hasMore = page.hasMore,
                    )

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
                if (generation != pagingGeneration) return@launch
                val error = expensesResult.exceptionOrNull()
                if (isRefresh && currentState is ExpenseListUiState.Success) {
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

    private fun loadNextPage() {
        val currentState = _uiState.value as? ExpenseListUiState.Success ?: return
        if (pageLoadJob?.isActive == true || currentState.isLoadingNextPage || !currentState.hasMore || currentState.nextCursor == null) return

        val generation = pagingGeneration
        pageLoadJob = viewModelScope.launch {
            _uiState.value = currentState.copy(isLoadingNextPage = true, nextPageError = null)
            getExpensePageUseCase(listId, currentState.nextCursor).fold(
                onSuccess = { page ->
                    if (generation != pagingGeneration) return@fold
                    val latestState = _uiState.value as? ExpenseListUiState.Success ?: return@fold
                    val expenses = (latestState.expenses + page.expenses).distinctBy { it.documentId }
                    _uiState.value = latestState.copy(
                        expenses = expenses,
                        filteredExpenses = filterExpenses(expenses, latestState.searchQuery, latestState.selectedCategories, latestState.sortOption, latestState.pendingDeletions),
                        isLoadingNextPage = false,
                        nextCursor = page.nextCursor,
                        hasMore = page.hasMore,
                    )
                },
                onFailure = { error ->
                    if (generation != pagingGeneration) return@fold
                    val latestState = _uiState.value as? ExpenseListUiState.Success ?: return@fold
                    _uiState.value = latestState.copy(isLoadingNextPage = false, nextPageError = error.message ?: "Failed to load more expenses")
                },
            )
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
                            debtExpenses = debtExpenses.filter { it.documentId != expenseId }
                            debtLoadGeneration++
                            debtSummaries = calculateDebtsFromExpenses(debtExpenses)
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
            debtExpenses = (debtExpenses + newExpenses).distinctBy { it.documentId }
            debtLoadGeneration++
            debtSummaries = calculateDebtsFromExpenses(debtExpenses)

            // Extract new categories from the added expenses and add them to available categories if not already present
            val newCategories =
                newExpenses
                    .map { it.category }
                    .distinctBy { category ->
                        category.documentId.ifBlank { category.name }
                    }
            val currentAvailableCategories = _availableCategories.value
            val updatedAvailableCategories =
                (currentAvailableCategories + newCategories)
                    .distinctBy { category ->
                        category.documentId.ifBlank { category.name }
                    }

            // Add new categories to selected categories by default
            val categoriesNotYetSelected =
                newCategories.filter { newCategory ->
                    !currentState.selectedCategories.any { selected ->
                        if (newCategory.documentId.isNotBlank() && selected.documentId.isNotBlank()) {
                            newCategory.documentId == selected.documentId
                        } else {
                            newCategory.name == selected.name
                        }
                    }
                }
            val updatedSelectedCategories = currentState.selectedCategories + categoriesNotYetSelected

            _availableCategories.value = updatedAvailableCategories

            _uiState.update {
                currentState.copy(
                    expenses = updatedExpenses,
                    selectedCategories = updatedSelectedCategories,
                    filteredExpenses =
                        filterExpenses(
                            expenses = updatedExpenses,
                            searchQuery = currentState.searchQuery,
                            selectedCategories = updatedSelectedCategories,
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
                debtExpenses =
                    debtExpenses.map { expense ->
                        if (expense.documentId == updatedExpense.documentId) updatedExpense else expense
                    }
                debtLoadGeneration++
                debtSummaries = calculateDebtsFromExpenses(debtExpenses)

                // Check if the updated expense uses a new category and add it if needed
                val updatedCategory = updatedExpense.category
                val currentAvailableCategories = _availableCategories.value
                val categoryExists =
                    currentAvailableCategories.any { category ->
                        if (updatedCategory.documentId.isNotBlank() && category.documentId.isNotBlank()) {
                            updatedCategory.documentId == category.documentId
                        } else {
                            updatedCategory.name == category.name
                        }
                    }

                if (!categoryExists) {
                    val updatedAvailableCategories = currentAvailableCategories + updatedCategory
                    _availableCategories.value = updatedAvailableCategories

                    val updatedSelectedCategories = state.selectedCategories + updatedCategory
                    return@update state.copy(
                        expenses = updatedExpenses,
                        selectedCategories = updatedSelectedCategories,
                        filteredExpenses =
                            filterExpenses(
                                expenses = updatedExpenses,
                                searchQuery = state.searchQuery,
                                selectedCategories = updatedSelectedCategories,
                                sortOption = state.sortOption,
                                pendingDeletions = state.pendingDeletions,
                            ),
                        debtSummaries = debtSummaries,
                    )
                }

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
