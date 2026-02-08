package com.marquis.zorroexpense.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.marquis.zorroexpense.domain.model.Group
import com.marquis.zorroexpense.domain.usecase.DeleteGroupUseCase
import com.marquis.zorroexpense.domain.usecase.GetUserGroupUseCase
import com.marquis.zorroexpense.domain.usecase.GetUsersUseCase
import com.marquis.zorroexpense.domain.usecase.JoinGroupUseCase
import com.marquis.zorroexpense.domain.usecase.RefreshUserGroupUseCase
import com.marquis.zorroexpense.presentation.state.GroupListUiEvent
import com.marquis.zorroexpense.presentation.state.GroupListUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel for managing expense lists selection
 */
class GroupListViewModel(
    private val userId: String,
    private val getUserGroupUseCase: GetUserGroupUseCase,
    private val refreshUserGroupUseCase: RefreshUserGroupUseCase,
    private val joinGroupUseCase: JoinGroupUseCase,
    private val deleteExpenseListUseCase: DeleteGroupUseCase,
    private val getUsersUseCase: GetUsersUseCase,
    private val onListSelected: (listId: String, listName: String) -> Unit = { _, _ -> },
    private val onListDeleted: (listId: String) -> Unit = { _ -> },
) : ViewModel() {
    private val _uiState = MutableStateFlow<GroupListUiState>(GroupListUiState.Loading)
    val uiState: StateFlow<GroupListUiState> = _uiState.asStateFlow()

    private var cachedLists: List<Group>? = null

    init {
        loadLists()
    }

    fun onEvent(event: GroupListUiEvent) {
        when (event) {
            is GroupListUiEvent.LoadGroups -> loadLists()
            is GroupListUiEvent.RefreshGroups -> refreshLists()
            is GroupListUiEvent.CreateNewGroup -> {}
            is GroupListUiEvent.SelectGroup -> selectList(event.listId)
            is GroupListUiEvent.JoinGroup -> joinList(event.shareCode)
            is GroupListUiEvent.DeleteGroup -> showDeleteConfirmation(event.list)
            is GroupListUiEvent.ConfirmDelete -> confirmDelete()
            is GroupListUiEvent.CancelDelete -> cancelDelete()
            is GroupListUiEvent.EditGroup -> editList(event.list)
            is GroupListUiEvent.RetryLoad -> loadLists()
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
            val shouldShowCacheImmediately = showCacheImmediately || (currentState is GroupListUiState.Success)

            // If we have cached data and should show it immediately, do so
            if (shouldShowCacheImmediately && (cachedLists != null || currentState is GroupListUiState.Success)) {
                val listsToShow = cachedLists ?: (currentState as? GroupListUiState.Success)?.lists ?: emptyList()
                _uiState.value = GroupListUiState.Success(listsToShow, isRefreshing = true)
            } else {
                _uiState.value = GroupListUiState.Loading
            }

            // Use refresh use case for force refresh (pull-to-refresh), otherwise use normal get
            val result = if (forceRefresh) {
                refreshUserGroupUseCase.invoke(userId)
            } else {
                getUserGroupUseCase.invoke(userId)
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
                                GroupListUiState.Empty
                            } else {
                                GroupListUiState.Success(enrichedLists)
                            }
                    }.onFailure {
                        val sortedLists = lists.sortedByDescending { it.lastModified }
                        cachedLists = sortedLists

                        _uiState.value =
                            if (sortedLists.isEmpty()) {
                                GroupListUiState.Empty
                            } else {
                                GroupListUiState.Success(sortedLists)
                            }
                    }
                } else {
                    val sortedLists = lists.sortedByDescending { it.lastModified }
                    cachedLists = sortedLists

                    _uiState.value =
                        if (sortedLists.isEmpty()) {
                            GroupListUiState.Empty
                        } else {
                            GroupListUiState.Success(sortedLists)
                        }
                }
            }

            result.onFailure { error ->
                // Show error but keep cached data visible if available
                _uiState.value =
                    GroupListUiState.Error(
                        error.message ?: "Failed to load expense lists",
                        cachedLists,
                    )
            }
        }
    }

    private fun selectList(listId: String) {
        val currentState = _uiState.value
        if (currentState is GroupListUiState.Success) {
            val selectedList = currentState.lists.find { it.listId == listId }
            selectedList?.let {
                onListSelected(it.listId, it.name)
            }
        }
    }

    private fun joinList(shareCode: String) {
        viewModelScope.launch {
            _uiState.value = GroupListUiState.Loading
            val result = joinGroupUseCase.invoke(userId, shareCode)

            result.onSuccess { list ->
                onListSelected(list.listId, list.name)
            }

            result.onFailure { error ->
                // Show error but keep current lists visible
                val currentState = _uiState.value
                if (currentState is GroupListUiState.Success) {
                    _uiState.value =
                        GroupListUiState.Error(
                            error.message ?: "Failed to join list",
                        )
                    // Reload lists after a moment
                    loadLists()
                } else {
                    _uiState.value =
                        GroupListUiState.Error(
                            error.message ?: "Failed to join list",
                        )
                }
            }
        }
    }

    private fun showDeleteConfirmation(list: Group) {
        val currentState = _uiState.value
        if (currentState is GroupListUiState.Success) {
            _uiState.value =
                currentState.copy(
                    showDeleteDialog = true,
                    listToDelete = list,
                )
        }
    }

    private fun cancelDelete() {
        val currentState = _uiState.value
        if (currentState is GroupListUiState.Success) {
            _uiState.value =
                currentState.copy(
                    showDeleteDialog = false,
                    listToDelete = null,
                )
        }
    }

    private fun confirmDelete() {
        val currentState = _uiState.value
        if (currentState is GroupListUiState.Success) {
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
                                GroupListUiState.Empty
                            } else {
                                GroupListUiState.Success(updatedLists)
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
                            GroupListUiState.Error(
                                error.message ?: "Failed to delete list",
                                cachedLists,
                            )
                    }
                }
            }
        }
    }

    private fun editList(list: Group) {
        // Callback will be handled by the screen to navigate to edit
        // This is a placeholder - the screen will handle navigation
    }
}
