package com.marquis.zorroexpense.presentation.state

import com.marquis.zorroexpense.domain.model.Category
import com.marquis.zorroexpense.domain.model.Expense

sealed class ExpenseListUiState {
    object Loading : ExpenseListUiState()
    
    data class Success(
        val expenses: List<Expense> = emptyList(),
        val filteredExpenses: List<Expense> = emptyList(),
        val searchQuery: String = "",
        val isSearchExpanded: Boolean = false,
        val selectedCategories: Set<Category> = emptySet(),
        val sortOption: SortOption = SortOption.DATE_DESC,
        val collapsedMonths: Set<String> = emptySet(),
        val isFabExpanded: Boolean = true,
        val isRefreshing: Boolean = false,
        val hasInitiallyLoaded: Boolean = false
    ) : ExpenseListUiState()
    
    data class Error(
        val message: String
    ) : ExpenseListUiState()
}

enum class SortOption(val displayName: String) {
    DATE_DESC("Date (Newest first)"),
    DATE_ASC("Date (Oldest first)"),
    PRICE_DESC("Price (Highest first)"),
    PRICE_ASC("Price (Lowest first)"),
    NAME_ASC("Name (A-Z)"),
    NAME_DESC("Name (Z-A)")
}