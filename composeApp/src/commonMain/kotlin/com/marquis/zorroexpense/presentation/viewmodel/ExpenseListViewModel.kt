package com.marquis.zorroexpense.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.marquis.zorroexpense.MockExpenseData
import com.marquis.zorroexpense.domain.model.Category
import com.marquis.zorroexpense.domain.model.Expense
import com.marquis.zorroexpense.domain.usecase.GetExpensesUseCase
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
    private val onExpenseClick: (Expense) -> Unit = {},
    private val onAddExpenseClick: () -> Unit = {}
) : ViewModel() {

    private val _uiState = MutableStateFlow<ExpenseListUiState>(ExpenseListUiState.Loading)
    val uiState: StateFlow<ExpenseListUiState> = _uiState.asStateFlow()

    init {
        loadExpenses()
    }

    fun onEvent(event: ExpenseListUiEvent) {
        when (event) {
            is ExpenseListUiEvent.LoadExpenses -> loadExpenses()
            is ExpenseListUiEvent.RefreshExpenses -> loadExpenses()
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

    private fun loadExpenses() {
        viewModelScope.launch {
            _uiState.value = ExpenseListUiState.Loading
            
            getExpensesUseCase()
                .onSuccess { expenses ->
                    val initialState = ExpenseListUiState.Success(
                        expenses = expenses,
                        filteredExpenses = expenses,
                        selectedCategories = MockExpenseData.allCategories.toSet(),
                        searchQuery = "",
                        isSearchExpanded = false,
                        sortOption = SortOption.DATE_DESC,
                        collapsedMonths = emptySet(),
                        isFabExpanded = true
                    )
                    
                    // Apply initial filtering (which should show all expenses since all categories are selected)
                    _uiState.value = initialState.copy(
                        filteredExpenses = filterExpenses(
                            expenses = expenses,
                            searchQuery = "",
                            selectedCategories = MockExpenseData.allCategories.toSet(),
                            sortOption = SortOption.DATE_DESC
                        )
                    )
                }
                .onFailure { exception ->
                    _uiState.value = ExpenseListUiState.Error(
                        message = exception.message ?: "Unknown error occurred"
                    )
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
                selectedCategories.contains(expense.category) || selectedCategories.any { it.name == expense.category.name }
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