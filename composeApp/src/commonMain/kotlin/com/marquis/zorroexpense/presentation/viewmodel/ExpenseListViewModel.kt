package com.marquis.zorroexpense.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.marquis.zorroexpense.MockExpenseData
import com.marquis.zorroexpense.domain.model.Category
import com.marquis.zorroexpense.domain.model.Expense
import com.marquis.zorroexpense.domain.usecase.GetExpensesUseCase
import com.marquis.zorroexpense.domain.usecase.GetCategoriesUseCase
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
    private var onExpenseClick: (Expense) -> Unit = {},
    private var onAddExpenseClick: () -> Unit = {}
) : ViewModel() {

    private val _uiState = MutableStateFlow<ExpenseListUiState>(ExpenseListUiState.Loading)
    val uiState: StateFlow<ExpenseListUiState> = _uiState.asStateFlow()

    init {
        // Load expenses on initialization - Firestore cache makes this instant after first load
        loadExpenses(isRefresh = false)
    }

    fun updateCallbacks(
        onExpenseClick: (Expense) -> Unit,
        onAddExpenseClick: () -> Unit
    ) {
        this.onExpenseClick = onExpenseClick
        this.onAddExpenseClick = onAddExpenseClick
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
            val expensesResult = getExpensesUseCase()
            val categoriesResult = getCategoriesUseCase()
            
            if (expensesResult.isSuccess && categoriesResult.isSuccess) {
                val expenses = expensesResult.getOrThrow()
                val categories = categoriesResult.getOrThrow()
                
                // Preserve existing UI state if refreshing, otherwise use defaults
                val existingState = if (isRefresh && currentState is ExpenseListUiState.Success) {
                    currentState
                } else {
                    ExpenseListUiState.Success()
                }
                
                val newState = ExpenseListUiState.Success(
                    expenses = expenses,
                    filteredExpenses = expenses,
                    selectedCategories = if (isRefresh && currentState is ExpenseListUiState.Success) {
                        currentState.selectedCategories
                    } else {
                        categories.toSet() // Use all categories for initial load
                    },
                    searchQuery = existingState.searchQuery,
                    isSearchExpanded = existingState.isSearchExpanded,
                    sortOption = existingState.sortOption,
                    collapsedMonths = existingState.collapsedMonths,
                    isFabExpanded = existingState.isFabExpanded,
                    isRefreshing = false,
                    hasInitiallyLoaded = true
                )
                
                // Apply filtering with current filters
                _uiState.value = newState.copy(
                    filteredExpenses = filterExpenses(
                        expenses = expenses,
                        searchQuery = newState.searchQuery,
                        selectedCategories = newState.selectedCategories,
                        sortOption = newState.sortOption
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
                        sortOption = currentState.sortOption
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
                        sortOption = currentState.sortOption
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
                        sortOption = sortOption
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
        sortOption: SortOption
    ): List<Expense> {
        var filtered = expenses

        filtered = if (selectedCategories.isNotEmpty()) {
            filtered.filter { expense ->
                // Use name-based comparison since categories from Firestore might be different object instances
                selectedCategories.any { selectedCategory -> 
                    selectedCategory.name == expense.category.name 
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
}