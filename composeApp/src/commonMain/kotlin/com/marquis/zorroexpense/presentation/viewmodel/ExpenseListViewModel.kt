package com.marquis.zorroexpense.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.marquis.zorroexpense.domain.model.Category
import com.marquis.zorroexpense.domain.model.Expense
import com.marquis.zorroexpense.domain.usecase.DeleteExpenseUseCase
import com.marquis.zorroexpense.domain.usecase.GetCategoriesUseCase
import com.marquis.zorroexpense.domain.usecase.GetExpensesUseCase
import com.marquis.zorroexpense.domain.usecase.RefreshExpensesUseCase
import com.marquis.zorroexpense.presentation.state.ExpenseListUiEvent
import com.marquis.zorroexpense.presentation.state.ExpenseListUiState
import com.marquis.zorroexpense.presentation.state.SortOption
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ExpenseListViewModel(
    private val getExpensesUseCase: GetExpensesUseCase,
    private val getCategoriesUseCase: GetCategoriesUseCase,
    private val refreshExpensesUseCase: RefreshExpensesUseCase,
    private val deleteExpenseUseCase: DeleteExpenseUseCase,
    private var onExpenseClick: (Expense) -> Unit = {},
    private var onAddExpenseClick: () -> Unit = {}
) : ViewModel() {

    private val _uiState = MutableStateFlow<ExpenseListUiState>(ExpenseListUiState.Loading)
    val uiState: StateFlow<ExpenseListUiState> = _uiState.asStateFlow()
    
    private val _availableCategories = MutableStateFlow<List<Category>>(emptyList())
    val availableCategories: StateFlow<List<Category>> = _availableCategories.asStateFlow()
    
    private var hasLoadedOnce = false

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
        onAddExpenseClick: () -> Unit
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
        }
    }

    private fun loadExpenses(isRefresh: Boolean = false) {
        viewModelScope.launch {
            val currentState = _uiState.value
            
            // For initial load, show loading state
            // For refresh, keep existing data visible and set isRefreshing = true
            if (!isRefresh) {
                _uiState.value = ExpenseListUiState.Loading
            } else if (currentState is ExpenseListUiState.Success) {
                _uiState.value = currentState.copy(isRefreshing = true)
            }
            
            // Load both expenses and categories
            // Use refreshExpensesUseCase for force refresh, otherwise use cached version
            val expensesResult = if (isRefresh) {
                refreshExpensesUseCase()
            } else {
                getExpensesUseCase()
            }
            val categoriesResult = getCategoriesUseCase()
            
            if (expensesResult.isSuccess && categoriesResult.isSuccess) {
                val expenses = expensesResult.getOrThrow()
                val categories = categoriesResult.getOrThrow()
                
                // Update available categories for the UI
                _availableCategories.value = categories
                
                // Preserve existing UI state if refreshing, otherwise use defaults
                val existingState = if (isRefresh && currentState is ExpenseListUiState.Success) {
                    currentState
                } else {
                    ExpenseListUiState.Success()
                }
                
                val selectedCats = if (isRefresh && currentState is ExpenseListUiState.Success) {
                    currentState.selectedCategories
                } else {
                    categories.toSet() // Use all categories for initial load
                }
                
                // Preserve pending deletions when refreshing
                val preservedPendingDeletions = if (isRefresh && currentState is ExpenseListUiState.Success) {
                    currentState.pendingDeletions
                } else {
                    emptySet()
                }
                
                // When refreshing with pending deletions, we need to merge the new expenses
                // with any expenses that are pending deletion (so they can be restored)
                val finalExpenses = if (isRefresh && currentState is ExpenseListUiState.Success && preservedPendingDeletions.isNotEmpty()) {
                    // Get expenses that are pending deletion from current state
                    val pendingExpenses = currentState.expenses.filter { expense ->
                        preservedPendingDeletions.contains(expense.documentId)
                    }
                    // Merge new expenses with pending expenses, avoiding duplicates
                    (expenses + pendingExpenses).distinctBy { it.documentId }
                } else {
                    expenses
                }
                
                val newState = ExpenseListUiState.Success(
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
                    pendingDeletions = preservedPendingDeletions
                )
                
                // Apply filtering with current filters
                _uiState.value = newState.copy(
                    filteredExpenses = filterExpenses(
                        expenses = finalExpenses,
                        searchQuery = newState.searchQuery,
                        selectedCategories = newState.selectedCategories,
                        sortOption = newState.sortOption,
                        pendingDeletions = preservedPendingDeletions
                    )
                )
            } else {
                val error = expensesResult.exceptionOrNull() ?: categoriesResult.exceptionOrNull()
                if (isRefresh && currentState is ExpenseListUiState.Success) {
                    // If refresh fails, keep existing data but show error somehow
                    // For now, just stop refreshing
                    _uiState.value = currentState.copy(isRefreshing = false)
                } else {
                    _uiState.value = ExpenseListUiState.Error(
                        message = error?.message ?: "Failed to load data"
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
                    filteredExpenses = filterExpenses(
                        expenses = currentState.expenses,
                        searchQuery = query,
                        selectedCategories = currentState.selectedCategories,
                        sortOption = currentState.sortOption,
                        pendingDeletions = currentState.pendingDeletions
                    )
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
                    searchQuery = if (!isExpanded) "" else currentState.searchQuery
                )
            }
        }
    }

    private fun toggleCategory(category: Category) {
        val currentState = _uiState.value
        if (currentState is ExpenseListUiState.Success) {
            val newSelectedCategories = if (currentState.selectedCategories.contains(category)) {
                currentState.selectedCategories - category
            } else {
                currentState.selectedCategories + category
            }

            _uiState.update { 
                currentState.copy(
                    selectedCategories = newSelectedCategories,
                    filteredExpenses = filterExpenses(
                        expenses = currentState.expenses,
                        searchQuery = currentState.searchQuery,
                        selectedCategories = newSelectedCategories,
                        sortOption = currentState.sortOption,
                        pendingDeletions = currentState.pendingDeletions
                    )
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
                    filteredExpenses = filterExpenses(
                        expenses = currentState.expenses,
                        searchQuery = currentState.searchQuery,
                        selectedCategories = currentState.selectedCategories,
                        sortOption = sortOption,
                        pendingDeletions = currentState.pendingDeletions
                    )
                )
            }
        }
    }

    private fun toggleMonth(monthYear: String) {
        val currentState = _uiState.value
        if (currentState is ExpenseListUiState.Success) {
            val newCollapsedMonths = if (currentState.collapsedMonths.contains(monthYear)) {
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
        pendingDeletions: Set<String> = emptySet()
    ): List<Expense> {
        // First, exclude expenses pending deletion
        var filtered = expenses.filter { expense ->
            !pendingDeletions.contains(expense.documentId)
        }

        filtered = if (selectedCategories.isNotEmpty()) {
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
            filtered = filtered.filter { expense ->
                expense.name.contains(searchQuery, ignoreCase = true) ||
                expense.description.contains(searchQuery, ignoreCase = true) ||
                expense.price.toString().contains(searchQuery)
            }
        }

        // Apply sorting
        filtered = when (sortOption) {
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
                val newFilteredExpenses = filterExpenses(
                    expenses = currentState.expenses,
                    searchQuery = currentState.searchQuery,
                    selectedCategories = currentState.selectedCategories,
                    sortOption = currentState.sortOption,
                    pendingDeletions = newPendingDeletions
                )
                currentState.copy(
                    pendingDeletions = newPendingDeletions,
                    filteredExpenses = newFilteredExpenses
                )
            }
        }
    }
    
    private fun undoDeleteExpense(expenseId: String) {
        val currentState = _uiState.value
        if (currentState is ExpenseListUiState.Success) {
            _uiState.update {
                val newPendingDeletions = currentState.pendingDeletions - expenseId
                val newFilteredExpenses = filterExpenses(
                    expenses = currentState.expenses,
                    searchQuery = currentState.searchQuery,
                    selectedCategories = currentState.selectedCategories,
                    sortOption = currentState.sortOption,
                    pendingDeletions = newPendingDeletions
                )
                currentState.copy(
                    pendingDeletions = newPendingDeletions,
                    filteredExpenses = newFilteredExpenses
                )
            }
        }
    }
    
    private fun confirmDeleteExpense(expenseId: String) {
        val currentState = _uiState.value
        if (currentState is ExpenseListUiState.Success) {
            viewModelScope.launch {
                deleteExpenseUseCase(expenseId).fold(
                    onSuccess = {
                        // Remove from pending deletions and expenses list
                        _uiState.update {
                            val newExpenses = currentState.expenses.filter { it.documentId != expenseId }
                            val newPendingDeletions = currentState.pendingDeletions - expenseId
                            currentState.copy(
                                expenses = newExpenses,
                                pendingDeletions = newPendingDeletions,
                                filteredExpenses = filterExpenses(
                                    expenses = newExpenses,
                                    searchQuery = currentState.searchQuery,
                                    selectedCategories = currentState.selectedCategories,
                                    sortOption = currentState.sortOption,
                                    pendingDeletions = newPendingDeletions
                                )
                            )
                        }
                    },
                    onFailure = { exception ->
                        // If actual deletion fails, remove from pending but keep the expense
                        // Could show error message here
                        _uiState.update {
                            currentState.copy(
                                pendingDeletions = currentState.pendingDeletions - expenseId,
                                filteredExpenses = filterExpenses(
                                    expenses = currentState.expenses,
                                    searchQuery = currentState.searchQuery,
                                    selectedCategories = currentState.selectedCategories,
                                    sortOption = currentState.sortOption,
                                    pendingDeletions = currentState.pendingDeletions - expenseId
                                )
                            )
                        }
                    }
                )
            }
        }
    }
}