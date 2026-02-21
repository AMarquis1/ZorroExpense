package com.marquis.zorroexpense.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.marquis.zorroexpense.domain.model.Category
import com.marquis.zorroexpense.domain.usecase.CreateCategoryUseCase
import com.marquis.zorroexpense.domain.usecase.DeleteCategoryUseCase
import com.marquis.zorroexpense.domain.usecase.UpdateCategoryUseCase
import com.marquis.zorroexpense.presentation.state.CategoryDetailMode
import com.marquis.zorroexpense.presentation.state.CategoryDetailUiEvent
import com.marquis.zorroexpense.presentation.state.CategoryDetailUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class CategoryDetailViewModel(
    private val groupId: String,
    private val category: Category,
    initialMode: CategoryDetailMode,
    private val createCategoryUseCase: CreateCategoryUseCase,
    private val updateCategoryUseCase: UpdateCategoryUseCase,
    private val deleteCategoryUseCase: DeleteCategoryUseCase,
    private val onCategorySaved: () -> Unit = {},
    private val onCategoryDeleted: () -> Unit = {},
) : ViewModel() {
    private val _uiState = MutableStateFlow<CategoryDetailUiState>(
        CategoryDetailUiState.Success(
            category = category,
            mode = initialMode,
        ),
    )
    val uiState: StateFlow<CategoryDetailUiState> = _uiState.asStateFlow()

    fun onEvent(event: CategoryDetailUiEvent) {
        when (event) {
            is CategoryDetailUiEvent.EnterEditMode -> enterEditMode()
            is CategoryDetailUiEvent.CancelEdit -> cancelEdit()
            is CategoryDetailUiEvent.SaveChanges -> saveChanges()
            is CategoryDetailUiEvent.UpdateName -> updateName(event.name)
            is CategoryDetailUiEvent.UpdateIcon -> updateIcon(event.icon)
            is CategoryDetailUiEvent.UpdateColor -> updateColor(event.color)
            is CategoryDetailUiEvent.DeleteCategory -> showDeleteConfirmation()
            is CategoryDetailUiEvent.ConfirmDelete -> confirmDelete()
            is CategoryDetailUiEvent.CancelDelete -> cancelDelete()
        }
    }

    private fun enterEditMode() {
        val currentState = _uiState.value
        if (currentState is CategoryDetailUiState.Success) {
            _uiState.update {
                currentState.copy(
                    mode = CategoryDetailMode.EDIT,
                    editedName = currentState.category.name,
                    editedIcon = currentState.category.icon,
                    editedColor = currentState.category.color,
                )
            }
        }
    }

    private fun cancelEdit() {
        val currentState = _uiState.value
        if (currentState is CategoryDetailUiState.Success) {
            _uiState.update {
                currentState.copy(
                    mode = CategoryDetailMode.VIEW,
                    editedName = currentState.category.name,
                    editedIcon = currentState.category.icon,
                    editedColor = currentState.category.color,
                )
            }
        }
    }

    private fun updateName(name: String) {
        val currentState = _uiState.value
        if (currentState is CategoryDetailUiState.Success) {
            _uiState.update {
                currentState.copy(editedName = name)
            }
        }
    }

    private fun updateIcon(icon: String) {
        val currentState = _uiState.value
        if (currentState is CategoryDetailUiState.Success) {
            _uiState.update {
                currentState.copy(editedIcon = icon)
            }
        }
    }

    private fun updateColor(color: String) {
        val currentState = _uiState.value
        if (currentState is CategoryDetailUiState.Success) {
            _uiState.update {
                currentState.copy(editedColor = color)
            }
        }
    }

    private fun saveChanges() {
        viewModelScope.launch {
            val currentState = _uiState.value
            if (currentState is CategoryDetailUiState.Success) {
                _uiState.update { currentState.copy(isSaving = true) }

                val updatedCategory = currentState.category.copy(
                    name = currentState.editedName,
                    icon = currentState.editedIcon,
                    color = currentState.editedColor,
                )

                when (currentState.mode) {
                    CategoryDetailMode.ADD -> {
                        createCategoryUseCase(groupId, updatedCategory).fold(
                            onSuccess = { newCategoryId ->
                                _uiState.update {
                                    currentState.copy(
                                        isSaving = false,
                                        category = updatedCategory.copy(documentId = newCategoryId),
                                        mode = CategoryDetailMode.VIEW,
                                    )
                                }
                                onCategorySaved()
                            },
                            onFailure = { error ->
                                _uiState.value = CategoryDetailUiState.Error(
                                    error.message ?: "Failed to create category",
                                )
                            },
                        )
                    }
                    CategoryDetailMode.EDIT -> {
                        updateCategoryUseCase(groupId, updatedCategory).fold(
                            onSuccess = {
                                _uiState.update {
                                    currentState.copy(
                                        isSaving = false,
                                        category = updatedCategory,
                                        mode = CategoryDetailMode.VIEW,
                                    )
                                }
                                onCategorySaved()
                            },
                            onFailure = { error ->
                                _uiState.value = CategoryDetailUiState.Error(
                                    error.message ?: "Failed to update category",
                                )
                            },
                        )
                    }
                    CategoryDetailMode.VIEW -> {
                        _uiState.update { currentState.copy(isSaving = false) }
                    }
                }
            }
        }
    }

    private fun showDeleteConfirmation() {
        val currentState = _uiState.value
        if (currentState is CategoryDetailUiState.Success) {
            _uiState.update {
                currentState.copy(showDeleteDialog = true)
            }
        }
    }

    private fun cancelDelete() {
        val currentState = _uiState.value
        if (currentState is CategoryDetailUiState.Success) {
            _uiState.update {
                currentState.copy(showDeleteDialog = false)
            }
        }
    }

    private fun confirmDelete() {
        viewModelScope.launch {
            val currentState = _uiState.value
            if (currentState is CategoryDetailUiState.Success) {
                _uiState.update {
                    currentState.copy(showDeleteDialog = false)
                }

                deleteCategoryUseCase(groupId, currentState.category.documentId).fold(
                    onSuccess = {
                        _uiState.value = CategoryDetailUiState.Deleted
                        onCategoryDeleted()
                    },
                    onFailure = { error ->
                        _uiState.value = CategoryDetailUiState.Error(
                            error.message ?: "Failed to delete category",
                        )
                    },
                )
            }
        }
    }
}
