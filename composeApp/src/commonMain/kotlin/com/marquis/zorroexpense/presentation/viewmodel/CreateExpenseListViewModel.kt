package com.marquis.zorroexpense.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.marquis.zorroexpense.domain.model.Category
import com.marquis.zorroexpense.domain.usecase.CreateExpenseListUseCase
import com.marquis.zorroexpense.domain.usecase.GetCategoriesUseCase
import com.marquis.zorroexpense.presentation.state.CreateExpenseListUiEvent
import com.marquis.zorroexpense.presentation.state.CreateExpenseListUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel for creating a new expense list
 */
class CreateExpenseListViewModel(
    private val userId: String,
    private val createExpenseListUseCase: CreateExpenseListUseCase,
    private val getCategoriesUseCase: GetCategoriesUseCase,
    private val onListCreated: (listId: String, listName: String) -> Unit = { _, _ -> },
) : ViewModel() {

    private val _uiState = MutableStateFlow<CreateExpenseListUiState>(CreateExpenseListUiState.Idle)
    val uiState: StateFlow<CreateExpenseListUiState> = _uiState.asStateFlow()

    private val _listName = MutableStateFlow("")
    val listName: StateFlow<String> = _listName.asStateFlow()

    private val _availableCategories = MutableStateFlow<List<Category>>(emptyList())
    val availableCategories: StateFlow<List<Category>> = _availableCategories.asStateFlow()

    private val _selectedCategories = MutableStateFlow<Set<String>>(emptySet())
    val selectedCategories: StateFlow<Set<String>> = _selectedCategories.asStateFlow()

    init {
        loadCategories()
    }

    fun onEvent(event: CreateExpenseListUiEvent) {
        when (event) {
            is CreateExpenseListUiEvent.ListNameChanged -> updateListName(event.name)
            is CreateExpenseListUiEvent.CategoriesSelected -> updateSelectedCategories(event.categoryIds)
            CreateExpenseListUiEvent.CreateList -> createList()
            CreateExpenseListUiEvent.ClearError -> clearError()
        }
    }

    private fun loadCategories() {
        viewModelScope.launch {
            val result = getCategoriesUseCase()
            result.onSuccess { categories ->
                _availableCategories.value = categories
                // Select all categories by default
                _selectedCategories.value = categories.map { it.documentId }.toSet()
            }
            result.onFailure { _ ->
                // Continue without categories, user can select later
            }
        }
    }

    private fun updateListName(name: String) {
        _listName.value = name
    }

    private fun updateSelectedCategories(categoryIds: List<String>) {
        _selectedCategories.value = categoryIds.toSet()
    }

    private fun createList() {
        val name = _listName.value.trim()

        if (name.isEmpty()) {
            _uiState.value = CreateExpenseListUiState.Error("List name cannot be empty")
            return
        }

        viewModelScope.launch {
            _uiState.value = CreateExpenseListUiState.Loading
            val result = createExpenseListUseCase(
                userId = userId,
                name = name,
                categoryIds = _selectedCategories.value.toList()
            )

            result.onSuccess { listId ->
                _uiState.value = CreateExpenseListUiState.Success(listId, name)
                onListCreated(listId, name)
            }

            result.onFailure { error ->
                _uiState.value = CreateExpenseListUiState.Error(
                    error.message ?: "Failed to create expense list"
                )
            }
        }
    }

    private fun clearError() {
        _uiState.value = CreateExpenseListUiState.Idle
    }
}
