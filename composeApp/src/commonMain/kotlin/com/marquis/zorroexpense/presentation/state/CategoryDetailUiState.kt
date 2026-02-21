package com.marquis.zorroexpense.presentation.state

import com.marquis.zorroexpense.domain.model.Category

enum class CategoryDetailMode {
    VIEW,
    EDIT,
    ADD,
}

sealed class CategoryDetailUiState {
    data object Loading : CategoryDetailUiState()

    data class Success(
        val category: Category,
        val mode: CategoryDetailMode = CategoryDetailMode.VIEW,
        val editedName: String = category.name,
        val editedIcon: String = category.icon,
        val editedColor: String = category.color,
        val isSaving: Boolean = false,
        val showDeleteDialog: Boolean = false,
    ) : CategoryDetailUiState()

    data object Deleted : CategoryDetailUiState()

    data class Error(
        val message: String,
    ) : CategoryDetailUiState()
}

sealed class CategoryDetailUiEvent {
    data object EnterEditMode : CategoryDetailUiEvent()

    data object CancelEdit : CategoryDetailUiEvent()

    data object SaveChanges : CategoryDetailUiEvent()

    data class UpdateName(val name: String) : CategoryDetailUiEvent()

    data class UpdateIcon(val icon: String) : CategoryDetailUiEvent()

    data class UpdateColor(val color: String) : CategoryDetailUiEvent()

    data object DeleteCategory : CategoryDetailUiEvent()

    data object ConfirmDelete : CategoryDetailUiEvent()

    data object CancelDelete : CategoryDetailUiEvent()
}
