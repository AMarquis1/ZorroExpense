package com.marquis.zorroexpense.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.marquis.zorroexpense.data.remote.dto.toDateString
import com.marquis.zorroexpense.domain.model.ExpenseList
import com.marquis.zorroexpense.domain.usecase.DeleteExpenseListUseCase
import com.marquis.zorroexpense.domain.usecase.GetUserExpenseListsUseCase
import com.marquis.zorroexpense.domain.usecase.GetUsersUseCase
import com.marquis.zorroexpense.domain.usecase.JoinExpenseListUseCase
import com.marquis.zorroexpense.domain.usecase.RefreshUserExpenseListsUseCase
import com.marquis.zorroexpense.presentation.state.ExpenseListsUiEvent
import com.marquis.zorroexpense.presentation.state.ExpenseListsUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel for managing expense lists selection
 */
class ExpenseListsOverviewViewModel(
    private val userId: String,
    private val getUserExpenseListsUseCase: GetUserExpenseListsUseCase,
    private val refreshUserExpenseListsUseCase: RefreshUserExpenseListsUseCase,
    private val joinExpenseListUseCase: JoinExpenseListUseCase,
    private val deleteExpenseListUseCase: DeleteExpenseListUseCase,
    private val getUsersUseCase: GetUsersUseCase,
    private val onListSelected: (listId: String, listName: String) -> Unit = { _, _ -> },
    private val onListDeleted: (listId: String) -> Unit = { _ -> },
) : ViewModel() {
    private val _uiState = MutableStateFlow<ExpenseListsUiState>(ExpenseListsUiState.Loading)
    val uiState: StateFlow<ExpenseListsUiState> = _uiState.asStateFlow()

    private var cachedLists: List<ExpenseList>? = null

    init {
        loadLists()
    }

    fun onEvent(event: ExpenseListsUiEvent) {
        when (event) {
            is ExpenseListsUiEvent.LoadLists -> loadLists()
            is ExpenseListsUiEvent.RefreshLists -> refreshLists()
            is ExpenseListsUiEvent.CreateNewList -> {}
            is ExpenseListsUiEvent.SelectList -> selectList(event.listId)
            is ExpenseListsUiEvent.JoinList -> joinList(event.shareCode)
            is ExpenseListsUiEvent.DeleteList -> showDeleteConfirmation(event.list)
            is ExpenseListsUiEvent.ConfirmDelete -> confirmDelete()
            is ExpenseListsUiEvent.CancelDelete -> cancelDelete()
            is ExpenseListsUiEvent.EditList -> editList(event.list)
            is ExpenseListsUiEvent.RetryLoad -> loadLists()
        }
    }

    fun refreshLists() {
        loadListsWithCache(showCacheImmediately = true, forceRefresh = true)
    }

    private fun loadLists() {
        loadListsWithCache(showCacheImmediately = false, forceRefresh = false)
    }

    private fun loadListsWithCache(showCacheImmediately: Boolean, forceRefresh: Boolean = false) {
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

            // Use refresh use case for force refresh (pull-to-refresh), otherwise use normal get
            val result = if (forceRefresh) {
                refreshUserExpenseListsUseCase.invoke(userId)
            } else {
                getUserExpenseListsUseCase.invoke(userId)
            }

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
                        }.sortedByDescending { it.lastModified }

                        cachedLists = enrichedLists

                        _uiState.value =
                            if (enrichedLists.isEmpty()) {
                                ExpenseListsUiState.Empty
                            } else {
                                ExpenseListsUiState.Success(enrichedLists)
                            }
                    }.onFailure {
                        val sortedLists = lists.sortedByDescending { it.lastModified }
                        cachedLists = sortedLists

                        _uiState.value =
                            if (sortedLists.isEmpty()) {
                                ExpenseListsUiState.Empty
                            } else {
                                ExpenseListsUiState.Success(sortedLists)
                            }
                    }
                } else {
                    val sortedLists = lists.sortedByDescending { it.lastModified }
                    cachedLists = sortedLists

                    _uiState.value =
                        if (sortedLists.isEmpty()) {
                            ExpenseListsUiState.Empty
                        } else {
                            ExpenseListsUiState.Success(sortedLists)
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

    private fun showDeleteConfirmation(list: ExpenseList) {
        val currentState = _uiState.value
        if (currentState is ExpenseListsUiState.Success) {
            _uiState.value =
                currentState.copy(
                    showDeleteDialog = true,
                    listToDelete = list,
                )
        }
    }

    private fun cancelDelete() {
        val currentState = _uiState.value
        if (currentState is ExpenseListsUiState.Success) {
            _uiState.value =
                currentState.copy(
                    showDeleteDialog = false,
                    listToDelete = null,
                )
        }
    }

    private fun confirmDelete() {
        val currentState = _uiState.value
        if (currentState is ExpenseListsUiState.Success) {
            val listToDelete = currentState.listToDelete
            if (listToDelete != null) {
                viewModelScope.launch {
                    val result = deleteExpenseListUseCase.invoke(listToDelete.listId)

                    result.onSuccess {
                        // Remove the list from cached data
                        val updatedLists = cachedLists?.filter { it.listId != listToDelete.listId } ?: emptyList()
                        cachedLists = updatedLists

                        _uiState.value =
                            if (updatedLists.isEmpty()) {
                                ExpenseListsUiState.Empty
                            } else {
                                ExpenseListsUiState.Success(updatedLists)
                            }

                        // Notify parent that list was deleted
                        onListDeleted(listToDelete.listId)
                    }

                    result.onFailure { error ->
                        // Show error but keep showing the list
                        _uiState.value =
                            currentState.copy(
                                showDeleteDialog = false,
                                listToDelete = null,
                            )
                        _uiState.value =
                            ExpenseListsUiState.Error(
                                error.message ?: "Failed to delete list",
                                cachedLists,
                            )
                    }
                }
            }
        }
    }

    private fun editList(list: ExpenseList) {
        // Callback will be handled by the screen to navigate to edit
        // This is a placeholder - the screen will handle navigation
    }
}
