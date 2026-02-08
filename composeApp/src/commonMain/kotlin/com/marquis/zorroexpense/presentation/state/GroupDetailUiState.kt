package com.marquis.zorroexpense.presentation.state

import com.marquis.zorroexpense.domain.model.Category
import com.marquis.zorroexpense.domain.model.Group
import com.marquis.zorroexpense.domain.model.User

enum class GroupDetailMode {
    VIEW,
    EDIT,
    ADD,
}

sealed class GroupDetailUiState {
    data object Loading : GroupDetailUiState()

    data class Success(
        val group: Group,
        val mode: GroupDetailMode = GroupDetailMode.VIEW,
        val showDeleteDialog: Boolean = false,
        val showDeleteMemberDialog: Boolean = false,
        val memberToDelete: User? = null,
        val isSaving: Boolean = false,
        val showCategoryBottomSheet: Boolean = false,
        // Editable fields for EDIT/ADD modes
        val editedName: String = group.name,
        val editedCategories: List<Category> = group.categories,
        val editedMembers: List<User> = group.members,
    ) : GroupDetailUiState()

    data object Deleted : GroupDetailUiState()

    data class Error(
        val message: String,
    ) : GroupDetailUiState()
}

sealed class GroupDetailUiEvent {
    data object DeleteGroup : GroupDetailUiEvent()

    data object ConfirmDelete : GroupDetailUiEvent()

    data object CancelDelete : GroupDetailUiEvent()

    /** Switch to edit mode */
    data object EnterEditMode : GroupDetailUiEvent()

    /** Cancel edit and return to view mode */
    data object CancelEdit : GroupDetailUiEvent()

    /** Save changes (for EDIT/ADD modes) */
    data object SaveChanges : GroupDetailUiEvent()

    /** Update list name */
    data class UpdateName(val name: String) : GroupDetailUiEvent()

    /** Add a category */
    data object AddCategoryClicked : GroupDetailUiEvent()

    /** Toggle a category selection in the bottom sheet */
    data class CategoryToggled(val category: Category) : GroupDetailUiEvent()

    /** Dismiss the category selection bottom sheet */
    data object DismissCategoryBottomSheet : GroupDetailUiEvent()

    /** Remove a category */
    data class RemoveCategory(val category: Category) : GroupDetailUiEvent()

    /** Remove a member */
    data class RemoveMember(val member: User) : GroupDetailUiEvent()

    /** Confirm member deletion */
    data object ConfirmDeleteMember : GroupDetailUiEvent()

    /** Cancel member deletion */
    data object CancelDeleteMember : GroupDetailUiEvent()
}
