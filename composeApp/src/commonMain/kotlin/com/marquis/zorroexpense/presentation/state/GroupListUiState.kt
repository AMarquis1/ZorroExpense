package com.marquis.zorroexpense.presentation.state

import com.marquis.zorroexpense.domain.model.Group

/**
 * UI state for the expense lists selection screen
 */
sealed class GroupListUiState {
    data object Loading : GroupListUiState()

    data class Success(
        val lists: List<Group>,
        val isRefreshing: Boolean = false,
        val showDeleteDialog: Boolean = false,
        val listToDelete: Group? = null,
    ) : GroupListUiState()

    data class Error(
        val message: String,
        val cachedLists: List<Group>? = null,
    ) : GroupListUiState()

    data object Empty : GroupListUiState()
}

/**
 * User events for the expense lists selection screen
 */
sealed class GroupListUiEvent {
    data object LoadGroups : GroupListUiEvent()

    data object CreateNewGroup : GroupListUiEvent()
    data object RefreshGroups: GroupListUiEvent()

    data class SelectGroup(
        val listId: String,
    ) : GroupListUiEvent()

    data class JoinGroup(
        val shareCode: String,
    ) : GroupListUiEvent()

    data class DeleteGroup(val list: Group) : GroupListUiEvent()

    data object ConfirmDelete : GroupListUiEvent()

    data object CancelDelete : GroupListUiEvent()

    data class EditGroup(val list: Group) : GroupListUiEvent()

    data object RetryLoad : GroupListUiEvent()
}
