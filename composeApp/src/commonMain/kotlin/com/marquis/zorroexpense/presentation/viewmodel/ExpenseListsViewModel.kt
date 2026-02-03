package com.marquis.zorroexpense.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.marquis.zorroexpense.domain.model.ExpenseList
import com.marquis.zorroexpense.domain.model.User
import com.marquis.zorroexpense.domain.usecase.GetUserExpenseListsUseCase
import com.marquis.zorroexpense.domain.usecase.GetUsersUseCase
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
    private val getUsersUseCase: GetUsersUseCase,
    private val onListSelected: (listId: String, listName: String) -> Unit = { _, _ -> },
) : ViewModel() {
    private val _uiState = MutableStateFlow<ExpenseListsUiState>(ExpenseListsUiState.Loading)
    val uiState: StateFlow<ExpenseListsUiState> = _uiState.asStateFlow()

    private var cachedLists: List<ExpenseList>? = null

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
        // Load with cache - shows cached data while refreshing
        loadListsWithCache(showCacheImmediately = true)
    }

    private fun loadLists() {
        // Initial load without cache optimization
        loadListsWithCache(showCacheImmediately = false)
    }

    private fun loadListsWithCache(showCacheImmediately: Boolean) {
        viewModelScope.launch {
            // Check if we should show cache immediately
            val currentState = _uiState.value
            val shouldShowCacheImmediately = showCacheImmediately || (currentState is ExpenseListsUiState.Success)

            // If we have cached data and should show it immediately, do so
            if (shouldShowCacheImmediately && (cachedLists != null || currentState is ExpenseListsUiState.Success)) {
                val listsToShow = cachedLists ?: (currentState as? ExpenseListsUiState.Success)?.lists ?: emptyList()
                _uiState.value = ExpenseListsUiState.Success(listsToShow, isRefreshing = true)
            } else {
                _uiState.value = ExpenseListsUiState.Loading
            }

            val result = getUserExpenseListsUseCase.invoke(userId)

            result.onSuccess { lists ->
                val allUserIds = lists.flatMap { list -> list.members.map { "Users/${it.userId}" } }.distinct()

                if (allUserIds.isNotEmpty()) {
                    getUsersUseCase.invoke(allUserIds).onSuccess { users ->
                        val userMap = users.associateBy { it.userId }

                        val enrichedLists = lists.map { list ->
                            val enrichedMembers = list.members.map { member ->
                                userMap[member.userId]?.let {
                                    member.copy(name = it.name, profileImage = it.profileImage)
                                } ?: member
                            }
                            list.copy(members = enrichedMembers)
                        }

                        cachedLists = enrichedLists

                        _uiState.value =
                            if (enrichedLists.isEmpty()) {
                                ExpenseListsUiState.Empty
                            } else {
                                ExpenseListsUiState.Success(enrichedLists)
                            }
                    }.onFailure {
                        cachedLists = lists

                        _uiState.value =
                            if (lists.isEmpty()) {
                                ExpenseListsUiState.Empty
                            } else {
                                ExpenseListsUiState.Success(lists)
                            }
                    }
                } else {
                    cachedLists = lists

                    _uiState.value =
                        if (lists.isEmpty()) {
                            ExpenseListsUiState.Empty
                        } else {
                            ExpenseListsUiState.Success(lists)
                        }
                }
            }

            result.onFailure { error ->
                // Show error but keep cached data visible if available
                _uiState.value =
                    ExpenseListsUiState.Error(
                        error.message ?: "Failed to load expense lists",
                        cachedLists,
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
                    _uiState.value =
                        ExpenseListsUiState.Error(
                            error.message ?: "Failed to join list",
                        )
                    // Reload lists after a moment
                    loadLists()
                } else {
                    _uiState.value =
                        ExpenseListsUiState.Error(
                            error.message ?: "Failed to join list",
                        )
                }
            }
        }
    }
}
