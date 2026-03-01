package com.marquis.zorroexpense.presentation.state

import com.marquis.zorroexpense.domain.model.Category
import io.github.ismoy.imagepickerkmp.domain.models.GalleryPhotoResult

sealed class CategoryManagementUiState {
    data object Loading : CategoryManagementUiState()

    data class Success(
        val groupId: String,
        val groupName: String,
        val categories: List<Category> = emptyList(),
        val isSaving: Boolean = false,
        val imageUrl: String = "",
        val isUploading: Boolean = false,
    ) : CategoryManagementUiState()

    data class Error(
        val message: String,
    ) : CategoryManagementUiState()
}

sealed class CategoryManagementUiEvent {
    data class CategoryToggled(
        val category: Category,
    ) : CategoryManagementUiEvent()

    data class RemoveCategory(
        val category: Category,
    ) : CategoryManagementUiEvent()

    data object SaveChanges : CategoryManagementUiEvent()

    data object CancelClicked : CategoryManagementUiEvent()

    data object AddCategoryClicked : CategoryManagementUiEvent()

    data object DismissCategoryBottomSheet : CategoryManagementUiEvent()

    data class PhotoSelected(
        val photo: GalleryPhotoResult,
    ) : CategoryManagementUiEvent()
}
