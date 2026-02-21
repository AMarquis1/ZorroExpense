package com.marquis.zorroexpense.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.marquis.zorroexpense.domain.model.Category
import com.marquis.zorroexpense.domain.model.Group
import com.marquis.zorroexpense.domain.model.User
import com.marquis.zorroexpense.domain.usecase.CreateGroupUseCase
import com.marquis.zorroexpense.domain.usecase.DeleteGroupUseCase
import com.marquis.zorroexpense.domain.usecase.GetCategoriesUseCase
import com.marquis.zorroexpense.domain.usecase.GetGroupByIdUseCase
import com.marquis.zorroexpense.domain.usecase.UpdateGroupUseCase
import com.marquis.zorroexpense.presentation.state.GroupDetailUiEvent
import com.marquis.zorroexpense.presentation.state.GroupDetailUiState
import com.marquis.zorroexpense.presentation.state.GroupDetailMode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class GroupDetailViewModel(
    private val listId: String,
    private val userId: String,
    initialGroup: Group,
    initialMode: GroupDetailMode,
    private val deleteGroupUseCase: DeleteGroupUseCase,
    private val getGroupByIdUseCase: GetGroupByIdUseCase,
    private val updateGroupUseCase: UpdateGroupUseCase,
    private val createGroupUseCase: CreateGroupUseCase,
    private val getCategoriesUseCase: GetCategoriesUseCase,
    private val onListDeleted: () -> Unit = {},
    private val onListSaved: (listId: String, listName: String) -> Unit = { _, _ -> },
) : ViewModel() {
    private val _uiState = MutableStateFlow<GroupDetailUiState>(
        GroupDetailUiState.Success(
            group = initialGroup,
            mode = initialMode,
        ),
    )
    val uiState: StateFlow<GroupDetailUiState> = _uiState.asStateFlow()

    private val _allCategories = MutableStateFlow<List<Category>>(emptyList())
    val allCategories: StateFlow<List<Category>> = _allCategories.asStateFlow()

    /** Exposes the current user ID to the UI for determining which members can be removed */
    val currentUserId: String get() = userId

    init {
        loadCategories()
    }

    fun onEvent(event: GroupDetailUiEvent) {
        when (event) {
            is GroupDetailUiEvent.DeleteGroup -> showDeleteConfirmation()
            is GroupDetailUiEvent.ConfirmDelete -> confirmDelete()
            is GroupDetailUiEvent.CancelDelete -> cancelDelete()
            is GroupDetailUiEvent.EnterEditMode -> enterEditMode()
            is GroupDetailUiEvent.CancelEdit -> cancelEdit()
            is GroupDetailUiEvent.SaveChanges -> saveChanges()
            is GroupDetailUiEvent.UpdateName -> updateName(event.name)
            is GroupDetailUiEvent.AddCategoryClicked -> onAddCategoryClicked()
            is GroupDetailUiEvent.CategoryToggled -> toggleCategory(event.category)
            is GroupDetailUiEvent.DismissCategoryBottomSheet -> dismissCategoryBottomSheet()
            is GroupDetailUiEvent.RemoveCategory -> removeCategory(event.category)
            is GroupDetailUiEvent.RemoveMember -> showDeleteMemberConfirmation(event.member)
            is GroupDetailUiEvent.ConfirmDeleteMember -> confirmDeleteMember()
            is GroupDetailUiEvent.CancelDeleteMember -> cancelDeleteMember()
        }
    }

    /**
     * Refresh the expense list data from the repository.
     * Called when returning from edit screen to get updated data.
     */
    fun refreshData() {
        viewModelScope.launch {
            getGroupByIdUseCase(listId).onSuccess { expenseList ->
                if (expenseList != null) {
                    val currentState = _uiState.value
                    if (currentState is GroupDetailUiState.Success) {
                        _uiState.value = currentState.copy(
                            group = expenseList,
                            editedName = expenseList.name,
                            editedCategories = expenseList.categories,
                            editedMembers = expenseList.members,
                        )
                    } else {
                        _uiState.value = GroupDetailUiState.Success(expenseList)
                    }
                }
            }
        }
    }

    private fun enterEditMode() {
        val currentState = _uiState.value
        if (currentState is GroupDetailUiState.Success) {
            _uiState.update {
                currentState.copy(
                    mode = GroupDetailMode.EDIT,
                    editedName = currentState.group.name,
                    editedCategories = currentState.group.categories,
                    editedMembers = currentState.group.members,
                )
            }
        }
    }

    private fun cancelEdit() {
        val currentState = _uiState.value
        if (currentState is GroupDetailUiState.Success) {
            _uiState.update {
                currentState.copy(
                    mode = GroupDetailMode.VIEW,
                    editedName = currentState.group.name,
                    editedCategories = currentState.group.categories,
                    editedMembers = currentState.group.members,
                )
            }
        }
    }

    private fun updateName(name: String) {
        val currentState = _uiState.value
        if (currentState is GroupDetailUiState.Success) {
            _uiState.update {
                currentState.copy(editedName = name)
            }
        }
    }

    private fun onAddCategoryClicked() {
        val currentState = _uiState.value
        if (currentState is GroupDetailUiState.Success) {
            _uiState.update {
                currentState.copy(showCategoryBottomSheet = true)
            }
        }
    }

    private fun toggleCategory(category: Category) {
        val currentState = _uiState.value
        if (currentState is GroupDetailUiState.Success) {
            val isAlreadySelected = currentState.editedCategories.any {
                it.documentId == category.documentId
            }

            val updatedCategories = if (isAlreadySelected) {
                currentState.editedCategories.filter {
                    it.documentId != category.documentId
                }
            } else {
                currentState.editedCategories + category
            }

            _uiState.update {
                currentState.copy(editedCategories = updatedCategories)
            }
        }
    }

    private fun dismissCategoryBottomSheet() {
        val currentState = _uiState.value
        if (currentState is GroupDetailUiState.Success) {
            _uiState.update {
                currentState.copy(showCategoryBottomSheet = false)
            }
        }
    }

    private fun removeCategory(category: Category) {
        val currentState = _uiState.value
        if (currentState is GroupDetailUiState.Success) {
            _uiState.update {
                currentState.copy(
                    editedCategories = currentState.editedCategories.filter {
                        it.documentId != category.documentId
                    },
                )
            }
        }
    }

    private fun showDeleteMemberConfirmation(member: User) {
        // Cannot remove yourself
        if (member.userId == userId) return

        val currentState = _uiState.value
        if (currentState is GroupDetailUiState.Success) {
            _uiState.update {
                currentState.copy(
                    showDeleteMemberDialog = true,
                    memberToDelete = member,
                )
            }
        }
    }

    private fun confirmDeleteMember() {
        val currentState = _uiState.value
        if (currentState is GroupDetailUiState.Success && currentState.memberToDelete != null) {
            val memberToRemove = currentState.memberToDelete
            _uiState.update {
                currentState.copy(
                    showDeleteMemberDialog = false,
                    memberToDelete = null,
                    editedMembers = currentState.editedMembers.filter {
                        it.userId != memberToRemove.userId
                    },
                )
            }
        }
    }

    private fun cancelDeleteMember() {
        val currentState = _uiState.value
        if (currentState is GroupDetailUiState.Success) {
            _uiState.update {
                currentState.copy(
                    showDeleteMemberDialog = false,
                    memberToDelete = null,
                )
            }
        }
    }

    private fun saveChanges() {
        viewModelScope.launch {
            val currentState = _uiState.value
            if (currentState is GroupDetailUiState.Success) {
                _uiState.update { currentState.copy(isSaving = true) }

                val updatedList = currentState.group.copy(
                    name = currentState.editedName,
                    categories = currentState.editedCategories,
                    members = currentState.editedMembers,
                )

                when (currentState.mode) {
                    GroupDetailMode.ADD -> {
                        createGroupUseCase(
                            userId = userId,
                            name = currentState.editedName,
                            categories = currentState.editedCategories,
                        ).fold(
                            onSuccess = { newListId ->
                                _uiState.update {
                                    currentState.copy(
                                        isSaving = false,
                                        group = updatedList.copy(listId = newListId),
                                        mode = GroupDetailMode.VIEW,
                                    )
                                }
                                onListSaved(newListId, updatedList.name)
                            },
                            onFailure = { error ->
                                _uiState.value = GroupDetailUiState.Error(
                                    error.message ?: "Failed to create list",
                                )
                            },
                        )
                    }
                    GroupDetailMode.EDIT -> {
                        updateGroupUseCase(listId, updatedList).fold(
                            onSuccess = {
                                _uiState.update {
                                    currentState.copy(
                                        isSaving = false,
                                        group = updatedList,
                                        mode = GroupDetailMode.VIEW,
                                    )
                                }
                                onListSaved(listId, updatedList.name)
                            },
                            onFailure = { error ->
                                _uiState.value = GroupDetailUiState.Error(
                                    error.message ?: "Failed to update list",
                                )
                            },
                        )
                    }
                    GroupDetailMode.VIEW -> {
                        // Should not happen
                        _uiState.update { currentState.copy(isSaving = false) }
                    }
                }
            }
        }
    }

    private fun showDeleteConfirmation() {
        val currentState = _uiState.value
        if (currentState is GroupDetailUiState.Success) {
            _uiState.update {
                currentState.copy(showDeleteDialog = true)
            }
        }
    }

    private fun cancelDelete() {
        val currentState = _uiState.value
        if (currentState is GroupDetailUiState.Success) {
            _uiState.update {
                currentState.copy(showDeleteDialog = false)
            }
        }
    }

    private fun confirmDelete() {
        viewModelScope.launch {
            val currentState = _uiState.value
            if (currentState is GroupDetailUiState.Success) {
                _uiState.update {
                    currentState.copy(showDeleteDialog = false)
                }

                deleteGroupUseCase(listId).fold(
                    onSuccess = {
                        _uiState.value = GroupDetailUiState.Deleted
                        onListDeleted()
                    },
                    onFailure = { error ->
                        _uiState.value = GroupDetailUiState.Error(
                            error.message ?: "Failed to delete list",
                        )
                    },
                )
            }
        }
    }

    fun loadCategories() {
        viewModelScope.launch {
            getCategoriesUseCase().fold(
                onSuccess = { categories ->
                    _allCategories.value = categories
                },
                onFailure = {
                    _allCategories.value = emptyList()
                },
            )
        }
    }
}
