package com.marquis.zorroexpense.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.marquis.zorroexpense.domain.model.Category
import com.marquis.zorroexpense.domain.model.ExpenseList
import com.marquis.zorroexpense.domain.model.User
import com.marquis.zorroexpense.domain.usecase.CreateExpenseListUseCase
import com.marquis.zorroexpense.domain.usecase.DeleteExpenseListUseCase
import com.marquis.zorroexpense.domain.usecase.GetExpenseListByIdUseCase
import com.marquis.zorroexpense.domain.usecase.UpdateExpenseListUseCase
import com.marquis.zorroexpense.presentation.state.ExpenseListDetailUiEvent
import com.marquis.zorroexpense.presentation.state.ExpenseListDetailUiState
import com.marquis.zorroexpense.presentation.state.ListDetailMode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ExpenseListDetailViewModel(
    private val listId: String,
    private val userId: String,
    private val initialExpenseList: ExpenseList,
    private val initialMode: ListDetailMode,
    private val deleteExpenseListUseCase: DeleteExpenseListUseCase,
    private val getExpenseListByIdUseCase: GetExpenseListByIdUseCase,
    private val updateExpenseListUseCase: UpdateExpenseListUseCase,
    private val createExpenseListUseCase: CreateExpenseListUseCase,
    private val onListDeleted: () -> Unit = {},
    private val onListSaved: (listId: String, listName: String) -> Unit = { _, _ -> },
) : ViewModel() {
    private val _uiState = MutableStateFlow<ExpenseListDetailUiState>(
        ExpenseListDetailUiState.Success(
            expenseList = initialExpenseList,
            mode = initialMode,
        ),
    )
    val uiState: StateFlow<ExpenseListDetailUiState> = _uiState.asStateFlow()

    /** Exposes the current user ID to the UI for determining which members can be removed */
    val currentUserId: String get() = userId

    fun onEvent(event: ExpenseListDetailUiEvent) {
        when (event) {
            is ExpenseListDetailUiEvent.DeleteList -> showDeleteConfirmation()
            is ExpenseListDetailUiEvent.ConfirmDelete -> confirmDelete()
            is ExpenseListDetailUiEvent.CancelDelete -> cancelDelete()
            is ExpenseListDetailUiEvent.EnterEditMode -> enterEditMode()
            is ExpenseListDetailUiEvent.CancelEdit -> cancelEdit()
            is ExpenseListDetailUiEvent.SaveChanges -> saveChanges()
            is ExpenseListDetailUiEvent.UpdateName -> updateName(event.name)
            is ExpenseListDetailUiEvent.AddCategoryClicked -> onAddCategoryClicked()
            is ExpenseListDetailUiEvent.RemoveCategory -> removeCategory(event.category)
            is ExpenseListDetailUiEvent.RemoveMember -> removeMember(event.member)
        }
    }

    /**
     * Refresh the expense list data from the repository.
     * Called when returning from edit screen to get updated data.
     */
    fun refreshData() {
        viewModelScope.launch {
            getExpenseListByIdUseCase(listId).onSuccess { expenseList ->
                if (expenseList != null) {
                    val currentState = _uiState.value
                    if (currentState is ExpenseListDetailUiState.Success) {
                        _uiState.value = currentState.copy(
                            expenseList = expenseList,
                            editedName = expenseList.name,
                            editedCategories = expenseList.categories,
                            editedMembers = expenseList.members,
                        )
                    } else {
                        _uiState.value = ExpenseListDetailUiState.Success(expenseList)
                    }
                }
            }
        }
    }

    private fun enterEditMode() {
        val currentState = _uiState.value
        if (currentState is ExpenseListDetailUiState.Success) {
            _uiState.update {
                currentState.copy(
                    mode = ListDetailMode.EDIT,
                    editedName = currentState.expenseList.name,
                    editedCategories = currentState.expenseList.categories,
                    editedMembers = currentState.expenseList.members,
                )
            }
        }
    }

    private fun cancelEdit() {
        val currentState = _uiState.value
        if (currentState is ExpenseListDetailUiState.Success) {
            _uiState.update {
                currentState.copy(
                    mode = ListDetailMode.VIEW,
                    editedName = currentState.expenseList.name,
                    editedCategories = currentState.expenseList.categories,
                    editedMembers = currentState.expenseList.members,
                )
            }
        }
    }

    private fun updateName(name: String) {
        val currentState = _uiState.value
        if (currentState is ExpenseListDetailUiState.Success) {
            _uiState.update {
                currentState.copy(editedName = name)
            }
        }
    }

    private fun onAddCategoryClicked() {
        // TODO: Will be implemented later - navigate to category picker
    }

    private fun removeCategory(category: Category) {
        val currentState = _uiState.value
        if (currentState is ExpenseListDetailUiState.Success) {
            _uiState.update {
                currentState.copy(
                    editedCategories = currentState.editedCategories.filter {
                        it.documentId != category.documentId
                    },
                )
            }
        }
    }

    private fun removeMember(member: User) {
        // Cannot remove yourself
        if (member.userId == userId) return

        val currentState = _uiState.value
        if (currentState is ExpenseListDetailUiState.Success) {
            _uiState.update {
                currentState.copy(
                    editedMembers = currentState.editedMembers.filter {
                        it.userId != member.userId
                    },
                )
            }
        }
    }

    private fun saveChanges() {
        viewModelScope.launch {
            val currentState = _uiState.value
            if (currentState is ExpenseListDetailUiState.Success) {
                _uiState.update { currentState.copy(isSaving = true) }

                val updatedList = currentState.expenseList.copy(
                    name = currentState.editedName,
                    categories = currentState.editedCategories,
                    members = currentState.editedMembers,
                )

                when (currentState.mode) {
                    ListDetailMode.ADD -> {
                        val categoryIds = currentState.editedCategories.map { it.documentId }
                        createExpenseListUseCase(
                            userId = userId,
                            name = currentState.editedName,
                            categoryIds = categoryIds,
                        ).fold(
                            onSuccess = { newListId ->
                                _uiState.update {
                                    currentState.copy(
                                        isSaving = false,
                                        expenseList = updatedList.copy(listId = newListId),
                                        mode = ListDetailMode.VIEW,
                                    )
                                }
                                onListSaved(newListId, updatedList.name)
                            },
                            onFailure = { error ->
                                _uiState.value = ExpenseListDetailUiState.Error(
                                    error.message ?: "Failed to create list",
                                )
                            },
                        )
                    }
                    ListDetailMode.EDIT -> {
                        updateExpenseListUseCase(listId, updatedList).fold(
                            onSuccess = {
                                _uiState.update {
                                    currentState.copy(
                                        isSaving = false,
                                        expenseList = updatedList,
                                        mode = ListDetailMode.VIEW,
                                    )
                                }
                                onListSaved(listId, updatedList.name)
                            },
                            onFailure = { error ->
                                _uiState.value = ExpenseListDetailUiState.Error(
                                    error.message ?: "Failed to update list",
                                )
                            },
                        )
                    }
                    ListDetailMode.VIEW -> {
                        // Should not happen
                        _uiState.update { currentState.copy(isSaving = false) }
                    }
                }
            }
        }
    }

    private fun showDeleteConfirmation() {
        val currentState = _uiState.value
        if (currentState is ExpenseListDetailUiState.Success) {
            _uiState.update {
                currentState.copy(showDeleteDialog = true)
            }
        }
    }

    private fun cancelDelete() {
        val currentState = _uiState.value
        if (currentState is ExpenseListDetailUiState.Success) {
            _uiState.update {
                currentState.copy(showDeleteDialog = false)
            }
        }
    }

    private fun confirmDelete() {
        viewModelScope.launch {
            val currentState = _uiState.value
            if (currentState is ExpenseListDetailUiState.Success) {
                _uiState.update {
                    currentState.copy(showDeleteDialog = false)
                }

                deleteExpenseListUseCase(listId).fold(
                    onSuccess = {
                        _uiState.value = ExpenseListDetailUiState.Deleted
                        onListDeleted()
                    },
                    onFailure = { error ->
                        _uiState.value = ExpenseListDetailUiState.Error(
                            error.message ?: "Failed to delete list",
                        )
                    },
                )
            }
        }
    }
}
