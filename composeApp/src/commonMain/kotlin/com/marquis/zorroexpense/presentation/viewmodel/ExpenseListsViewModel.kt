package com.marquis.zorroexpense.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.marquis.zorroexpense.domain.model.ExpenseList
import com.marquis.zorroexpense.domain.usecase.GetUserExpenseListsUseCase
import com.marquis.zorroexpense.domain.usecase.JoinExpenseListUseCase
import com.marquis.zorroexpense.presentation.state.ExpenseListsUiEvent
import com.marquis.zorroexpense.presentation.state.ExpenseListsUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel for managing expense lists selection
 */
class ExpenseListsViewModel(
    private val userId: String,
    private val getUserExpenseListsUseCase: GetUserExpenseListsUseCase,
    private val joinExpenseListUseCase: JoinExpenseListUseCase,
    private val onListSelected: (listId: String, listName: String) -> Unit = { _, _ -> },
) : ViewModel() {

    private val _uiState = MutableStateFlow<ExpenseListsUiState>(ExpenseListsUiState.Loading)
    val uiState: StateFlow<ExpenseListsUiState> = _uiState.asStateFlow()

    init {
        loadLists()
    }

    fun onEvent(event: ExpenseListsUiEvent) {
        when (event) {
            ExpenseListsUiEvent.LoadLists -> loadLists()
            ExpenseListsUiEvent.CreateNewList -> {
                // This will be handled by navigation callback
            }
            is ExpenseListsUiEvent.SelectList -> selectList(event.listId)
            is ExpenseListsUiEvent.JoinList -> joinList(event.shareCode)
            ExpenseListsUiEvent.RetryLoad -> loadLists()
        }
    }

    fun refreshLists() {
        loadLists()
    }

    private fun loadLists() {
        viewModelScope.launch {
            _uiState.value = ExpenseListsUiState.Loading
            val result = getUserExpenseListsUseCase.invoke(userId)

            result.onSuccess { lists ->
                _uiState.value = if (lists.isEmpty()) {
                    ExpenseListsUiState.Empty
                } else {
                    ExpenseListsUiState.Success(lists)
                }
            }

            result.onFailure { error ->
                _uiState.value = ExpenseListsUiState.Error(
                    error.message ?: "Failed to load expense lists"
                )
            }
        }
    }

    private fun selectList(listId: String) {
        val currentState = _uiState.value
        if (currentState is ExpenseListsUiState.Success) {
            val selectedList = currentState.lists.find { it.listId == listId }
            selectedList?.let {
                onListSelected(it.listId, it.name)
            }
        }
    }

    private fun joinList(shareCode: String) {
        viewModelScope.launch {
            _uiState.value = ExpenseListsUiState.Loading
            val result = joinExpenseListUseCase.invoke(userId, shareCode)

            result.onSuccess { list ->
                onListSelected(list.listId, list.name)
            }

            result.onFailure { error ->
                // Show error but keep current lists visible
                val currentState = _uiState.value
                if (currentState is ExpenseListsUiState.Success) {
                    _uiState.value = ExpenseListsUiState.Error(
                        error.message ?: "Failed to join list"
                    )
                    // Reload lists after a moment
                    loadLists()
                } else {
                    _uiState.value = ExpenseListsUiState.Error(
                        error.message ?: "Failed to join list"
                    )
                }
            }
        }
    }
}
